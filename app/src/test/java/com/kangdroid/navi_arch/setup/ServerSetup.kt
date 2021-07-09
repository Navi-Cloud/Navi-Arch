package com.kangdroid.navi_arch.setup

import com.kangdroid.navi_arch.setup.common.ServerSetupCommon

class ServerSetup(private val serverImplementation: ServerSetupCommon) {
    // Lifecycle one. Start Server
    fun setupServer() = serverImplementation.setupServer()

    // Lifecycle two. Clear Server data
    fun clearData() = serverImplementation.clearData()

    // Lifecycle three. Kill server
    fun killServer(fullCleanup: Boolean) = serverImplementation.killServer(fullCleanup)
}