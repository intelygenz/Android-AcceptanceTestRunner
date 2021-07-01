package com.intelygenz.android.acceptanceplugin

import com.intellij.openapi.vfs.VirtualFile
import com.intelygenz.android.gen.CollisionBehavior
import com.intelygenz.android.gen.generation.generateCodeTo
import com.intelygenz.android.gen.generation.generateTestClass
import com.intelygenz.android.gherkinparser.GherkinParser
import com.intelygenz.android.gherkinparser.Resource
import java.io.InputStream

object Generator {

    fun generateFeat(feature: VirtualFile, toPath: VirtualFile, packageName: String) {
        val features = parse(listOf(feature))
        features.first().generateCodeTo(packageName, toPath.toNioPath(), CollisionBehavior.RENAME_FILE)
        toPath.refresh(true, true)
    }

    fun generateTests(features: List<VirtualFile>, templateFile: VirtualFile, toPath: VirtualFile, packageName: String) {
        val template = templateFile.toNioPath().toFile().readText(Charsets.UTF_8)
        parse(features).generateTestClass(template, packageName, toPath.toNioPath(), CollisionBehavior.OVERRIDE)
        toPath.refresh(false, true)
    }

    private fun parse(features: List<VirtualFile>) = GherkinParser(features.map(::VirtualFileResource)).parserFeatures()

}

private class VirtualFileResource(val file: VirtualFile): Resource {
    override val path: String get() = file.path
    override val isFile: Boolean get() = !file.isDirectory
    override val extension: String get() = ".${file.extension ?: ""}"
    override fun list(): List<Resource> = file.children?.map { VirtualFileResource(it) } ?: emptyList()
    override fun open(): InputStream = file.inputStream
}