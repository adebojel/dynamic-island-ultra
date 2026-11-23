package com.adebojel.dynamicislandultra.ai
import org.junit.Assert.*
import org.junit.Test
class WicaraModelRulesTest {
    @Test fun modelSizeHasExactHardLimit() {
        assertTrue(WicaraModelRules.modelSizeAllowed(1))
        assertTrue(WicaraModelRules.modelSizeAllowed(100_000_000))
        assertFalse(WicaraModelRules.modelSizeAllowed(100_000_001))
        assertFalse(WicaraModelRules.modelSizeAllowed(0))
    }
    @Test fun validatesGgufMagic() {
        assertTrue(WicaraModelRules.hasGgufHeader(byteArrayOf(71,71,85,70)))
        assertFalse(WicaraModelRules.hasGgufHeader(byteArrayOf(80,75,3,4)))
        assertFalse(WicaraModelRules.hasGgufHeader(byteArrayOf(71,71,85)))
    }
    @Test fun validatesSha256() {
        val hash="a".repeat(64)
        assertTrue(WicaraModelRules.sha256Matches(hash,hash.uppercase()))
        assertFalse(WicaraModelRules.sha256Matches(hash,"b".repeat(64)))
        assertFalse(WicaraModelRules.sha256Matches("invalid",hash))
    }
}
