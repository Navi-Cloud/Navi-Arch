package com.kangdroid.navi_arch.utils

import android.util.Log
import com.kangdroid.navi_arch.adapter.FileAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PagerCacheUtils @Inject constructor() {
    // Cache Related - Only remove cache when upload method is defined.
    private val pageCache: HashMap<String, FileAdapter> = HashMap()

    fun invalidateCache(targetToken: String) {
        if (pageCache.contains(targetToken)) {
            pageCache.remove(targetToken)
        } else {
            // Since removing cache does not effect data consistency so just put warning log.
            Log.w(this::class.java.simpleName, "Page cache with entry $targetToken does not exists!")
        }
    }

    fun createCache(targetToken: String, adapter: FileAdapter) {
        if (pageCache.contains(targetToken)) {
            throw IllegalStateException("Page cache with entry $targetToken is already exists. Please remove cache using invalidateCache function first.")
        }
        pageCache[targetToken] = adapter
    }

    fun getCacheContent(targetTokenEntry: String): FileAdapter {
        if (pageCache[targetTokenEntry] == null) {
            throw IllegalStateException("Page cache with entry $targetTokenEntry does not exists!")
        }

        return pageCache[targetTokenEntry]!!
    }
}