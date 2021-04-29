package com.kangdroid.navi_arch.data

sealed class FileSortingMode : Comparator<FileData> {
    object TypedName : FileSortingMode(),
        Comparator<FileData> by compareBy({ it.fileType }, { it.fileName }, { it.lastModifiedTime })

    object TypedLMT : FileSortingMode(),
        Comparator<FileData> by compareBy({ it.fileType }, { it.lastModifiedTime }, { it.fileName })

    object Name : FileSortingMode(),
        Comparator<FileData> by compareBy({ it.fileName }, { it.lastModifiedTime })

    object LMT : FileSortingMode(),
        Comparator<FileData> by compareBy({ it.lastModifiedTime }, { it.fileName })
}