package com.intelygenz.android.dsl

import com.intelygenz.android.GherkinEngine
import com.intelygenz.android.GherkinTestRunner
import com.intelygenz.android.ScenarioMatch
import com.intelygenz.android.StepMatch
import com.intelygenz.android.parser.StepTag
import org.junit.Assume

class ScenarioBuilder internal constructor() {
    internal val steps = mutableListOf<StepMatch>()
    internal fun append(step: StepMatch) = steps.add(step)
}
fun ScenarioBuilder.given(expression: String, action: () -> Unit) = append(StepMatch { it.runIf(StepTag.GIVEN, expression, action) })
fun ScenarioBuilder.`when`(expression: String, action: () -> Unit) = append(StepMatch { it.runIf(StepTag.WHEN, expression, action) })
fun ScenarioBuilder.then(expression: String, action: () -> Unit) = append(StepMatch { it.runIf(StepTag.THEN, expression, action) })
fun ScenarioBuilder.and(expression: String, action: () -> Unit) = append(StepMatch { it.runIf(StepTag.AND, expression, action) })

fun runScenario(feature: String, scenario: String, builder: ScenarioBuilder.() -> Unit, background: (ScenarioBuilder.() -> Unit)? = null)  = GherkinEngine.getRunner().runScenario(feature, scenario, builder, background)

internal fun GherkinTestRunner.runScenario(feature: String, scenario: String, builder: ScenarioBuilder.() -> Unit, background: (ScenarioBuilder.() -> Unit)?)  {
    val scenarioMatch = ScenarioMatch(feature, scenario, ScenarioBuilder().apply(builder).steps, background?.let { ScenarioBuilder().apply(it).steps })
    Assume.assumeTrue(runScenario(scenarioMatch))
}
