package com.intelygenz.android.acceptanceplugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import java.util.*
import javax.swing.JDialog

internal inline fun VirtualFile.isChildOf(contains: (VirtualFile) -> Boolean): Boolean = firstInHierarchy(contains) != null
internal inline fun VirtualFile.firstInHierarchy(contains: (VirtualFile) -> Boolean): VirtualFile? {
    var element: VirtualFile? = this
    var found = false
    while(!found && element != null) {
        found = contains(element).apply { if(!this) element = element?.parent }
    }
    return if(found) element else null
}

internal inline fun VirtualFile.containsChild(contains: (VirtualFile) -> Boolean): Boolean = filterChildren(contains).isNotEmpty()
internal inline fun VirtualFile.filterChildren(filter: (VirtualFile) -> Boolean): List<VirtualFile> {
    val children = mutableListOf<VirtualFile>()
    val elements = mutableListOf(this)
    while(elements.isNotEmpty()) {
        elements.first().also { elements.remove(it) }.let { element ->
            if(element.isDirectory) {
                elements.addAll(element.children?.toList() ?: emptyList())
            } else {
                if(filter(element)) children.add(element) else Unit
            }
        }
    }
    return children
}


internal fun AnActionEvent.virtualFile() = getData(CommonDataKeys.VIRTUAL_FILE)

internal fun JDialog.present(project: Project?) {
    pack()
    setLocationRelativeTo(WindowManager.getInstance().suggestParentWindow(project))
    isVisible = true
}

internal fun String.lowercase() = toLowerCase(Locale.getDefault())