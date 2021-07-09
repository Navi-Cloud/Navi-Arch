package com.kangdroid.navi_arch.setup.common

import at.favre.lib.bytes.Bytes
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

abstract class ServerDownloadManager: ServerCommonFields() {
    fun downloadFile() {
        if (!downloadTargetFile.exists()) {
            logDebug("No Cached content for now. Downloading file content...")
            downloadInitialFile()
        } else {
            val tmpMd5Sum: String = inspectMd5File()
            if (fileMd5Sum != tmpMd5Sum) {
                logError("File exists, but different MD5!")
                logError("Local: ${tmpMd5Sum}, Server: $fileMd5Sum!")
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

    private fun inspectMd5File(): String {
        val fileBytes: ByteArray = Files.readAllBytes(
            Paths.get(downloadTargetFile.absolutePath)
        )
        val digestedHash: ByteArray = MessageDigest.getInstance("MD5").digest(fileBytes)

        return Bytes.wrap(digestedHash).encodeHex()
    }
}