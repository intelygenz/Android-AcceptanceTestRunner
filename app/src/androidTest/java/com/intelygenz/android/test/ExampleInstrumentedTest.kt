package com.intelygenz.android.test


import com.intelygenz.android.dsl.classpath.AllScenariosExecutedChecker
import com.intelygenz.android.dsl.classpath.runScenario
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AllScenariosExecutedChecker::class)
class ExampleInstrumentedTest {
    @Test
    fun example_someDeterminableBusinessSituation()  {
        runScenario("example", "Some determinable business situation")
    }

}
