package com.kangdroid.navi_arch.utils

import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class PagerCacheUtilsTest {
    // Target
    private val pagerCacheUtils: PagerCacheUtils = PagerCacheUtils()

    // Fake adapter
    private val fakeFileAdapter: FileAdapter = FileAdapter(
        onClick = { _, _ -> },
        onLongClick = { true },
        fileList = listOf(
            FileData(
                id = 10,
                fileName = "/tmp/a.txt",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
        ),
        pageNumber = 10,
        currentFolder = FileData(
            id = 1,
            fileName = "/tmp",
            fileType = FileType.File.toString(),
            token = "/tmp.token",
            lastModifiedTime = System.currentTimeMillis()
        )
    )

    // Fake Token
    private val targetToken: String = "non-existence-token"

    // private cache
    private var pageCache: HashMap<String, FileAdapter> = HashMap()
        set(value) {
            field = value
            setCache(value)
        }
        get() {
            return getCache()
        }

    // Get private pageCache
    private fun getCache(): HashMap<String, FileAdapter> {
        val targetField: Field = PagerCacheUtils::class.java.getDeclaredField("pageCache").apply {
            isAccessible = true
        }
        return targetField.get(pagerCacheUtils) as HashMap<String, FileAdapter>
    }

    // Set private pageCache
    private fun setCache(target: HashMap<String, FileAdapter>) {
        PagerCacheUtils::class.java.getDeclaredField("pageCache").apply {
            isAccessible = true
            set(pagerCacheUtils, target)
        }
    }

    @After
    @Before
    fun destroyCache() {
        pageCache = pageCache.apply {
            clear()
        }
    }

    @Test
    fun is_invalidateCache_not_erase_cache_no_corresponding_token() {
        val currentSize: Int = getCache().size

        // Do
        pagerCacheUtils.invalidateCache(targetToken)

        // Should not be changed
        assertThat(pageCache.size).isEqualTo(currentSize)
    }

    @Test
    fun is_invalidateCache_removes_well() {
        // Setup Cache
        pageCache = pageCache.apply {
            put(targetToken, fakeFileAdapter)
        }

        // Before function execute
        val currentSize: Int = pageCache.size

        // Do
        pagerCacheUtils.invalidateCache(targetToken)

        // check
        assertThat(pageCache.size).isEqualTo(currentSize-1)
        assertThat(pageCache.contains(targetToken)).isEqualTo(false)
    }

    @Test
    fun is_createCache_throws_IllegalStateException_token_exists() {
        // Setup cache
        pageCache = pageCache.apply {
            put(targetToken, fakeFileAdapter)
        }

        runCatching {
            pagerCacheUtils.createCache(targetToken, fakeFileAdapter)
        }.onSuccess {
            fail("This should throw illegal state exception since we set up tokens.")
        }.onFailure {
            assertThat(it is IllegalStateException).isEqualTo(true)
            assertThat(pageCache.size).isEqualTo(1)
        }
    }

    @Test
    fun is_createCache_works_well() {
        runCatching {
            pagerCacheUtils.createCache(targetToken, fakeFileAdapter)
        }.onSuccess {
            assertThat(pageCache.size).isEqualTo(1)
            assertThat(pageCache.contains(targetToken)).isEqualTo(true)
        }.onFailure {
            fail("This should be passed since we did not added mock-up data.")
        }
    }

    @Test
    fun is_getCacheContent_throws_IllegalStateException_no_token() {
        runCatching {
            pagerCacheUtils.getCacheContent("null")
        }.onSuccess {
            fail("We provided some non-existence token, but it succeeds?")
        }.onFailure {
            assertThat(it is IllegalStateException).isEqualTo(true)
        }
    }

    @Test
    fun is_getCacheContent_works_well() {
        pageCache = pageCache.apply {
            put(targetToken, fakeFileAdapter)
        }

        runCatching {
            pagerCacheUtils.getCacheContent(targetToken)
        }.onFailure {
            fail("We provided some mock data, but it failed")
        }.onSuccess {
            assertThat(it.pageNumber).isEqualTo(fakeFileAdapter.pageNumber)
        }
    }
}