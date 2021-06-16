package com.intelygenz.android.instrumentation

import java.lang.RuntimeException

class NotMatchedStepException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)