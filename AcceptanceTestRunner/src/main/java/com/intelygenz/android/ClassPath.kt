package com.intelygenz.android

import androidx.test.platform.app.InstrumentationRegistry
import cucumber.runtime.android.DexClassFinder
import dalvik.system.DexFile

internal val classPath: ClassPath by lazy {
    InstrumentationRegistry.getInstrumentation().let {
        ClassPath(it.context.packageCodePath, it.targetContext.packageName)
    }
}

internal class ClassPath(private val packageCodePath: String, private val packageName: String) {

    val testClasses: List<Class<*>> by lazy {
        DexClassFinder(DexFile(packageCodePath)).getDescendants(Any::class.java, packageName).toList()
    }
}

internal fun <T> ClassPath.testClassesAnnotated(annotation: Class<T>) : List<Class<*>> = testClasses.filter { it.isAnnotatedWith(annotation) }
internal fun Class<*>.isAnnotatedWith(annotation: Class<*>): Boolean = annotations.any { it.annotationClass.java == annotation }
