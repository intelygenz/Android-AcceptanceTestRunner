package com.intelygenz.android.instrumentation

import com.intelygenz.android.Feat
import com.intelygenz.android.domains.Feature
import com.intelygenz.android.domains.Scenario
import cucumber.runtime.StepDefinition
import cucumber.runtime.model.CucumberBackground
import cucumber.runtime.model.CucumberFeature
import cucumber.runtime.model.StepContainer
import gherkin.formatter.model.BasicStatement
import gherkin.formatter.model.Step


internal fun List<CucumberFeature>.matchScenarioClass(featurePath: String, step: Step, stepDefinition: StepDefinition): Boolean {
    return firstOrNull { it.path.lowercase() == featurePath.lowercase() }
        ?.scenariosWith(step)
        ?.any{ it.matchWithStepDefinition(featurePath, stepDefinition) }
        ?: false
}

internal fun CucumberFeature.scenariosWith(step: Step): List<String> {
    fun sameStep(one: Step, other: Step): Boolean = (one.line == other.line)
    fun CucumberFeature.background(): CucumberBackground? = getField("cucumberBackground")
    fun StepContainer.statement(): BasicStatement? = getField("statement")
    fun CucumberBackground.name(): String = statement()?.name ?: ""

    val scenarios = featureElements.filter { it.steps.any{ sameStep(step, it) } }.map { it.visualName }
    val backgrounds = background()?.takeIf { it.steps.any{ sameStep(step, it) } }?.let { listOf(it.name()) } ?: emptyList()
    return scenarios + backgrounds
}

internal fun StepDefinition.scenarioClassName(): String = getLocation(true).split(" in ".toRegex())[1]

internal fun StepDefinition.scenarioClass(): Class<*> = Class.forName(scenarioClassName())

internal fun String.matchWithStepDefinition(featurePath: String, stepDefinition: StepDefinition): Boolean {
    try {
        val feat = stepDefinition.scenarioClass().getAnnotation(Feat::class.java) as Feat
        val isCoincident = Feature(feat.feature).normalized == Feature(featurePath).normalized
        return isCoincident && Scenario(feat.scenario).normalized == Scenario(this).normalized
    } catch (ignored: ClassNotFoundException) {
    }
    return false
}

internal inline fun <reified T, reified R> T.getField(name: String): R? {
    return kotlin.runCatching {
        val hierarchy = mutableListOf<Class<*>>()
        var clazz: Class<*> = T::class.java
        while(clazz != Object::class.java) { hierarchy.add(clazz); clazz = clazz.superclass }
        return hierarchy.flatMap { it.declaredFields.toList() }.firstOrNull { it.name == name }?.let {
            it.isAccessible = true
            it[this] as R
        }
    }.getOrNull()
}