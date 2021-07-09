package com.kangdroid.navi_arch

import at.favre.lib.bytes.Bytes
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

object ServerSetup {
    // Server Download URL
    private val downloadUrl: URL by lazy {
        val apiUrl: URL = URL("https://api.github.com/repos/Navi-Cloud/Navi-Server/releases/latest")
        val objectMapper: ObjectMapper = jacksonObjectMapper()
        val treeNode: JsonNode = objectMapper.readTree(apiUrl.readText())
        URL(treeNode.get("assets").get(0).get("browser_download_url").textValue())
    }

    // Server Store Location
    private val downloadTargetFile: File = File(System.getProperty("java.io.tmpdir"), "tmpServer.jar")

    // Server Heartbeat URL
    private val serverUrl: URL = URL("http://localhost:8080/api/remove")

    // File MD5
    private const val fileMd5Sum: String = "382fe3291da7aff442a4f6dedbe7bb0e"

    private var targetProcess: Process? = null

    // Server Jobs
    private var serverJob: Job? = null

    private fun inspectMd5File(): String {
        val fileBytes: ByteArray = Files.readAllBytes(
            Paths.get(downloadTargetFile.absolutePath)
        )
        val digestedHash: ByteArray = MessageDigest.getInstance("MD5").digest(fileBytes)

        return Bytes.wrap(digestedHash).encodeHex()
    }

    // Cycle Starts
    fun setupServer() {
        downloadFile()
        executeFile()
    }

    // Clear Data
    fun clearData() {
        serverUrl.readText()
    }

    // Cycle Ends
    fun clearServer(fullCleanup: Boolean) {
        killProcess()
        if (fullCleanup) {
            cleanUp()
        }
    }

    private fun executeFile() {
        logDebug("Executing Server!")

        // Create Command List
        val processBuilder: ProcessBuilder = ProcessBuilder(
            listOf("java", "-jar", downloadTargetFile.absolutePath)
        )

        // Launch Server
        serverJob = launchServer(processBuilder)

        logDebug("Waiting for server to run..!")
        runBlocking {
            checkServerAlivePoll()
        }
    }

    private fun launchServer(processBuilder: ProcessBuilder): Job {
        return GlobalScope.launch {
            targetProcess = processBuilder.start()
            val bufferedReader: BufferedReader = BufferedReader(InputStreamReader(targetProcess?.inputStream))

            var str: String? = ""
            while (true) {
                str = bufferedReader.readLine()
                if (str == null) break
                println(str)
            }
        }
    }

    private fun killProcess() {
        logDebug("About to shut down server..")
        targetProcess?.destroy()
        var tryCount: Int = 0
        while (true) {
            if (targetProcess?.isAlive == false) {
                logDebug("Seems like server is destroyed!")
                break
            } else {
                logError("Seems like server is still running!")
                sleep(1000)
                tryCount++
                if (tryCount > 5) {
                    logError("Seems like server is NOT DESTROYING...")
                    logError("Look up Task manager for any zombie process.")
                    targetProcess?.destroyForcibly()
                    break
                }
                continue
            }
        }

        if (targetProcess?.isAlive == true) {
            logError("Seems like server is still alive! Trying to force-kill.")
            targetProcess?.destroyForcibly()
        } else {
            logDebug("Seems like server shut down!")
        }

        runBlocking {
            serverJob?.cancelAndJoin()
        }

        if (serverJob?.isCompleted == true) {
            logDebug("Seems like server Coroutine also has been finished!")
            serverJob = null
        } else {
            logError("Seems like there is some leakage of server coroutine..")
        }

        targetProcess = null
    }

    private fun cleanUp() {
        downloadTargetFile.delete()
        targetProcess = null
    }

    private fun logDebug(input: String) {
        println("D/${ServerSetup::class.java.simpleName}: $input")
    }

    private fun logError(input: String) {
        println("E/${ServerSetup::class.java.simpleName}: $input")
    }

    private fun checkServerAlivePoll() {
        while (true) {
            if (serverHeartBeat()) {
                logDebug("Server is now running :)")
                break
            }
            sleep(1000)
        }
    }

    private fun serverHeartBeat(): Boolean {
        runCatching {
            serverUrl.readText()
        }.onSuccess {
            return it == "OK"
        }.onFailure {
            return false
        }

        return true
    }

    private fun downloadFile() {
        if (!downloadTargetFile.exists()) {
            logDebug("No Cached content for now. Downloading file content...")
            downloadInitialFile()
        } else {
            val tmpMd5Sum: String = inspectMd5File()
            if (fileMd5Sum != tmpMd5Sum) {
                logError("File exists, but different MD5!")
                logError("Local: ${tmpMd5Sum}, Server: ${fileMd5Sum}!")
                downloadInitialFile()
            }
        }
    }

    private fun downloadInitialFile() {
        val downloadChannel: ReadableByteChannel = Channels.newChannel(downloadUrl.openStream())
        FileOutputStream(downloadTargetFile).also {
            it.channel.transferFrom(downloadChannel, 0, Long.MAX_VALUE)
            it.close()
        }
        downloadChannel.close()
    }
}