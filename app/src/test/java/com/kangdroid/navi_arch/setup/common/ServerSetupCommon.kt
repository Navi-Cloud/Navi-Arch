package com.kangdroid.navi_arch.setup.common

abstract class ServerSetupCommon: ServerDownloadManager() {
    protected fun checkServerAlivePoll() {
        while (true) {
            if (serverHeartBeat()) {
                logDebug("Server is now running :)")
                break
            }
            Thread.sleep(1000)
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

    // Lifecycle one. Start Server
    abstract fun setupServer()

    // Lifecycle two. Clear Server data
    abstract fun clearData()

    // Lifecycle three. Kill server
    abstract fun killServer(fullCleanup: Boolean)
}