package com.kangdroid.navi_arch.data.dto.request

data class CreateFolderRequestDTO (
    var parentFolderToken: String,
    var newFolderName: String
)