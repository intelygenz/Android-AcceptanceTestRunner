package com.intelygenz.android.features.example


import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.intelygenz.android.Feat
import com.intelygenz.android.MainActivity
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.junit.Rule


@Feat(feature = "example.feature", scenario = "Some determinable business situation")
class SomeDeterminableBusinessSituation {

    @Given("^some precondition$")
    fun somePrecondition() {}

    @And("^some other precondition$")
    fun someOtherPrecondition() {}

    @When("^some action by the actor$")
    fun someActionByTheActor() {}

    @And("^some other action$")
    fun someOtherAction() {}

    @And("^yet another action$")
    fun yetAnotherAction() {}

    @Then("some testable outcome is achieved")
    fun someTestableOutcomeIsAchieved() {}

    @And("something else we can check happens too")
    fun somethingElseWeCanCheckHappensToo() {}
}