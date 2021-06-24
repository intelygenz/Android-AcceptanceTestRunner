package com.intelygenz.android.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.intelygenz.android.dsl.*

import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun example_aasdfas() {
        runFeature("example.feature")
    }

    @Test
    fun missingFeatures() {
        assertMissingFeatures()
    }

}
