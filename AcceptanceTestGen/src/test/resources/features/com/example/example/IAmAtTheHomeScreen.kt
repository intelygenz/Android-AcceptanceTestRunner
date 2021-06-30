package com.example.example

import com.intelygenz.android.And
import com.intelygenz.android.Feat
import com.intelygenz.android.Given
import kotlin.Suppress

@Suppress("RedundantVisibilityModifier")
@Feat(feature = "example.feature", scenario = "I am at the home screen")
public class IAmAtTheHomeScreen {
  @Given("I logged in the app")
  public fun iLoggedInTheApp() = Unit

  @And("I am at the \"home screen\"")
  public fun iAmAtTheHomeScreen() = Unit
}
