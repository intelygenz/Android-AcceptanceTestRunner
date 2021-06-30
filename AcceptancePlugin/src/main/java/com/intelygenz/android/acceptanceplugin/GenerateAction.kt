package com.intelygenz.android.acceptanceplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GenerateFeat : AnAction() {

    override fun update(e: AnActionEvent) {
        val isVisible = e.virtualFile()?.name?.lowercase()?.endsWith(".feature") ?: false
        e.presentation.isEnabled = isVisible
    }

    override fun actionPerformed(e: AnActionEvent) {
        val feature = e.virtualFile() ?: return
        e.project?.destinationDialog(e.virtualFile()!!) { srcPath, packageName ->
            Generator.generateFeat(feature, srcPath, packageName)
        }
    }
}

class GenerateTests : AnAction() {

    override fun update(e: AnActionEvent) {
        val isVisible = e.virtualFile()
            ?.takeIf { it.isDirectory }
            ?.takeIf { it.containsChild { it.name.lowercase().endsWith(".feature") } }
            ?.let { true } ?: false
        e.presentation.isEnabled = isVisible
    }

    override fun actionPerformed(e: AnActionEvent) {
        val features = e.virtualFile()?.filterChildren { it.name.lowercase().endsWith(".feature") } ?: return
        e.project?.destinationDialog(e.virtualFile()!!) { srcPath, packageName ->
            Generator.generateTests(features, srcPath, packageName)
        }
    }
}

private fun Project.destinationDialog(virtualFile: VirtualFile, onSelected: (VirtualFile, String) -> Unit) {
    FileChooserFactory.getInstance()
        .createPathChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), this, null)
        .choose(virtualFile) {
            it.first().run {
                val srcParent = firstInHierarchy { it.name == "java" || it.name == "kotlin" }?.takeIf { it != this }
                val packageName = srcParent?.let { path.removePrefix(it.path).replace("/", ".") }
                if (packageName != null) onSelected(srcParent, packageName) else packageDialog(this, onSelected)
            }
        }
}

private fun Project.packageDialog(virtualFile: VirtualFile, onSelected: (VirtualFile, String) -> Unit) {
    GenerateCodeDialog("Package of generated files", "com.") {
        onSelected(virtualFile, it)
    }.present(this)
}
