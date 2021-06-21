package com.intelygenz.android.instrumentation

import cucumber.api.StepDefinitionReporter
import cucumber.runtime.*
import cucumber.runtime.model.CucumberFeature
import cucumber.runtime.xstream.LocalizedXStreams
import gherkin.I18n
import gherkin.formatter.Argument
import gherkin.formatter.model.Step
import java.lang.IllegalStateException
import java.lang.reflect.Type


internal class AcceptanceRuntimeGlue(private val localizedXStreams: LocalizedXStreams,
                                     private val features: List<CucumberFeature>) : RuntimeGlue(undefinedStepsTracker, localizedXStreams) {

    private var stepDefinitionsByPattern = mapOf<String, StepDefinition>()
    private var beforeHooks = listOf<HookDefinition>()
    private var afterHooks = listOf<HookDefinition>()


    override fun addStepDefinition(stepDefinition: StepDefinition) {
        val key = stepDefinition.pattern + stepDefinition.getLocation(true)
        stepDefinitionsByPattern = (stepDefinitionsByPattern.toList() + (key to stepDefinition)).toMap()
    }

    override fun addBeforeHook(hookDefinition: HookDefinition) {
        beforeHooks = (beforeHooks + hookDefinition).sortedBy { it.order }
    }

    override fun addAfterHook(hookDefinition: HookDefinition) {
        afterHooks = (afterHooks + hookDefinition).sortedByDescending { it.order }
    }

    override fun getBeforeHooks(): List<HookDefinition> {
        return beforeHooks
    }

    override fun getAfterHooks(): List<HookDefinition> {
        return afterHooks
    }

    override fun stepDefinitionMatch(featurePath: String, step: Step, i18n: I18n): StepDefinitionMatch? {
        val matches = stepDefinitionMatches(featurePath, step)
        try {
            return when {
                matches.isEmpty() -> undefinedStepsTracker.addUndefinedStep(step, i18n).let{ matchThrow(featurePath, step, localizedXStreams, IllegalStateException("Step not implemented: ${step.name}")) }
                matches.size == 1 -> matches[0]
                else -> matchThrow(featurePath, step, localizedXStreams, AmbiguousStepDefinitionsException(matches))
            }
        } finally {
            undefinedStepsTracker.storeStepKeyword(step, i18n)
        }

    }

    private fun stepDefinitionMatches(featurePath: String, step: Step): List<StepDefinitionMatch> {
        return stepDefinitions(featurePath, step)
            .mapNotNull { stepDefinition -> stepDefinition.matchedArguments(step)?.let { StepDefinitionMatch(it, stepDefinition, featurePath, step, localizedXStreams) } }
    }

    internal fun stepDefinitions(featurePath: String, step: Step): List<StepDefinition> {
        return stepDefinitionsByPattern.values.filter { features.matchScenarioClass(featurePath, step, it) }
    }

    override fun reportStepDefinitions(stepDefinitionReporter: StepDefinitionReporter) {
        stepDefinitionsByPattern.values.forEach { stepDefinitionReporter.stepDefinition(it) }
    }

    override fun removeScenarioScopedGlue() {
        beforeHooks = beforeHooks.filter { !it.isScenarioScoped }
        afterHooks = afterHooks.filter { !it.isScenarioScoped }
        stepDefinitionsByPattern = stepDefinitionsByPattern.toList().filter { !it.second.isScenarioScoped }.toMap()
    }


    companion object {
        private val undefinedStepsTracker = UndefinedStepsTracker()
    }

}



private fun matchThrow(featurePath: String, step: Step, localizedXStreams: LocalizedXStreams, throwable: Throwable): ThrowableStepDefinitionMatch {
    return ThrowableStepDefinitionMatch(throwable, featurePath, step, localizedXStreams)
}

private class ThrowableStepDefinitionMatch(private val throwable: Throwable, featurePath: String, step: Step, localizedXStreams: LocalizedXStreams): StepDefinitionMatch(emptyList(), EmptyStepDefinition, featurePath, step, localizedXStreams) {
    override fun runStep(i18n: I18n?) {
        throw throwable
    }
    private object EmptyStepDefinition: StepDefinition {
        override fun matchedArguments(step: Step?): List<Argument> = emptyList()
        override fun getLocation(detail: Boolean): String  = ""
        override fun getParameterCount(): Int = 0
        override fun getParameterType(n: Int, argumentType: Type?): ParameterInfo {
            throw IllegalStateException("Should not be executed")
        }

        override fun execute(i18n: I18n?, args: Array<out Any>?) = Unit
        override fun isDefinedAt(stackTraceElement: StackTraceElement?): Boolean = false
        override fun getPattern(): String = ""
        override fun isScenarioScoped(): Boolean = false
    }
}