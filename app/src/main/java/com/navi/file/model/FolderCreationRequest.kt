package com.navi.file.model

data class FolderCreationRequest(
    // New Folder name
    var newFolderName: String,

    // Parent Folder Information.
    var parentFolderMetadata: FileMetadata
)
