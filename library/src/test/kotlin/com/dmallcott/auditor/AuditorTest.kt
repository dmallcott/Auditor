package com.dmallcott.auditor


import com.dmallcott.auditor.factories.Quote
import com.dmallcott.auditor.factories.changeAmountPatch
import com.dmallcott.auditor.factories.getQuote
import com.dmallcott.auditor.model.AuditLog
import com.dmallcott.auditor.model.AuditLogFactory
import com.dmallcott.auditor.model.ChangelogEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

internal class AuditorTest {

    private val parser = mockk<Parser>()
    private val auditLogFactory = mockk<AuditLogFactory>()
    private val repository = mockk<Repository>()
    private val underTest = Auditor(parser, repository, auditLogFactory)

    @Test
    internal fun `When logging item create is called when it's new`() {
        val quote = getQuote()
        val actor = "Daniel"
        val log = AuditLog("1", "{}", emptyList(), Instant.now())

        every { repository.find(quote.id, Quote::class.java) } returns null
        every { auditLogFactory.newLog(quote.id.id, quote, actor) } returns log
        every { repository.create(log, Quote::class.java) } returns AuditingResult.Success

        underTest.log(quote.id, quote, actor)

        verify { repository.create(log, Quote::class.java) }
    }

    @Test
    internal fun `When logging item update is called when it exists`() {
        val quote = getQuote()
        val actor = "Daniel"
        val originalLog = AuditLog(quote.id.id, quote.toString(), emptyList(), Instant.now())

        val newQuote = quote.copy(amount = quote.amount + 10.0)
        val patch = changeAmountPatch(amount = newQuote.amount)
        val newLog = AuditLog(quote.id.id, newQuote.toString(), listOf(ChangelogEvent(Instant.ofEpochMilli(1588430942), actor, patch)), originalLog.lastUpdated)

        every { repository.find(quote.id, Quote::class.java) } returns originalLog
        every { auditLogFactory.newFromExisting(originalLog, newQuote, actor) } returns newLog
        every { repository.update(any(), Quote::class.java) } returns AuditingResult.Success

        underTest.log(quote.id, newQuote, actor)

        verify { repository.update(any(), Quote::class.java) }
    }
}