package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class AuditorImplTest {

    val parser = mockk<Parser>()
    val repository = mockk<Repository>()
    val underTest = AuditorImpl(parser, repository)

    @Test
    internal fun `When logging item create is called when it's new`() {
        val quote = getQuote()

        every { repository.find2(quote.id, Quote::class.java) } returns null
        every { repository.create2(quote.id, quote, Quote::class.java) } returns true

        underTest.log(quote.id, quote)

        verify { repository.create2(quote.id, quote, Quote::class.java) }
    }

    @Test
    internal fun `When logging item update is called when it exists`() {
        val quote = getQuote()
        val originalLog = AuditLog(quote.id.id, quote, emptyList())

        val newQuote = quote.copy(amount = quote.amount + 10.0)
        val patch = changeAmountPatch(amount = newQuote.amount)

        val newLog = AuditLog(quote.id.id, newQuote, listOf(patch))

        every { repository.find2(quote.id, Quote::class.java) } returns originalLog
        every { parser.differences(quote, newQuote) } returns patch
        every { repository.update2(quote.id, newLog, Quote::class.java) } returns true

        underTest.log(quote.id, newQuote)

        verify { repository.update2(quote.id, newLog, Quote::class.java) }
    }

    @Test
    internal fun name() {
        val latest = getQuote("adbcd", sourceCurrency = "GBP", amount = 10.0)
        val patches = listOf(changeAmountPatch(20.0), changeSourceCurrencyPatch("EUR"))
        val original = latest.copy(amount = 20.0, source = "EUR")

        val log = AuditLog(logId = latest.id.id, latestVersion = latest, changelog = patches)

        every { repository.find2(latest.id, Quote::class.java) } returns log


        underTest.getChangelog<Quote>(latest.id)

        // Pointless test
    }
}