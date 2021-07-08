package com.kangdroid.navi_arch

import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

object ServerSetup {
    // Server Download URL
    private val downloadUrl: URL = URL("https://github.com/KangDroid/Navi-Server/releases/download/PVJar/NavIServer-1.0-SNAPSHOT.jar")

    // Server Store Location
    private val downloadTargetFile: File = File(System.getProperty("java.io.tmpdir"), "tmpServer.jar")

    // File MD5
    private const val fileMd5Sum: String = "57137ca341e3820e59c1c4e986dfaef9"

    private var targetProcess: Process? = null

    private fun inspectMd5File(file: File): String {
        val fileBytes: ByteArray = Files.readAllBytes(
            Paths.get(downloadTargetFile.absolutePath)
        )
        val digestedHash: ByteArray = MessageDigest.getInstance("MD5").digest(fileBytes)

        return DatatypeConverter.printHexBinary(digestedHash).toLowerCase()
    }

    // Cycle Starts
    fun setupServer() {
        downloadFile()
        executeFile()
    }

    // Cycle Ends
    fun clearServer(fullCleanup: Boolean) {
        killProcess()
        if (fullCleanup) {
            cleanUp()
        }
    }

    private fun downloadFile() {
        if (!downloadTargetFile.exists()) {
            logDebug("No Cached content for now. Downloading file content...")
            downloadInitialFile()
        } else {
            val tmpMd5Sum: String = inspectMd5File(downloadTargetFile)
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

    private fun executeFile() {
        logDebug("Executing Server!")
        val processBuilder: ProcessBuilder = ProcessBuilder(
            listOf("java", "-jar", downloadTargetFile.absolutePath)
        )
        targetProcess = processBuilder.start()

        logDebug("Waiting for server to run..!")
        runBlocking {
            val serverUrl: URL = URL("http://localhost:8080/")
            while (true) {
                if (serverHeartBeat(serverUrl)) {
                    logDebug("Server is now running :)")
                    break
                } else {
                    logError("Seems like server isn't still running.")
                }
                sleep(1000)
            }
        }
    }

    private fun serverHeartBeat(url: URL): Boolean {
        runCatching {
            url.readText()
        }.onSuccess {
            return it == "SERVER_RUNNING"
        }.onFailure {
            return false
        }

        return true
    }

    private fun killProcess() {
        logDebug("About to shut down server..")
        targetProcess?.destroy()
        runBlocking {
            sleep(5000)
        }

        if (targetProcess?.isAlive == true) {
            logError("Seems like server is still alive! Trying to force-kill.")
            targetProcess?.destroyForcibly()
        } else {
            logDebug("Seems like server shut down!")
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
}