package com.intelygenz.android.dsl

import com.intelygenz.android.gherkinparser.Step
import com.intelygenz.android.gherkinparser.StepTag
import org.junit.Assume

internal fun Step.runIf(tag: StepTag, regex: String, action: () -> Unit): Boolean = if(match(tag, regex)) action().let { true } else false
internal fun Step.match(tag: StepTag, regex: String): Boolean = this.tag == tag && Regex(regex).matches(name.trim())

internal fun Boolean.skipOnFalse() = Assume.assumeTrue(this)

internal fun String.fileName(): String = lastIndexOf("/").takeIf { it != -1 }?.let { substring(it + 1) } ?: this

internal fun asFeatureId(feature: String): String = if(feature.endsWith(".feature")) feature else "$feature.feature"
internal fun asScenarioId(scenario: String): String = scenario.trim()