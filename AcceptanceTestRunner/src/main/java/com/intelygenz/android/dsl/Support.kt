package com.intelygenz.android.dsl

import com.intelygenz.android.parser.Step
import com.intelygenz.android.parser.StepTag

internal fun Step.runIf(tag: StepTag, regex: String, action: () -> Unit): Boolean = if(match(tag, regex)) action().let { true } else false
internal fun Step.match(tag: StepTag, regex: String): Boolean = this.tag == tag && Regex(regex).matches(name.trim())