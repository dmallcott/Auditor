package com.dmallcott.auditor


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

internal class AuditorTest {

    private val parser = mockk<Parser>()
    private val repository = mockk<Repository>()
    private val underTest = Auditor(parser, repository)

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
        val mapper = jacksonObjectMapper()

        val quote = getQuote()
        val quoteAsString = mapper.writeValueAsString(quote)
        val originalLog = AuditLog(quote.id.id, quoteAsString, emptyList())

        val newQuote = quote.copy(amount = quote.amount + 10.0)
        val newQuoteAsString = mapper.writeValueAsString(newQuote)
        val patch = changeAmountPatch(amount = newQuote.amount)

        val newLog = AuditLog(quote.id.id, newQuoteAsString, listOf(ChangelogEvent(Date(1588430942), patch)))

        every { repository.find(quote.id, Quote::class.java) } returns originalLog
        every { parser.differences(quoteAsString, newQuoteAsString) } returns patch
        every { repository.update(quote.id, any(), Quote::class.java) } returns true

        underTest.log(quote.id, newQuote)

        verify { repository.update(quote.id, any(), Quote::class.java) }
    }
}