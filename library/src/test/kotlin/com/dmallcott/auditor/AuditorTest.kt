package com.dmallcott.auditor


import com.dmallcott.auditor.factories.Quote
import com.dmallcott.auditor.factories.changeAmountPatch
import com.dmallcott.auditor.factories.getQuote
import com.dmallcott.auditor.model.AuditLogFactory
import com.dmallcott.auditor.model.AuditLogFactory.Companion.latest
import com.dmallcott.auditor.model.ChangelogEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
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
        val quote = getQuote()
        val originalLog = AuditLogFactory.from(quote.id.id, quote, emptyList(), Date())

        val newQuote = quote.copy(amount = quote.amount + 10.0)
        val patch = changeAmountPatch(amount = newQuote.amount)
        val newLog = AuditLogFactory.from(quote.id.id, newQuote, listOf(ChangelogEvent(Instant.ofEpochMilli(1588430942), patch)), originalLog.created)

        every { repository.find(quote.id, Quote::class.java) } returns originalLog
        every { parser.differences(originalLog.latest(Quote::class.java), newQuote) } returns patch
        every { repository.update(quote.id, any(), Quote::class.java) } returns true

        underTest.log(quote.id, newQuote)

        verify { repository.update(quote.id, any(), Quote::class.java) }
    }
}