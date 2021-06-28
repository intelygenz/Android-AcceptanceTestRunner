package com.intelygenz.android.gen

import com.intelygenz.android.gherkinparser.Feature
import com.intelygenz.android.gherkinparser.Scenario
import com.intelygenz.android.gherkinparser.Step
import com.intelygenz.android.gherkinparser.StepTag
import com.squareup.kotlinpoet.*
import java.io.File
import java.lang.IllegalArgumentException


private val TestClassName = ClassName("org.junit", "Test")
private val RunWithClassName = ClassName("org.junit.runner", "RunWith")
private val AllScenariosExecutedCheckerClassName = ClassName("com.intelygenz.android.dsl.classpath", "AllScenariosExecutedChecker")
private val SuppressClassName = ClassName("kotlin", "Suppress")
private val suppressRedundantVisibility = AnnotationSpec.builder(SuppressClassName).addMember("%S", "RedundantVisibilityModifier").build()

private val FeatClassName = ClassName("com.intelygenz.android", "Feat")
private val GivenClassName = ClassName("com.intelygenz.android", "Given")
private val WhenClassName = ClassName("com.intelygenz.android", "When")
private val ThenClassName = ClassName("com.intelygenz.android", "Then")
private val AndClassName = ClassName("com.intelygenz.android", "And")

fun List<Feature>.generateTestClass(packageName: String, folder: File) {
    folder.assertIsFolder()

    fun Scenario.toTestFun() = FunSpec.builder(description.camelCase().decapitalize())
        .addAnnotation(TestClassName)
      //  .returns(Unit::class)
        .addCode("return runScenario(%S, %S)", featurePath.fileName(), description)
        .build()

    FileSpec.builder(packageName, "AllScenariosTest")
        .addImport("com.intelygenz.android.dsl.classpath", "AllScenariosExecutedChecker", "runScenario")
        .addType(TypeSpec.classBuilder("AllScenariosTest")
            .addAnnotation(suppressRedundantVisibility)
            .addAnnotation(AnnotationSpec.builder(RunWithClassName)
                .addMember("%T::class", AllScenariosExecutedCheckerClassName)
                .build())
            .addFunctions(flatMap { it.scenarios }.map { it.toTestFun() })
            .build()
        ).build()
        .writeTo(folder)

}



fun Feature.generateCodeTo(packageName: String, srcFolder: File) {
    srcFolder.assertIsFolder()
    val featureFolderName = path.fileName().replace(".feature", "")
    (scenarios + listOfNotNull(background)).forEach { it.generateCodeTo("$packageName.$featureFolderName", srcFolder) }
}

fun Scenario.generateCodeTo(packageName: String, srcFolder: File) {
    srcFolder.assertIsFolder()

    fun StepTag.className() = when(this) {
        StepTag.GIVEN -> GivenClassName
        StepTag.WHEN -> WhenClassName
        StepTag.THEN -> ThenClassName
        StepTag.AND -> AndClassName
    }

    fun Step.funSpec() = FunSpec.builder(name.camelCase().decapitalize())
        .addAnnotation(AnnotationSpec.builder(tag.className())
            .addMember("%S", name)
            .build())
        .addStatement("return Unit", "")
        .build()

    FileSpec.builder(packageName, description.camelCase())
        .addType(TypeSpec.classBuilder(description.camelCase())
            .addAnnotation(suppressRedundantVisibility)
            .addAnnotation(AnnotationSpec.builder(FeatClassName)
                .addMember("feature = %S, scenario = %S", featurePath.fileName(), description )
                .build())
            .addFunctions(stepDescriptions.map { it.funSpec() })
            .build())
        .build()
        .writeTo(srcFolder)
}

private fun File.assertIsFolder() = if(isFile) throw IllegalArgumentException("Expected $this to be a folder") else Unit

private fun String.fileName(): String = lastIndexOf("/").takeIf { it != -1 }?.let { substring(it + 1) } ?: this
private fun String.clean() = filter { it.isLetterOrDigit() || it == '_' || it == ' ' }.let { if(it.first().isDigit()) "_$it" else it }
private fun String.camelCase(): String = clean().split(" ").joinToString("") { it.capitalize() }
private fun String.capitalize(): String = replaceFirstChar { it.titlecase() }
private fun String.decapitalize(): String = replaceFirstChar { it.lowercase() }
