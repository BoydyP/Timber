package com.android.timberworkoutlogs

import android.app.Application
import android.content.Context
import com.kaspersky.kaspresso.runner.KaspressoRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * A custom test runner that combines the functionality of Kaspresso and Hilt.
 *
 * This runner inherits from KaspressoRunner to gain its advanced testing features
 * and overrides `newApplication` to use Hilt's `HiltTestApplication`. This ensures
 * that Hilt's dependency injection works correctly within Kaspresso tests.
 */
@Suppress("unused")
class HiltKaspressoRunner : KaspressoRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
