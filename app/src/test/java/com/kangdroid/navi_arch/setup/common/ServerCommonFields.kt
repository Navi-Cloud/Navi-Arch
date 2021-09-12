package com.kangdroid.navi_arch.setup.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.net.URL

abstract class ServerCommonFields {
    // Server Download dURL
    protected val downloadUrl: URL by lazy {
        val apiUrl: URL = URL("https://api.github.com/repos/Navi-Cloud/Navi-Server/releases/latest")
        val objectMapper: ObjectMapper = jacksonObjectMapper()
        val treeNode: JsonNode = objectMapper.readTree(apiUrl.readText())
        URL(treeNode.get("assets").get(0).get("browser_download_url").textValue())
    }

    // Server Store Location
    protected val downloadTargetFile: File = File(System.getProperty("java.io.tmpdir"), "tmpServer.jar")

    // Server Heartbeat / Remove URL
    protected val serverUrl: URL = URL("http://localhost:8080/api/remove")

    // File MD5
    protected val fileMd5Sum: String = "899692b7e401b934609a455266ab6b2b"

    // Target Process
    protected var targetProcess: Process? = null

    // Log Tag
    abstract val logTag: String

    // Logging Function - Debug
    protected fun logDebug(input: String) {
        println("D/${logTag}: $input")
    }

    // Logging Function - Error
    protected fun logError(input: String) {
        println("E/${logTag}: $input")
    }
}