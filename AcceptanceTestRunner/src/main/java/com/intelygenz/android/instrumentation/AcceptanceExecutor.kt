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
import cucumber.runtime.model.*
import cucumber.runtime.xstream.LocalizedXStreams
import dalvik.system.DexFile
import gherkin.formatter.Formatter
import gherkin.formatter.Reporter
import java.io.IOException


internal class AcceptanceExecutor(arguments: Arguments, private val instrumentation: Instrumentation) {

    private val classLoader: ClassLoader = instrumentation.context.classLoader
    private val classFinder: ClassFinder = createDexClassFinder(instrumentation.context)
    private val runtimeOptions: RuntimeOptions = createRuntimeOptions(instrumentation.context)
    private val resourceLoader: ResourceLoader = AndroidResourceLoader(instrumentation.context)
    private val cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader)
    private val runtimeGlue = AcceptanceRuntimeGlue(LocalizedXStreams(classLoader), cucumberFeatures)
    private val runtime: Runtime = Runtime(resourceLoader, classLoader, createBackends(), runtimeOptions, runtimeGlue)


    init {
        trySetCucumberOptionsToSystemProperties(arguments)
    }

    fun execute() {
        runtimeOptions.addPlugin(AndroidInstrumentationReporter(runtime, instrumentation, numberOfConcreteScenarios))
        runtimeOptions.addPlugin(AndroidLogcatReporter(runtime, TAG))
        // TODO: This is duplicated in info.cucumber.Runtime.
        val reporter = runtimeOptions.reporter(classLoader)
        val formatter = runtimeOptions.formatter(classLoader)
        val stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader)
        val ruleExecutor = GlueRuleExecutor(glue = runtimeGlue)
        runtime.glue.reportStepDefinitions(stepDefinitionReporter)
        cucumberFeatures.map { AcceptanceFeature(ruleExecutor, it) }.forEach {
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

private class AcceptanceFeature(val ruleExecutor: RuleExecutor, val cucumberFeature: CucumberFeature): CucumberFeature(cucumberFeature.gherkinFeature, cucumberFeature.path) {

    override fun getFeatureElements(): MutableList<CucumberTagStatement> {
        return cucumberFeature.featureElements.map {
            when(it) {
                is CucumberScenario -> AcceptanceScenario( it)
                is CucumberScenarioOutline -> AcceptanceScenarioOutline(it)
                else -> throw java.lang.IllegalStateException("not supported")
            }
        }.toMutableList()
    }

    private inner class AcceptanceScenario(val scenario: CucumberScenario) : CucumberScenario(scenario.getCucumberFeature(), scenario.cucumberBackground, scenario.getScenario()) {
        override fun run(formatter: Formatter?, reporter: Reporter?, runtime: Runtime?) {
            ruleExecutor.execute(scenario) { scenario.run(formatter, reporter, runtime) }
        }
    }

    private inner class AcceptanceScenarioOutline(val cucumberOutline: CucumberScenarioOutline): CucumberScenarioOutline(cucumberOutline.getCucumberFeature(), cucumberOutline.getBackground(), cucumberOutline.getScenarioOutline()) {
        override fun run(formatter: Formatter?, reporter: Reporter?, runtime: Runtime?) {
            formatOutlineScenario(formatter)
            cucumberOutline.cucumberExamplesList.forEach { cucumberExamples ->
                cucumberExamples.format(formatter)
                cucumberExamples.createExampleScenarios().map { AcceptanceScenario(it) }.forEach { it.run(formatter, reporter, runtime) }

            }
        }
    }
}



