package com.intelygenz.android.instrumentation

import cucumber.runtime.model.CucumberScenario
import cucumber.runtime.model.StepContainer
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

interface RuleExecutor {
    fun execute(scenario: CucumberScenario, action: () -> Unit)
}

internal class GlueRuleExecutor(private val glue: AcceptanceRuntimeGlue): RuleExecutor {

    override fun execute(scenario: CucumberScenario, action: () -> Unit) {
        val featurePath = scenario.getCucumberFeature().path
        val scenarioRules = scenario.testRules(featurePath)
        val backgroundRules = scenario.cucumberBackground?.testRules(featurePath) ?: emptyList()
        (scenarioRules + backgroundRules).chain().apply(statementOf(action), Description.EMPTY).evaluate()
    }

    private fun StepContainer.testRules(featurePath: String): List<TestRule> = scenarioClass(featurePath)?.let { it.getTestRules(it.newInstance()) } ?: emptyList()
    private fun StepContainer.scenarioClass(featurePath: String): Class<*>? {
        val scenarioClassNames = steps.map { glue.stepDefinitions(featurePath, it) }.flatten().map { it.scenarioClassName() }.toSet()
        return when {
            scenarioClassNames.isEmpty() -> null
            else -> Class.forName(scenarioClassNames.first())
        }
    }

}

private fun List<TestRule>.chain(): RuleChain = fold(RuleChain.emptyRuleChain()) { acc, testRule -> acc.around(testRule) }

private fun Class<*>.getTestRules(instance: Any): List<TestRule> {
    return declaredFields.filter { it.annotations.any{ it.annotationClass.java == Rule::class.java } }
        .mapNotNull {
            it.isAccessible = true
            it.get(instance) as? TestRule
        }
}

private fun statementOf(action: () -> Unit): Statement = object: Statement() {
    override fun evaluate() = action()
}