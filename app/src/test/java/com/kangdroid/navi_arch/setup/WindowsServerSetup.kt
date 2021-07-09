package com.kangdroid.navi_arch.setup

import com.kangdroid.navi_arch.setup.common.ServerSetupCommon
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IllegalStateException

class WindowsServerSetup: ServerSetupCommon() {
    // Log Tag
    override val logTag: String = WindowsServerSetup::class.java.simpleName

    // Server Jobs
    private var serverJob: Job? = null

    override fun setupServer() {
        // Download Jar file
        downloadFile()

        // Launch Server
        serverJob = launchServer()

        // Wait
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

        // In windows, we need to finish those jobs as well.
        logDebug("Joining jobs...")
        runBlocking {
            serverJob?.join()
        }
        logDebug("Jobs are joined!")

        // Also, we need to kill out mongo, because it seems like windows does not kill subprocesses.
        killMongo()

        targetProcess = null
        serverJob = null
    }

    // Launch server and return coroutine job.
    private fun launchServer(): Job {
        val processBuilder: ProcessBuilder = ProcessBuilder(
            listOf("java", "-jar", downloadTargetFile.absolutePath)
        )
        return GlobalScope.launch {
            targetProcess = processBuilder.start()
            readOutput(BufferedReader(InputStreamReader(targetProcess?.inputStream))) {
                println(it)
            }
        }
    }

    // Read command output from bufferedReader
    private fun readOutput(bufferedReader: BufferedReader, whatToDo: (String) -> Unit) {
        // Read output
        var str: String? = ""
        while (true) {
            str = bufferedReader.readLine()
            if (str == null) break
            whatToDo(str)
        }
    }

    // Wait server for destroy
    private fun waitForDestroy(): Boolean {
        var tryCount: Int = 0
        while (tryCount < 10) {
            if (targetProcess?.isAlive == false) {
                break
            } else {
                logError("Server seems like still running! Waiting for another 1 seconds..")
                Thread.sleep(1000)
                tryCount++
            }
        }
        return tryCount < 10
    }

    private fun killMongo() {
        val processBuilder: ProcessBuilder = ProcessBuilder(
            listOf("tasklist", "/fi", "\"WINDOWTITLE eq Perfdisk PNP Window\"", "/fo", "csv")
        )
        val mongoSearchProcess: Process? = processBuilder.start()
        val bufferedReader: BufferedReader = BufferedReader(InputStreamReader(mongoSearchProcess?.inputStream))

        // Read output from command
        var finalString: String = ""
        readOutput(bufferedReader) {
            println(it)
            finalString += "${it}\n"
        }

        // Check if output contains 'mongo' keyword
        if (!finalString.contains("mongo")) {
            logError("Seems like windows does not have any mongo instance right now!")
            logError("Output: $finalString")
            return
        }

        // Remove last bit of string
        if (finalString.last() == '\n') {
            finalString = finalString.dropLast(1)
        }

        // Filter FinalString
        val textList: List<String> = finalString.split("\n")

        // Assert if finalString is more then 2
        if (textList.size > 2) {
            logError("Seems like there is more then 1 mongo instance right now, or there might be multiple window of \"WINDOWTITLE eq Perfdisk PNP Window\"!")
            throw IllegalStateException("Seems like there is more then 1 mongo instance right now, or there might be multiple window of \"WINDOWTITLE eq Perfdisk PNP Window\"!")
        }

        // Line containing
        killWithPid(getPidFromCSV(textList[1]))
    }

    private fun killWithPid(pid: String) {
        val processBuilder: ProcessBuilder = ProcessBuilder(
            listOf("taskkill", "/F", "/pid", pid)
        )

        val pidKillerProcess: Process? = processBuilder.start()
        val bufferedReader: BufferedReader = BufferedReader(InputStreamReader(pidKillerProcess?.inputStream))
        readOutput(bufferedReader) {
            println(it)
        }
    }

    private fun getPidFromCSV(csvString: String): String {
        val lineContainer: List<String> = csvString.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
        return filterNumbers(lineContainer[1])
    }

    private fun filterNumbers(targetString: String): String {
        return targetString.filter {
            it != '\"'
        }
    }
}