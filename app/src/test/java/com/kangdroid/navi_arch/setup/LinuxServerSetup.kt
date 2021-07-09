package com.kangdroid.navi_arch.setup

import com.kangdroid.navi_arch.setup.common.ServerSetupCommon
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep

class LinuxServerSetup: ServerSetupCommon() {
    override val logTag: String = LinuxServerSetup::class.java.simpleName

    override fun setupServer() {
        // Download
        downloadFile()

        // Launch
        targetProcess = launchServer()

        // Check for server alive
        runBlocking {
            checkServerAlivePoll()
        }
    }

    override fun clearData() {
        serverUrl.readText()
    }

    override fun killServer(fullCleanup: Boolean) {
        logDebug("About to shut down server..")

        // Destroy Server
        targetProcess?.destroy()

        // Wait for destroy
        if (waitForDestroy()) {
            logDebug("Server is destroyed!")
        } else {
            logError("After 10 attempts, it seems like server did not destroyed.")
            targetProcess?.destroyForcibly()
        }

        targetProcess = null
    }

    private fun launchServer(): Process {
        // Launch
        val processBuilder: ProcessBuilder = ProcessBuilder(
            listOf("java", "-jar", downloadTargetFile.absolutePath)
        )
        return processBuilder.start()
    }

    private fun waitForDestroy(): Boolean {
        var tryCount: Int = 0
        while (tryCount < 10) {
            if (targetProcess?.isAlive == false) {
                break
            } else {
                logError("Server seems like still running! Waiting for another 1 seconds..")
                sleep(1000)
                tryCount++
            }
        }

        return tryCount < 10
    }
}