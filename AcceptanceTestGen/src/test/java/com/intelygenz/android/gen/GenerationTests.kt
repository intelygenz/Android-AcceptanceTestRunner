package com.intelygenz.android.gen

import com.intelygenz.android.gherkinparser.GherkinParser
import com.intelygenz.android.gherkinparser.Resource
import org.junit.Test
import java.io.File
import java.io.InputStream

class GenerationTests {

    @Test
    fun testGenerateScenario() {
        val folder = File("./src/test/resources/features")
        val features = GherkinParser(listOf(FileResource(folder))).parserFeatures()
        features.generateTestClass("com.example.", folder)
        features.forEach { it.generateCodeTo("com.example", folder) }
    }

    class FileResource(private val file: File): Resource {
        override val path: String get() = file.absolutePath
        override val isFile: Boolean get() = !file.isDirectory
        override val extension: String get() = file.extension
        override fun list(): List<Resource> = file.listFiles()?.map { FileResource(it) } ?: emptyList()
        override fun open(): InputStream = file.inputStream()
    }
}