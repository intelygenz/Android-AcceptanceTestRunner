package com.intelygenz.android.domains

private const val FEATURE_PATH = "features"

class Feature(private val fileName: String, private val str: String = "", val scenarios: List<Scenario> = emptyList()) {
    val featureName: String get() = with(normalized) { if (removeNonValidChars().startsWithNumber()) "_${this}" else this }
    val asPackage: String get() = "$FEATURE_PATH.$featureName"
    val normalized : String get() = fileName.lowercase().trim()
            .removeNonValidUriChars()
            .split("/".toRegex())
            .last()
            .replace(".feature", "").trim()

    val unNormalized: String get() = fileName.split("/".toRegex()).last().trim()
}


