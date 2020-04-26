package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class AuditorImplTest {

    private val parser = mockk<Parser>()
    private val repository = mockk<Repository>()
    private val underTest = AuditorImpl(parser, repository)

    @Test
    internal fun `When logging item create is called when it's new`() {
        val quote = getQuote()

        every { repository.find(quote.id, Quote::class.java) } returns null
        every { repository.create(quote.id, quote, Quote::class.java) } returns true

        underTest.log(quote.id, quote)

        verify { repository.create(quote.id, quote, Quote::class.java) }
    }

    @Test
    internal fun `When logging item update is called when it exists`() {
        val quote = getQuote()
        val originalLog = AuditLog(quote.id.id, quote, emptyList())

        val newQuote = quote.copy(amount = quote.amount + 10.0)
        val patch = changeAmountPatch(amount = newQuote.amount)

        val newLog = AuditLog(quote.id.id, newQuote, listOf(patch))

        every { repository.find(quote.id, Quote::class.java) } returns originalLog
        every { parser.differences(quote, newQuote) } returns patch
        every { repository.update(quote.id, newLog, Quote::class.java) } returns true

        underTest.log(quote.id, newQuote)

        verify { repository.update(quote.id, newLog, Quote::class.java) }
    }
}