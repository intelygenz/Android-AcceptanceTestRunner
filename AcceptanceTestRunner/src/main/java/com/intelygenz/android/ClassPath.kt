package com.intelygenz.android

import androidx.test.platform.app.InstrumentationRegistry
import cucumber.runtime.android.DexClassFinder
import dalvik.system.DexFile

internal val classPath: ClassPath by lazy {
    InstrumentationRegistry.getInstrumentation().let {
        ClassPath(it.context.packageCodePath, it.targetContext.packageName)
    }
}

internal data class AnnotatedClass<T>(val annotation: T, val element: Class<*>)

internal class ClassPath(private val packageCodePath: String, private val packageName: String) {
    val testClasses: List<Class<*>> by lazy { DexClassFinder(DexFile(packageCodePath)).getDescendants(Any::class.java, packageName).toList() }
    val featClasses: List<AnnotatedClass<Feat>> by lazy { testClasses.withAnnotation() }
    val optionClasses: List<AnnotatedClass<AcceptanceOptions>> by lazy { testClasses.withAnnotation() }

    private inline fun <reified T: Annotation> List<Class<*>>.withAnnotation() = mapNotNull { element -> element.getAnnotation(T::class.java)?.let { AnnotatedClass(it, element) } }
}
