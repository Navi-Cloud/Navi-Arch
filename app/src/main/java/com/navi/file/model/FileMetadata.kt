package com.navi.file.model

data class FileMetadata(
    // A File Owner
    var fileOwnerEmail: String,

    // Virtual Directory - Full Path, including file name
    var virtualDirectory: String,

    // Virtual Parent Directory - Parent Directory
    var virtualParentDirectory: String,

    // Check whether this is folder or not.
    var isFolder: Boolean
)
