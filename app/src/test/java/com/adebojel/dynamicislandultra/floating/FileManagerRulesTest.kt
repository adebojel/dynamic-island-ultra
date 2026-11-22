package com.adebojel.dynamicislandultra.floating

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileManagerRulesTest {
    @Test fun acceptsSimpleFileAndFolderNames() {
        assertTrue(FileManagerRules.isValidName("Dokumen 2026"))
        assertTrue(FileManagerRules.isValidName("foto-final.jpg"))
    }

    @Test fun rejectsBlankTraversalAndPathSeparators() {
        assertFalse(FileManagerRules.isValidName(""))
        assertFalse(FileManagerRules.isValidName("   "))
        assertFalse(FileManagerRules.isValidName("."))
        assertFalse(FileManagerRules.isValidName(".."))
        assertFalse(FileManagerRules.isValidName("../private"))
        assertFalse(FileManagerRules.isValidName("folder/file.txt"))
        assertFalse(FileManagerRules.isValidName("folder\\file.txt"))
    }
}
