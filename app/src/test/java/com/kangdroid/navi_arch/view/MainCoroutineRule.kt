package com.kangdroid.navi_arch.view

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher)
{
    // For coroutine test
    // Since unit test can't use Main Looper, coroutine test should pass TestCoroutineDispatcher and execute within the runBlockingTest

    // Swaps the Dispatcher.Main for a TestCoroutineDispatcher.
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    // Reset
    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}