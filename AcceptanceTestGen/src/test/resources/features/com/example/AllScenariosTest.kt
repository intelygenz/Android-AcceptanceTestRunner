package com.example

import com.intelygenz.android.dsl.classpath.AllScenariosExecutedChecker
import com.intelygenz.android.dsl.classpath.runScenario
import kotlin.Suppress
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("RedundantVisibilityModifier")
@RunWith(AllScenariosExecutedChecker::class)
public class AllScenariosTest {
  @Test
  public fun someDeterminableBusinessSituation() = runScenario("example.feature",
      "Some determinable business situation")
}
