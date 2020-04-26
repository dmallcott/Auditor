package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class ParserTest {

    private val underTest = Parser()

    @Test
    internal fun `Applying patch to original json returns new json`() {
        val test1 = getQuote("adbcd")
        val test2 = getQuote("efghi")

        val patch = underTest.differences(test1, test2)

        assertNotNull(patch)
        assertEquals(underTest.asNode(test2), patch.apply(underTest.asNode(test1)))
    }

    @Test
    internal fun `Given latest and patches, changelog returns all previous versions with the last being original`() {
        val latest = getQuote("adbcd", sourceCurrency = "GBP", amount = 10.0)
        val patches = listOf(changeAmountPatch(20.0), changeSourceCurrencyPatch("EUR"))
        val original = latest.copy(amount = 20.0, source = "EUR")

        val changelog = underTest.changelog(latest, patches, Quote::class.java)

        assertNotNull(changelog)
        assertEquals(changelog.size, patches.size + 1)
        assertEquals(changelog.last(), original)
    }
}