package com.intelygenz.android.gen



import java.io.File
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger

enum class CollisionBehavior {
    OVERRIDE, RENAME_FILE, DO_NOTHING
}

internal fun CollisionBehavior.execute(packageName: String, srcPath: Path, className: String, run: (String) -> Unit) {
    val path = File(srcPath.toFile(), packageName.replace(".", "/"))
    val file = File(path, "$className.kt")
    Logger.getGlobal().log(Level.INFO, "[--x--] running collision checker (${file.exists()})): $this $path $file")
    fun nonCollidedName(): String = path.list()
        ?.filter { it.startsWith(className) }
        ?.mapNotNull {
            it.replace(className, "")
                .replace(".kt", "")
                .let { if(it.trim().isEmpty()) "0" else it }
                .toIntOrNull()
        }
        ?.maxOrNull()?.let { "$className${it + 1}" } ?: className

    if(!file.exists()) run(className) else when(this) {
        CollisionBehavior.OVERRIDE -> run(className)
        CollisionBehavior.RENAME_FILE -> run(nonCollidedName().also {
            Logger.getGlobal().log(Level.INFO, "[--x--] running collision checker: NewName: $it")

        })
        else -> {}
    }
}