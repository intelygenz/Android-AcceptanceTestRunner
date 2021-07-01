package com.intelygenz.android.gen.generation

import com.intelygenz.android.gen.*
import com.intelygenz.android.gherkinparser.Feature
import com.intelygenz.android.gherkinparser.Scenario
import java.io.File
import java.nio.file.Path

const val BODY = "{BODY}"

fun List<Feature>.generateTestClass(template: String, packageName: String, path: Path, onCollision: CollisionBehavior) = generateTestClass("AllScenariosTest", template, packageName, path, onCollision)
fun List<Feature>.generateTestClass(fileName: String, template: String, packageName: String, path: Path, onCollision: CollisionBehavior) {
    path.assertIsFolder()
    onCollision.execute(packageName, path, fileName) { generateTestClass(it, template, packageName, path) }
}

private fun List<Feature>.generateTestClass(fileName: String, template: String, packageName: String, path: Path) {
    fun Scenario.testFun() = testFunTemplate(featureName(), description.camelCase().decapitalizeFirst(), featureFileName(), description)
    val filePath = File(path.toFile(), packageName.replace(".", "/"))
    val file = File(filePath, "$fileName.kt")
    val body = flatMap { it.scenarios }.joinToString("\n\n") { "    ${it.testFun()}" }
    file.writeText("package $packageName\n\n${template.replaceFirst(BODY, body)}")
}

private fun testFunTemplate(featureMethodName: String, scenarioMethodName: String, feature: String, scenario: String) = "@Test fun ${featureMethodName}_$scenarioMethodName() = runScenario(\"$feature\", \"${scenario.escaped()}\")"

private fun Scenario.featureFileName(): String = featurePath.fileName()
private fun Scenario.featureName(): String = featurePath.fileName().removeSuffix(".feature")

private fun String.escaped(): String = map {
    when(it) {
        '\'' -> "'"
        '\"' -> "\\\""
        '$' -> "\$"
        '\b' -> "\\b"
        '\t' -> "\\t"
        '\n' -> "\\n"
        '\r' -> "\\r"
        '\\' -> "\\\\"
        else -> it
    }
}.joinToString("")
