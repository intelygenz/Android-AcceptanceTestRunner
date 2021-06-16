package com.intelygenz.android.instrumentation

import android.app.Instrumentation
import android.content.Context
import android.util.Log
import cucumber.api.CucumberOptions
import cucumber.api.java.ObjectFactory
import cucumber.runtime.*
import cucumber.runtime.android.*
import cucumber.runtime.io.ResourceLoader
import cucumber.runtime.java.JavaBackend
import cucumber.runtime.java.ObjectFactoryLoader
import cucumber.runtime.model.CucumberFeature
import cucumber.runtime.xstream.LocalizedXStreams
import dalvik.system.DexFile
import java.io.IOException
import java.util.*

internal class AcceptanceExecutor(arguments: Arguments, instrumentation: Instrumentation) {
    private val instrumentation: Instrumentation
    private val classLoader: ClassLoader
    private val classFinder: ClassFinder
    private val runtimeOptions: RuntimeOptions
    private val runtime: Runtime
    private val cucumberFeatures: List<CucumberFeature>

    init {
        trySetCucumberOptionsToSystemProperties(arguments)
        val context = instrumentation.context
        this.instrumentation = instrumentation
        classLoader = context.classLoader
        classFinder = createDexClassFinder(context)
        runtimeOptions = createRuntimeOptions(context)
        val resourceLoader: ResourceLoader = AndroidResourceLoader(context)
        cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader)
        val runtimeGlue = AcceptanceRuntimeGlue(LocalizedXStreams(classLoader), cucumberFeatures)
        runtime = Runtime(resourceLoader, classLoader, createBackends(), runtimeOptions, runtimeGlue)
    }

    fun execute() {
        runtimeOptions.addPlugin(AndroidInstrumentationReporter(runtime, instrumentation, numberOfConcreteScenarios))
        runtimeOptions.addPlugin(AndroidLogcatReporter(runtime, TAG))
        // TODO: This is duplicated in info.cucumber.Runtime.
        val reporter = runtimeOptions.reporter(classLoader)
        val formatter = runtimeOptions.formatter(classLoader)
        val stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader)
        runtime.glue.reportStepDefinitions(stepDefinitionReporter)
        cucumberFeatures.forEach {
            it.run(formatter, reporter, runtime)
        }

        formatter.done()
        formatter.close()
    }

    val numberOfConcreteScenarios: Int get() = ScenarioCounter.countScenarios(cucumberFeatures)

    private fun trySetCucumberOptionsToSystemProperties(arguments: Arguments) {
        val cucumberOptions = arguments.cucumberOptions
        if (cucumberOptions.isNotEmpty()) {
            Log.d(TAG, "Setting cucumber.options from arguments: '$cucumberOptions'")
            System.setProperty(CUCUMBER_OPTIONS_SYSTEM_PROPERTY, cucumberOptions)
        }
    }

    private fun createDexClassFinder(context: Context): ClassFinder {
        val apkPath = context.packageCodePath
        return DexClassFinder(newDexFile(apkPath))
    }

    private fun newDexFile(apkPath: String): DexFile {
        return try {
            DexFile(apkPath)
        } catch (e: IOException) {
            throw CucumberException("Failed to open $apkPath", e)
        }
    }

    private fun createRuntimeOptions(context: Context): RuntimeOptions {
        return classFinder.getDescendants(Any::class.java, context.packageName)
            .firstOrNull { it.isAnnotationPresent(CucumberOptions::class.java) }
            ?.let {
                Log.d(TAG, "Found CucumberOptions in class " + it.name)
                RuntimeOptionsFactory(it).create()
            } ?: throw CucumberException("No CucumberOptions annotation")
    }

    private fun createBackends(): Collection<Backend> {
        val delegateObjectFactory = ObjectFactoryLoader.loadObjectFactory(classFinder, Env.INSTANCE[ObjectFactory::class.java.name])
        val objectFactory = AndroidObjectFactory(delegateObjectFactory, instrumentation)
        val backends: MutableList<Backend> = ArrayList()
        backends.add(JavaBackend(objectFactory, classFinder))
        return backends
    }

    companion object {
        const val TAG = "cucumber-android"
        const val CUCUMBER_OPTIONS_SYSTEM_PROPERTY = "cucumber.options"
    }


}