package com.intelygenz.android.parser

import android.content.Context
import android.content.res.AssetManager
import java.io.InputStream
import java.lang.IllegalStateException

internal enum class StepTag { GIVEN, WHEN, THEN, AND }
internal data class Feature(val path: String, val annotations: List<String>, val description: String, val scenarios: List<Scenario>, val background: Scenario?)
internal data class Scenario(val annotations: List<String>, val description: String, val stepDescriptions: List<Step>, val index: Int, val isBackground: Boolean)
internal data class Step(val tag: StepTag, val name: String)

internal class GherkinParser(private val featurePaths: List<String>) {
    private val context: Context get() = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().context
    private val assets: AssetManager get() = context.assets

    fun parserFeatures(): List<Feature> = assets.recursiveFind(featurePaths, ".feature")
        .mapNotNull { path -> parseFeature(assets.open(path))?.toFeature(path) }.toList()

    private fun AssetManager.recursiveFind(paths: List<String>, extension: String): Sequence<String> {
        fun listPath(path: String) = list(path)?.map { "$path/$it" }?.let { recursiveFind(it, extension) } ?: emptySequence()
        return paths.asSequence().flatMap { path -> if(path.endsWith(extension)) sequenceOf(path) else listPath(path) }
    }

    private fun parseFeature(inputStream: InputStream): InnerFeature? {
        return inputStream.bufferedReader(Charsets.UTF_8).readLines().map { it.trim() }
            .filter { !it.startsWith("#") && it.count() > 0 }
            .takeIf { it.isNotEmpty() }
            ?.let { FileMachineState().build(it) }
    }
}

private fun InnerFeature.toFeature(path: String) = Feature(path, annotations, description, scenarios.map { it.toScenario() }, background?.toScenario(true))
private fun InnerScenario.toScenario(isBackground: Boolean = false) = Scenario(annotations, description, stepDescriptions.map{ it.toStep() }, index, isBackground)
private fun InnerStep.toStep() = Step(StepTag.values().first { it.name.lowercase() == tag.lowercase() }, name)


private fun FileMachineState.build(lines: List<String>) : InnerFeature {
    lines.forEachIndexed { index, line ->
        val components = line.byComponents()
        when(components?.first) {
            FileTag.FEATURE -> hitFeature(components.second)
            FileTag.BACKGROUND -> hitScenario(true, components.second)
            FileTag.SCENARIO, FileTag.OUTLINE -> hitScenario(false, components.second)
            FileTag.EXAMPLES -> hitExamples()
            FileTag.EXAMPLE_LINE -> hitExampleLine(components.second)
            FileTag.GIVEN, FileTag.WHEN, FileTag.THEN, FileTag.AND -> hitStep(components.first.tag, components.second)
            FileTag.ANNOTATION -> hitAnnotation(components.second)
        }
    }
    return build()
}

private fun String.byComponents(): Pair<FileTag, String>? {
    val prefix = FileTag.values().map{ it.tag }.firstOrNull { this@byComponents.startsWith(it) } ?: return null
    return prefix.let { FileTag.values().first { it.tag == prefix }} to removePrefix(prefix)
}


private enum class FileTag(val tag: String) {
    FEATURE("Feature:"), BACKGROUND("Background:"), SCENARIO("Scenario:"), OUTLINE("Scenario Outline:"),
    EXAMPLES("Examples:"), EXAMPLE_LINE("|"), GIVEN("Given"), WHEN("When"), THEN("Then"), AND("And"), ANNOTATION("@")
}



data class InnerFeature(val annotations: List<String>, val description: String, val scenarios: List<InnerScenario>, val background: InnerScenario?)
data class InnerScenario(val annotations: List<String>, val description: String, val stepDescriptions: List<InnerStep>, val index: Int)
data class InnerStep(val tag: String, val name: String)


private class FileMachineState {
    private var feature: FeatureMachineState? = null
    private val annotations = mutableListOf<String>()

    fun hitAnnotation(annotation: String) {
        feature?.hitAnnotation(annotation) ?: annotations.add(annotation)
    }

    fun hitFeature(description: String) {
        if(feature != null) { throw IllegalStateException("Duplicated Feature in File $description")
        }
        this.feature = FeatureMachineState(description)
    }

    fun hitScenario(isBackground: Boolean = false, description: String) = feature?.hitScenario(isBackground, description)
    fun hitStep(tag: String, description: String) = feature?.hitStep(tag, description)
    fun hitExamples() = feature?.hitExamples()
    fun hitExampleLine(example: String) = feature?.hitExampleLine(example)

    fun build(): InnerFeature {
        val featureMachine = feature ?: throw IllegalStateException("No Features found")
        return featureMachine.build(annotations).also {
            this.feature = null
            this.annotations.clear()
        }
    }
}

private class FeatureMachineState(private val description: String) {
    private var examplesState: ExamplesMachineState? = null

    private var index: Int = 0
    private val scenarios = mutableListOf<InnerScenario>()
    private var background: InnerScenario? = null

    private var annotations = mutableListOf<String>()
    private var nextScenarioAnnotations = mutableListOf<String>()
    private var isBackground = false
    private var scenarioDescription: String? = null
    private var steps: MutableList<InnerStep>? = null

    fun hitAnnotation(annotation: String) {
        nextScenarioAnnotations.add(annotation)
    }

    fun hitScenario(isBackground: Boolean, description: String) {
        hitEndPreviousBlock()
        if(isBackground && this.background != null) { throw IllegalStateException("Duplicated background on ${this.description}") }
        this.isBackground = isBackground
        this.scenarioDescription = description.trim()
        this.steps = mutableListOf()
    }

    fun hitStep(tag: String, description: String) {
        this.steps?.add(InnerStep(tag, description))
    }

    fun hitExamples() {
        this.examplesState = ExamplesMachineState()
    }

    fun hitExampleLine(example: String) = examplesState?.hitExampleLine(example)

    private fun hitEndPreviousBlock() {
        val scenarioAnnotations = annotations
        this.annotations = this.nextScenarioAnnotations
        this.nextScenarioAnnotations = mutableListOf()

        val desc = this.scenarioDescription
        val steps = this.steps
        if(desc != null && steps != null) {
            val newScenario = InnerScenario(scenarioAnnotations, desc, steps, index)
            if(isBackground) {
                this.background = newScenario
            } else {
                val scenarios = examplesState?.build(newScenario, index) ?: listOf(newScenario)
                this.scenarios.addAll(scenarios)
                this.index = this.index + scenarios.size
            }
        }
        this.scenarioDescription = null
        this.steps = null
    }

    fun build(annotations: List<String>): InnerFeature {
        hitEndPreviousBlock()
        return InnerFeature(annotations, description, scenarios, background)
    }
}

private class ExamplesMachineState  {
    private val examples = mutableListOf<String>()
    fun hitExampleLine(line: String) { examples.add(line) }
    fun build(scenario: InnerScenario, index: Int): List<InnerScenario> {
        if(examples.size < 2) return listOf(scenario)
        val exampleLines = this.examples.also { this.examples.clear() }
        val headers = exampleLines.removeFirst().split("|").map { it.trim() }
        val examples = exampleLines.map { headers.zip(it.split("|")).toMap() }
        return examples.mapIndexed { exampleIndex, map ->
            val description = "${scenario.description}Example${index + 1}"
            val steps = scenario.stepDescriptions.map {
                val name = map.keys.fold(it.name) { acc, key -> acc.replace("<$key>", map[key] ?: "") }
                InnerStep(it.tag, name)
            }
            InnerScenario(scenario.annotations, description, steps, index + exampleIndex)
        }
    }

}
