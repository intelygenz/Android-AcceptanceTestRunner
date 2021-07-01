package com.intelygenz.android.test

import com.intelygenz.android.dsl.classpath.ScenarioRunner
import com.intelygenz.android.dsl.classpath.runScenario
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(ScenarioRunner::class)
class ExampleInstrumentedTest {
    @Test
    fun example_someDeterminableBusinessSituation()  {
        runScenario("example", "Some determinable business situation")
    }

}
