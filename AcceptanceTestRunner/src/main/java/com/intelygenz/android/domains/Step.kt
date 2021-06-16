package com.intelygenz.android.domains

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

private val STEP_PATTERN = Pattern.compile("((^Given )|(^When )|(^Then )|(^And ))")
private val JAVA_KEYWORDS: Set<String> = HashSet(
    Arrays.asList(
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "false", "final", "finally",
        "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long",
        "native", "new", "null", "package", "private",
        "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "true", "try",
        "void", "volatile", "while"
    )
)

class Step(private val token: String, private val str: String) {

    enum class StepType {
        GIVEN, WHEN, THEN, AND
    }

    val stepType: StepType by lazy {
            token.lowercase().trim().let {
                when {
                    it.contains("given") -> StepType.GIVEN
                    it.contains("when") -> StepType.WHEN
                    it.contains("then") -> StepType.THEN
                    it.contains("and") -> StepType.AND
                    else -> throw IllegalStateException("token not defined not a Given, When, then or And")
                }
            }
    }

    val stepLine: String by lazy {
           STEP_PATTERN.matcher(str.trim()).let { if(it.find()) it.replaceFirst("") else str }.trim()
        }

    val asMethodName: String by lazy {
        normalize.removeNonValidChars().split(" ").joinToString { it.capitalize() }.decapitalize()
            .let { if(it.startsWithNumber() || JAVA_KEYWORDS.contains(it)) "_$it" else it }
    }

    val parameters: List<String> by lazy {
        Pattern.compile(Pattern.quote("<") + "(.*?)" + Pattern.quote(">")).matcher(str).findAll()
    }

    val normalize: String by lazy {
        stepLine.replace("<(.*?)>".toRegex(), "").replace("\"".toRegex(), Matcher.quoteReplacement("\\\"")).trim()
    }
    val unNormalized: String get() = str


}