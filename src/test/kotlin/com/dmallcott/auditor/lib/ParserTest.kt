package com.dmallcott.auditor.lib

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class ParserTest {

    val underTest = Parser()

    @Test
    internal fun `Applying patch to original json returns new json`() {
        val test1 = getQuote("adbcd")
        val test2 = getQuote("efghi")

        val patch = underTest.differences(test1, test2)

        assertNotNull(patch)
        assertEquals(underTest.asNode(test2), patch.apply(underTest.asNode(test1)))
    }
}