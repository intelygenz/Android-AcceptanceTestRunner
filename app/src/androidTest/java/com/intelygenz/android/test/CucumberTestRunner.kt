package com.intelygenz.android.test


import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.intelygenz.android.instrumentation.AcceptanceInstrumentationCore
import cucumber.api.CucumberOptions

private const val CUCUMBER_TAGS_KEY = "tags"

@CucumberOptions(
    features = ["features"],
    glue = ["com.intelygenz.android.features"],
    format = [
        "pretty", "html:/data/data/com.intelygenz.android/cucumber-reports/cucumber-html-report",
        "json:/data/data/com.intelygenz.android/cucumber-reports/cucumber.json",
        "junit:/data/data/com.intelygenz.android/cucumber-reports/cucumber.xml"
    ]
)
@Suppress("unused")
internal class CucumberTestCase

@Suppress("unused")
internal class CucumberTestRunner : AndroidJUnitRunner() {

    private val instrumentationCore = AcceptanceInstrumentationCore(this)

    override fun onCreate(bundle: Bundle) = super.onCreate(bundle).also {
        instrumentationCore.create(bundle)
    }

    override fun onStart() {
        waitForIdleSync()
        instrumentationCore.start()
    }


}