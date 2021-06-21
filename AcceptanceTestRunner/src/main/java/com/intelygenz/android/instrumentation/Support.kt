package com.intelygenz.android.instrumentation

import cucumber.runtime.model.*
import gherkin.formatter.model.Scenario
import gherkin.formatter.model.ScenarioOutline

internal fun CucumberTagStatement.getScenario() = gherkinModel as Scenario
internal fun CucumberTagStatement.getScenarioOutline() = gherkinModel as ScenarioOutline
internal fun CucumberScenarioOutline.getBackground(): CucumberBackground? = getField("cucumberBackground")
internal fun StepContainer.getCucumberFeature(): CucumberFeature = getField("cucumberFeature")!!