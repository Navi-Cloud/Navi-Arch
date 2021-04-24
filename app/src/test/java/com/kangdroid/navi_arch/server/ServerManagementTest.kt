package com.kangdroid.navi_arch.server

import com.kangdroid.navi_arch.data.FileData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ServerManagementTest {

    @Before
    fun init() {
        ServerManagement.initServerCommunication()
    }

    // Init Test
    @Test
    fun is_initServerCommunication_works_well() {
        assertThat(ServerManagement.initServerCommunication()).isEqualTo(true)
    }

    // Root Token Test
    @Test
    fun is_getRootToken_works_well() {
        assertThat(ServerManagement.getRootToken()).isNotEqualTo("")
    }

    // Get Inside Files
    @Test
    fun is_getInsideFiles_works_well() {
        val rootToken: String = ServerManagement.getRootToken() // Let
        val result: List<FileData> = ServerManagement.getInsideFiles(rootToken)

        assertThat(result.size).isNotEqualTo(0)
    }
}