package com.intelygenz.android.test

import com.intelygenz.android.AcceptanceOptions

private const val CUCUMBER_TAGS_KEY = "tags"

@AcceptanceOptions(
    features = ["features"]
)
@Suppress("unused")
internal class AcceptanceTestCase
