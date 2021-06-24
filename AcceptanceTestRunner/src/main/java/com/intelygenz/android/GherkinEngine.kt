package com.intelygenz.android

import androidx.test.platform.app.InstrumentationRegistry
import com.intelygenz.android.parser.*
import java.lang.IllegalStateException

data class Arguments(val featurePaths: List<String>, val featureSelection: FeatureSelection)
sealed class FeatureSelection {
    object IncludeAll: FeatureSelection()
    data class Include(val included: List<String>, val skipped: List<String>) : FeatureSelection()
}

internal fun FeatureSelection.description(): String {
    return when(this) {
        FeatureSelection.IncludeAll -> "Include all tests"
        is FeatureSelection.Include -> when {
            included.isNotEmpty() && skipped.isNotEmpty() -> "Not tagged as $included or tagged as $skipped"
            included.isEmpty() && skipped.isNotEmpty() -> "Tagged as $skipped"
            included.isNotEmpty() && skipped.isEmpty() -> "Not tagged as $included"
            else -> ""
        }
    }
}
internal data class FeatureMatch(val featureId: String, val scenarios: List<ScenarioMatch>)
internal data class ScenarioMatch(val featureId: String, val scenarioId: String, val steps: List<StepMatch>, val backgroundSteps: List<StepMatch>?)
internal data class StepMatch(val execute: (Step) -> Boolean)


internal object GherkinEngine {
    private var gherkinTestRunner: GherkinTestRunner? = null
    fun getRunner(): GherkinTestRunner = gherkinTestRunner ?: createTestRunner().also { gherkinTestRunner = it  }

    private fun createTestRunner(): GherkinTestRunner = GherkinTestRunner(getArguments())

    private fun getArguments(): Arguments {
        val bundle = InstrumentationRegistry.getArguments()
        val acceptanceOptions = classPath.optionClasses.first().annotation
        val skipped = bundle.getStringArrayList("--skip")?.toList() ?: acceptanceOptions.excludeTags.toList()
        val included = bundle.getStringArrayList("--include")?.toList() ?: acceptanceOptions.includeTags.toList()
        val features = bundle.getStringArrayList("--features")?.toList() ?: acceptanceOptions.features.toList()
        val selection = if(skipped.isEmpty() && included.isEmpty()) FeatureSelection.IncludeAll else FeatureSelection.Include(included, skipped)
        return Arguments(features, selection)
    }

}

internal class GherkinTestRunner(private val arguments: Arguments) {

    private val parser = GherkinParser(arguments.featurePaths)
    private val features: List<Feature> by lazy { parser.parserFeatures() }

    fun assertFeatureExists(featureId: String): Feature = features.firstOrNull { it.matches(featureId) } ?: throw IllegalStateException("Expected Feature $featureId")
    fun background(featureId: String): Scenario? = assertFeatureExists(featureId).background

    fun assertMissingFeatures() {
        val definedFeatures = classPath.featClasses.map { it.annotation.feature }.toSet()
        val missingFeatures = features.filter { feature -> !definedFeatures.any{ feature.matches(it)} }
        if(missingFeatures.isNotEmpty()) throw IllegalStateException("Missing Features: ${missingFeatures.map { it.path }}")
        assertMissingScenarios()
    }

    private fun assertMissingScenarios() {
        val definedFeatures = classPath.featClasses.groupBy { it.annotation.feature }
        val missingScenarios = features.mapNotNull { feature ->
            definedFeatures.keys.firstOrNull { feature.matches(it) }?.let { key ->
                val definedScenarios = definedFeatures[key]!!.map { it.annotation.scenario }
                val scenarios = feature.scenarios + listOfNotNull(feature.background)
                key to scenarios.filter { scenario -> !definedScenarios.any{ scenario.matches(it) } }.map { it.description }
            }?.takeIf { it.second.isNotEmpty() }
        }
        if(missingScenarios.isNotEmpty()) throw IllegalStateException("Missing scenarios: $missingScenarios")
    }

    fun runFeature(featureMatch: FeatureMatch): Boolean {
        val feature = assertFeatureExists(featureMatch.featureId)
        return if(feature.shouldBeIgnored()) false else featureMatch.scenarios.forEach { runScenario(it) }.let { true }
    }

    fun runScenario(scenarioMatch: ScenarioMatch): Boolean {
        with(scenarioMatch) {
            val feature = assertFeatureExists(scenarioMatch.featureId)
            val scenario = feature.scenarios.firstOrNull { it.matches(scenarioId) } ?: throw IllegalStateException("Expected Scenario $featureId $scenarioId")
            return if(feature.shouldBeIgnored() || scenario.shouldBeIgnored()) false else {
                feature.background?.assertSteps(scenarioMatch.backgroundSteps ?: emptyList())
                scenario.assertSteps(steps)
                true
            }
        }
    }

    private fun Feature.shouldBeIgnored(): Boolean = arguments.featureSelection.shouldBeIgnored(annotations)
    private fun Scenario.shouldBeIgnored(): Boolean = arguments.featureSelection.shouldBeIgnored(annotations)

    private fun Scenario.assertSteps(steps: List<StepMatch>) {
        if(stepDescriptions.isNotEmpty() && steps.isEmpty()) throw IllegalStateException("${if(isBackground) "Background" else "Scenario"} not defined: $description")
        stepDescriptions.map { definedStep ->
            steps.firstOrNull { it.execute(definedStep)  } ?: throw IllegalStateException("Not found Step ${definedStep.tag} ${definedStep.name}")
        }
    }

    private fun FeatureSelection.shouldBeIgnored(tags: List<String>): Boolean {
        return when(this) {
            FeatureSelection.IncludeAll -> false
            is FeatureSelection.Include -> tags.any { skipped.contains(it) } || (included.isNotEmpty() && !tags.any { included.contains(it) })
        }
    }

}

private fun Feature.matches(id: String): Boolean = description == id || description.classCamelCase() == id || path == id || path.endsWith(id)
private fun Scenario.matches(id: String): Boolean = description == id || description.methodCamelCase() == id || "test${description.methodCamelCase()}" == id

private fun String.methodCamelCase(): String = split(" ").joinToString("") { it.capitalize() }
private fun String.classCamelCase(): String = methodCamelCase().capitalize()

