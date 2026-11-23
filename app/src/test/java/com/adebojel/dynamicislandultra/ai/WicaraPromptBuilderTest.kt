package com.adebojel.dynamicislandultra.ai
import org.junit.Assert.*
import org.junit.Test
class WicaraPromptBuilderTest {
    @Test fun usesWicaraMarkers() {
        val p=WicaraPromptBuilder.build("Halo")
        assertTrue(p.contains("<|system|>"))
        assertTrue(p.contains("<|user|>"))
        assertTrue(p.contains("<|assistant|>"))
        assertTrue(p.contains("<|end|>"))
    }
    @Test fun truncatesAndRemovesNullByte() {
        val p=WicaraPromptBuilder.build("x\u0000".repeat(2000))
        assertFalse(p.contains('\u0000'))
        assertTrue(p.length<2000)
    }
}
