package com.intelygenz.android.domains

import java.util.regex.Pattern

private val TITLE_PATTERN = Pattern.compile("([a-z]|[A-Z])+.*:")
private const val SCENARIO_OUTLINE = "scenario outline:"
private const val SCENARIO = "scenario:"
private const val BACKGROUND = "background:"

class Scenario(private val str: String, val steps: List<Step> = emptyList()) {

    val normalized: String by lazy {
        str.lowercase().trim().removeNonValidChars()
            .let { if(it.startsWith("scenario")) it.replaceFirst("scenario".toRegex(), "") else it }
            .trim()
    }

    val asClassName: String by lazy {
       normalized.split(" ")
           .joinToString { it.capitalize() }
           .let { if(it.startsWithNumber()) "_$it" else it }
    }

    val unNormalized: String by lazy {
        str.lowercase().trim().let {
            when {
                it.startsWith(SCENARIO_OUTLINE) -> str.trim().substring(SCENARIO_OUTLINE.length).trim()
                it.startsWith(BACKGROUND) -> str.trim().substring(BACKGROUND.length).trim()
                it.startsWith(SCENARIO) -> str.trim().substring(SCENARIO.length).trim()
                else -> str.trim()
            }
        }
    }

}
