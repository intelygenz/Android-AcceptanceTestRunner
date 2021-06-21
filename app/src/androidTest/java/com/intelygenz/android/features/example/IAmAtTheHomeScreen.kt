package com.intelygenz.android.features.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.intelygenz.android.Feat
import com.intelygenz.android.MainActivity
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import org.junit.Rule


@Feat(feature = "example.feature", scenario = "I am at the home screen")
class IAmAtTheHomeScreen {

    @Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Given("^I logged in the app$")
    fun givenILoggedInTheApp() {
    }

    @And("^I am at the \"home screen\"$")
    fun andIAmAtTheHomeScreen() = Unit


}