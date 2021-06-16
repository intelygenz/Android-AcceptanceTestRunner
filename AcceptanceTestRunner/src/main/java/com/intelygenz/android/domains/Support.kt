package com.intelygenz.android.domains

import java.util.regex.Matcher

internal fun String.removeNonValidUriChars(): String = replace("[^A-Za-z0-9_/.]".toRegex(), "")
internal fun String.removeNonValidChars(): String =  replace("[^A-Za-z0-9_]".toRegex(), "")
internal fun String.startsWithNumber(): Boolean = first().isDigit()
internal fun String.capitalize(): String = replaceFirstChar { it.uppercase() }
internal fun String.decapitalize(): String = replaceFirstChar { it.lowercase() }
internal fun Matcher.findAll(): List<String> {
    val list = mutableListOf<String?>()
    while(find()) { list.add(group(0)) }
    return list.mapNotNull { it }
}