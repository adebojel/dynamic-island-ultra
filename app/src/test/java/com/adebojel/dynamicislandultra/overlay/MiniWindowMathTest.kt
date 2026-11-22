package com.adebojel.dynamicislandultra.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiniWindowMathTest {
    @Test fun supportsFourBasicOperators() {
        assertEquals(7.0, MiniWindowMath.calculate(3.0, "+", 4.0)!!, 0.00001)
        assertEquals(2.0, MiniWindowMath.calculate(5.0, "−", 3.0)!!, 0.00001)
        assertEquals(12.0, MiniWindowMath.calculate(3.0, "×", 4.0)!!, 0.00001)
        assertEquals(2.0, MiniWindowMath.calculate(8.0, "÷", 4.0)!!, 0.00001)
    }

    @Test fun rejectsDivisionByZeroAndUnknownOperator() {
        assertNull(MiniWindowMath.calculate(1.0, "÷", 0.0))
        assertNull(MiniWindowMath.calculate(1.0, "?", 2.0))
    }
}
