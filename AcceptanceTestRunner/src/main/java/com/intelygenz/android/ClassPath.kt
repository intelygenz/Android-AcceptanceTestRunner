package com.intelygenz.android

import androidx.test.platform.app.InstrumentationRegistry
import dalvik.system.DexFile

internal val classPath: ClassPath by lazy {
    InstrumentationRegistry.getInstrumentation().let {
        ClassPath(it.context.packageCodePath, it.targetContext.packageName)
    }
}

internal data class AnnotatedClass<T>(val annotation: T, val element: Class<*>)

internal class ClassPath(private val packageCodePath: String, private val packageName: String) {
    val testClasses: List<Class<*>> by lazy { DexClassFinder(Dex(packageCodePath)).getDescendants(Any::class.java, packageName).toList() }
    val featClasses: List<AnnotatedClass<Feat>> by lazy { testClasses.withAnnotation() }
    val optionClasses: List<AnnotatedClass<AcceptanceOptions>> by lazy { testClasses.withAnnotation() }

    private inline fun <reified T: Annotation> List<Class<*>>.withAnnotation() = mapNotNull { element -> element.getAnnotation(T::class.java)?.let { AnnotatedClass(it, element) } }
}


private class Dex(path: String) {
    private val dexFile = DexFile(path)
    data class Entry(val packageName: String, val className: String, val fullClassName: String)

    fun entries(): List<Entry> = dexFile.entries().toList().map { entry ->
            entry.lastIndexOf(".").takeIf { it != -1 }
                ?.let { Entry(entry.substring(0, it), entry.substring(it + 1), entry) }
                ?: Entry("", entry, entry)
    }
}


private class DexClassFinder(private val dex: Dex) {

    private val classLoader = DexClassFinder::class.java.classLoader

    fun <T> getDescendants(parentType: Class<T>, packageName: String): Collection<Class<out T>> {
        fun isGenerated(className: String): Boolean = className == "Manifest" || className == "R" || className.startsWith("R$")
        return dex.entries().filter { it.packageName.startsWith(packageName) && !isGenerated(it.className) }.mapNotNull {
           val clazz = Class.forName(it.fullClassName, false, classLoader) as? Class<out T>
           if(clazz != null && clazz != parentType && parentType.isAssignableFrom(clazz)) clazz else null
       }
    }



}