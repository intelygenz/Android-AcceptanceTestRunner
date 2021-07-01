package com.intelygenz.android.gen.generation

import com.intelygenz.android.gen.*
import com.intelygenz.android.gen.assertIsFolder
import com.intelygenz.android.gen.camelCase
import com.intelygenz.android.gen.execute
import com.intelygenz.android.gen.decapitalizeFirst
import com.intelygenz.android.gherkinparser.Feature
import com.intelygenz.android.gherkinparser.Scenario
import com.intelygenz.android.gherkinparser.Step
import com.intelygenz.android.gherkinparser.StepTag
import com.squareup.kotlinpoet.*
import java.nio.file.Path



private val SuppressClassName = ClassName("kotlin", "Suppress")
private val suppressRedundantVisibility = AnnotationSpec.builder(SuppressClassName).addMember("%S", "RedundantVisibilityModifier").build()

private val FeatClassName = ClassName("com.intelygenz.android", "Feat")
private val GivenClassName = ClassName("com.intelygenz.android", "Given")
private val WhenClassName = ClassName("com.intelygenz.android", "When")
private val ThenClassName = ClassName("com.intelygenz.android", "Then")
private val AndClassName = ClassName("com.intelygenz.android", "And")




fun Feature.generateCodeTo(packageName: String, path: Path, onCollision: CollisionBehavior) {
    path.assertIsFolder()
    val featureFolderName = this.path.fileName().replace(".feature", "").lowercase()
    (scenarios + listOfNotNull(background)).forEach { it.generateCodeTo("$packageName.$featureFolderName", path, onCollision) }
}

fun Scenario.generateCodeTo(packageName: String, path: Path, onCollision: CollisionBehavior)  {
    onCollision.execute(packageName, path, description.camelCase()) {
        generateCodeTo(it, packageName, path)
    }
}

private fun Scenario.generateCodeTo(className: String, packageName: String, path: Path) {
    path.assertIsFolder()

    fun StepTag.className() = when(this) {
        StepTag.GIVEN -> GivenClassName
        StepTag.WHEN -> WhenClassName
        StepTag.THEN -> ThenClassName
        StepTag.AND -> AndClassName
    }

    fun Step.funSpec() = FunSpec.builder(name.camelCase().decapitalizeFirst())
        .addAnnotation(AnnotationSpec.builder(tag.className())
            .addMember("%S", name)
            .build())
        .addStatement("return Unit", "")
        .build()

    FileSpec.builder(packageName, className)
        .addType(TypeSpec.classBuilder(description.camelCase())
            .addAnnotation(suppressRedundantVisibility)
            .addAnnotation(AnnotationSpec.builder(FeatClassName)
                .addMember("feature = %S, scenario = %S", featurePath.fileName(), description )
                .build())
            .addFunctions(stepDescriptions.map { it.funSpec() })
            .build())
        .build()
        .writeTo(path)
}





