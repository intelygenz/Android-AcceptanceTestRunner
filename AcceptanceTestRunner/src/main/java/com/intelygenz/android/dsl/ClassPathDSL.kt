package com.intelygenz.android.dsl

import com.intelygenz.android.*
import com.intelygenz.android.parser.StepTag
import org.junit.Assume
import java.lang.IllegalStateException
import java.lang.reflect.Method


fun assertMissingFeatures() = GherkinEngine.getRunner().assertMissingFeatures()
fun runFeature(feature: String) = GherkinEngine.getRunner().runFeature(feature)
fun runScenario(scenario: Class<*>, background: Class<*>?) = GherkinEngine.getRunner().runScenario(scenario, background)
fun runScenario(scenario: Class<*>) = GherkinEngine.getRunner().runScenario(scenario)

internal fun GherkinTestRunner.runFeature(feature: String) = Assume.assumeTrue(runFeature(classPath.toFeature(feature, background(feature)?.description)))
internal fun GherkinTestRunner.runScenario(scenario: Class<*>, background: Class<*>?) = Assume.assumeTrue(runScenario(scenario.toScenario(background)))
internal fun GherkinTestRunner.runScenario(scenario: Class<*>) {
    val feat = scenario.getFeat()
    val scenarioMatch = classPath.toFeature(feat.feature, background(feat.feature)?.description).scenarios.first { it.scenarioId == feat.scenario }
    Assume.assumeTrue(runScenario(scenarioMatch))
}


private fun ClassPath.toFeature(feature: String, background: String?): FeatureMatch {
    val allScenarios = featClasses.filter { it.annotation.feature == feature }
    val featureBackground = background?.let { allScenarios.firstOrNull { it.annotation.scenario == background }  ?: throw IllegalStateException("Not background implemented for Feature: $feature") }
    return FeatureMatch(feature, (allScenarios - listOfNotNull(featureBackground)).map { it.element.toScenario(featureBackground?.element) })
}

private fun Class<*>.toScenario(background: Class<*>? = null): ScenarioMatch {

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

private fun Class<*>.getFeat(): Feat = getAnnotation(Feat::class.java) ?: throw IllegalStateException("Scenario class is not annotated with Feat: $name")


private fun Method.getStepAnnotation() : Pair<StepTag, String>? = getAnnotation(Given::class.java)?.let { StepTag.GIVEN to it.regex }
        ?: getAnnotation(When::class.java)?.let { StepTag.WHEN to it.regex }
        ?: getAnnotation(Then::class.java)?.let { StepTag.THEN to it.regex }
        ?: getAnnotation(And::class.java)?.let { StepTag.AND to it.regex }
