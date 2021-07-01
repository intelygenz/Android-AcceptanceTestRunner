package com.intelygenz.android.dsl.classpath

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier

fun <T: Any> runScenario(instance: T) = runScenario(instance::class.java)

fun runScenario(clazz: Class<*>) = clazz.getFeat().let { executeScenario(it.feature, it.scenario) }

fun runScenario(feature: String, scenario: String) = executeScenario(feature, scenario)

fun assertMissingScenarios() = assertAllScenariosExecuted()

private fun RunNotifier.runOn(description: Description, action: () -> Unit) {
    fireTestStarted(description)
    runCatching(action).onFailure { fireTestFailure(Failure(description, it)) }
    fireTestFinished(description)
}

class ScenarioRunner(testClass: Class<*>): Runner() {
    private val testName = "allScenariosExecuted"
    private val assertMethodDescription = Description.createTestDescription(testClass.name, testName, testName)
    private val innerRunner = AndroidJUnit4ClassRunner(testClass)
    override fun getDescription(): Description = innerRunner.description.apply { addChild(assertMethodDescription) }
    override fun run(notifier: RunNotifier) = innerRunner.run(notifier).also { notifier.runOn(assertMethodDescription, ::assertMissingScenarios) }
}