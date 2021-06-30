package com.intelygenz.android.acceptanceplugin

import com.intellij.openapi.vfs.VirtualFile
import com.intelygenz.android.gen.CollisionBehavior
import com.intelygenz.android.gen.generateCodeTo
import com.intelygenz.android.gen.generateTestClass
import com.intelygenz.android.gherkinparser.GherkinParser
import com.intelygenz.android.gherkinparser.Resource
import java.io.InputStream

import kotlin.io.path.Path

object Generator {

    fun generateFeat(feature: VirtualFile, toPath: VirtualFile, packageName: String) {
        val features = parse(listOf(feature))
        features.first().generateCodeTo(packageName, Path(toPath.path), CollisionBehavior.RENAME_FILE)
        toPath.refresh(true, true)
    }

    fun generateTests(features: List<VirtualFile>, toPath: VirtualFile, packageName: String) {
        parse(features).generateTestClass(packageName, Path(toPath.path), CollisionBehavior.OVERRIDE)
        toPath.refresh(true, true)
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