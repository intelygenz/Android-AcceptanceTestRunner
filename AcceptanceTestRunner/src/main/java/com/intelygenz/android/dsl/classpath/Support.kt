package com.intelygenz.android.dsl.classpath

import com.intelygenz.android.*
import com.intelygenz.android.dsl.*
import com.intelygenz.android.dsl.asScenarioId
import com.intelygenz.android.dsl.runIf
import com.intelygenz.android.dsl.skipOnFalse
import com.intelygenz.android.parser.StepTag
import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.lang.IllegalStateException
import java.lang.reflect.Method

private val runner get() = GherkinEngine.getRunner()

internal fun Class<*>.getFeat(): Feat = getAnnotation(Feat::class.java) ?: throw IllegalStateException("Scenario class is not annotated with Feat: $name")

internal fun assertAllScenariosExecuted() {
    val expected = runner.features.flatMap { feature -> feature.scenarios.map { asFeatureId(feature.path.fileName()) to asScenarioId(it.description) } }
    val executed = ScenarioExecution.get()
    (expected - executed).takeIf { it.isNotEmpty() }?.let { throw IllegalStateException("Some Scenarios where not executed $it") }
}

internal fun executeFeature(feature: String) {
    val featureId = asFeatureId(feature)
    val featureMatch = classPath.toFeature(featureId, runner.background(featureId)?.description)
    featureMatch.scenarios.forEach { ScenarioExecution.notify(it.featureId, it.scenarioId) }
    runner.runFeature(featureMatch).skipOnFalse()
}

internal fun executeScenario(feature: String, scenario: String) {
    val featureId = asFeatureId(feature)
    val scenarioId = asScenarioId(scenario)
    ScenarioExecution.notify(featureId, scenarioId)
    val featureMatch = classPath.toFeature(featureId, runner.background(featureId)?.description)
    val scenarioMatch = featureMatch.scenarios.firstOrNull { it.scenarioId == scenarioId } ?: throw IllegalStateException("Not found $feature.$scenario declared")
    runner.runScenario(scenarioMatch).skipOnFalse()
}

private object ScenarioExecution {
    private val list = mutableListOf<Pair<String, String>>()
    fun notify(feature: String, scenario: String) = list.add(feature to scenario)
    fun get(): List<Pair<String, String>> = list
}

private fun ClassPath.toFeature(feature: String, background: String?): FeatureMatch {
    val allScenarios = featClasses.filter { it.annotation.feature == feature }
    val featureBackground = background?.let { allScenarios.firstOrNull { it.annotation.scenario == background }  ?: throw IllegalStateException("Not background implemented for Feature: $feature") }
    return FeatureMatch(feature, (allScenarios - listOfNotNull(featureBackground)).map { it.element.toScenario(featureBackground?.element) })
}

private fun Class<*>.toScenario(background: Class<*>? = null): ScenarioMatch {
    fun Method.getStepAnnotation() : Pair<StepTag, String>? = getAnnotation(Given::class.java)?.let { StepTag.GIVEN to it.regex }
        ?: getAnnotation(When::class.java)?.let { StepTag.WHEN to it.regex }
        ?: getAnnotation(Then::class.java)?.let { StepTag.THEN to it.regex }
        ?: getAnnotation(And::class.java)?.let { StepTag.AND to it.regex }

    fun Method.toStepMatch(instance: Any): StepMatch? {
        val (tag, regex) = getStepAnnotation() ?: return null
        if (parameterTypes.isNotEmpty()) return null
        return StepMatch { it.runIf(tag, regex) { this@toStepMatch.invoke(instance) } }
    }
    fun Class<*>.toStepMatches(): List<StepMatch> {
        val list = newInstance().let { instance -> methods.mapNotNull { it.toStepMatch(instance) } }
        return list.takeIf { it.isNotEmpty() } ?: throw IllegalStateException("Scenario without defined steps: $this")
    }
    val scenarioFeat = getFeat()
    return ScenarioMatch(
        featureId = scenarioFeat.feature,
        scenarioId = scenarioFeat.scenario,
        steps = toStepMatches(),
        backgroundSteps = background?.toStepMatches()
    )
}



