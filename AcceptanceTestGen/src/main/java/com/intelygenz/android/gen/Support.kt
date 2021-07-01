package com.intelygenz.android.gen

import java.lang.IllegalArgumentException
import java.nio.file.Path

internal fun Path.assertIsFolder() = if(toFile().isFile) throw IllegalArgumentException("Expected $this to be a folder") else Unit

internal fun String.fileName(): String = lastIndexOf("/").takeIf { it != -1 }?.let { substring(it + 1) } ?: this
internal fun String.clean() = filter { it.isLetterOrDigit() || it == '_' || it == ' ' }.let { if(it.first().isDigit()) "_$it" else it }
internal fun String.camelCase(): String = clean().split(" ").joinToString("") { it.capitalize() }
internal fun String.capitalize(): String = replaceFirstChar { it.titlecase() }
internal fun String.decapitalizeFirst(): String = replaceFirstChar { it.lowercase() }