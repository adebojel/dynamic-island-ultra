package com.adebojel.dynamicislandultra.floating

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileManagerOperationsTest {
    @Test fun sanitizesZipEntryNames() {
        assertEquals("folder_name.txt", FileManagerOperations.safeZipEntryName("folder/name.txt"))
        assertEquals("folder_name.txt", FileManagerOperations.safeZipEntryName("folder\\name.txt"))
        assertEquals("item", FileManagerOperations.safeZipEntryName("\u0000"))
        assertTrue(FileManagerOperations.safeZipEntryName("a".repeat(300)).length <= 180)
    }

    @Test fun zipBoundsAreExplicit() {
        assertEquals(5_000, FileManagerOperations.MAX_ZIP_ENTRIES)
        assertEquals(32, FileManagerOperations.MAX_ZIP_DEPTH)
        assertEquals(5_000, FileManagerOperations.MAX_COPY_ENTRIES)
        assertEquals(32, FileManagerOperations.MAX_COPY_DEPTH)
        assertTrue(FileManagerOperations.MAX_EXTRACTED_BYTES > 0)
        assertTrue(FileManagerOperations.MAX_EXTRACTED_FILE_BYTES <= FileManagerOperations.MAX_EXTRACTED_BYTES)
    }

    @Test fun rejectsZipSlipAndAbsolutePaths() {
        assertEquals(null, FileManagerOperations.normalizeZipEntryPath("../outside.txt"))
        assertEquals(null, FileManagerOperations.normalizeZipEntryPath("/absolute.txt"))
        assertEquals(null, FileManagerOperations.normalizeZipEntryPath("C:/outside.txt"))
        assertEquals(null, FileManagerOperations.normalizeZipEntryPath("folder/../../outside.txt"))
        assertEquals(listOf("folder", "safe.txt"), FileManagerOperations.normalizeZipEntryPath("folder/safe.txt"))
    }
}
