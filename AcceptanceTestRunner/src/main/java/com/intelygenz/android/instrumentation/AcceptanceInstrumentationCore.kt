package com.intelygenz.android.instrumentation

import android.app.Activity
import android.app.Instrumentation
import android.os.Bundle
import android.os.Looper
import cucumber.api.android.CucumberInstrumentationCore
import cucumber.runtime.android.Arguments
import cucumber.runtime.android.CoverageDumper
import cucumber.runtime.android.DebuggerWaiter


class AcceptanceInstrumentationCore(private val instrumentation: Instrumentation) {
    private lateinit var debuggerWaiter: DebuggerWaiter
    private lateinit var coverageDumper: CoverageDumper
    private lateinit var acceptanceExecutor: AcceptanceExecutor
    private lateinit var arguments: Arguments

    fun create(bundle: Bundle?) {
        arguments = Arguments(bundle)
        acceptanceExecutor = AcceptanceExecutor(arguments, instrumentation)
        coverageDumper = CoverageDumper(arguments)
        debuggerWaiter = DebuggerWaiter(arguments)
    }

    fun start() {
        Looper.prepare()
        val results = Bundle()
        if (arguments.isCountEnabled) {
            results.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID)
            results.putInt(REPORT_KEY_NUM_TOTAL, acceptanceExecutor.numberOfConcreteScenarios)
        } else {
            debuggerWaiter.requestWaitForDebugger()
            acceptanceExecutor.execute()
            coverageDumper.requestDump(results)
        }
        instrumentation.finish(Activity.RESULT_OK, results)
    }

    companion object {
        val REPORT_VALUE_ID: String = CucumberInstrumentationCore::class.java.simpleName
        const val REPORT_KEY_NUM_TOTAL = "numtests"
    }
}