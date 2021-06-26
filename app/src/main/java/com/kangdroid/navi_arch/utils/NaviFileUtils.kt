package com.kangdroid.navi_arch.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log

object NaviFileUtils {
    private val NAVI_FU_TAG: String = "NaviFileUtils"
    private val PRIMARY_STORAGE: String = "primary"
    val ERROR_GETTING_FILENAME: String = "ERROR"
    fun getPathFromUri(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            when (uri.authority) {
                "com.android.externalstorage.documents" -> {
                    val documentId: String = DocumentsContract.getDocumentId(uri)
                    val documentIdSplited: List<String> =
                        documentId.split(":") // left is storage identifier, right is path
                    if (documentIdSplited[0].toLowerCase() == PRIMARY_STORAGE) {
                        return "${Environment.getExternalStorageDirectory()}/${documentIdSplited[1]}"
                    }
                }

                "com.android.providers.downloads.documents" -> {
                    val documentId: String = DocumentsContract.getDocumentId(uri)

                    // This is RAW Path
                    if (documentId.startsWith("raw:")) {
                        return documentId.replaceFirst("raw:", "")
                    }

                    // OR else?
                    val documentLongId: Long = try {
                        java.lang.Long.valueOf(documentId)
                    } catch (e: NumberFormatException) {
                        Log.e(NAVI_FU_TAG, e.stackTraceToString())
                        return ERROR_GETTING_FILENAME
                    }
                    val contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        documentLongId,
                    )
                    return accessContentProviderDb(context, contentUri, null, null)
                        ?: ERROR_GETTING_FILENAME
                }

                "com.android.providers.media.documents" -> {

                    val documentId: String = DocumentsContract.getDocumentId(uri)
                    val documentIdSplited: List<String> = documentId.split(":")
                    val type = documentIdSplited[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection : String = "_id=?"
                    val selectionArgs : Array<String> = arrayOf(documentIdSplited[1])
                    return accessContentProviderDb(context, contentUri!!, selection, selectionArgs)

                }
            }
        }
        return ""
    }

    fun accessContentProviderDb(
        context: Context,
        queryUri: Uri,
        selectionQuery: String?,
        querySelectionArgs: Array<String>?
    ): String? {
        // The query column:[android.providers.Downloads.impl._DATA] --> Owner Can Read + Containing file name
        // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/provider/Downloads.java
        // "Absolute filesystem path to the media item on disk."
        val queryFindColumn: String = "_data"
        val projectionList: Array<String> = arrayOf(queryFindColumn)
        var queryCursor: Cursor = context.contentResolver.query(
            queryUri, projectionList, selectionQuery, querySelectionArgs, null
        ) ?: run {
            Log.e(NAVI_FU_TAG, "Query Result[Cursor] is responded with null!")
            return null
        }

        // Set table cursor to first[Since URI Query - exact search result should be first though]
        return if (!queryCursor.moveToFirst()) {
            Log.e(NAVI_FU_TAG, "Cannot move table cursor to first row!")
            null
        } else {
            val tableIndex: Int = queryCursor.getColumnIndex(queryFindColumn)
            queryCursor.getString(tableIndex)
        }
    }
}