package com.example.example

import com.intelygenz.android.And
import com.intelygenz.android.Feat
import com.intelygenz.android.Given
import com.intelygenz.android.Then
import com.intelygenz.android.When
import kotlin.Suppress

@Suppress("RedundantVisibilityModifier")
@Feat(feature = "example.feature", scenario = "Some determinable business situation")
public class SomeDeterminableBusinessSituation {
  @Given("some precondition")
  public fun somePrecondition() = Unit

  @And("some other precondition")
  public fun someOtherPrecondition() = Unit

  @When("some action by the actor")
  public fun someActionByTheActor() = Unit

  @And("some other action")
  public fun someOtherAction() = Unit

  @And("yet another action")
  public fun yetAnotherAction() = Unit

  @Then("some testable outcome is achieved")
  public fun someTestableOutcomeIsAchieved() = Unit

  @And("something else we can check happens too")
  public fun somethingElseWeCanCheckHappensToo() = Unit
}
