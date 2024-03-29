package com.dmallcott.auditor

import com.dmallcott.auditor.data.Repository
import com.dmallcott.auditor.factories.Quote
import com.dmallcott.auditor.factories.changeAmountPatch
import com.dmallcott.auditor.factories.getQuote
import com.dmallcott.auditor.factories.getQuoteId
import com.dmallcott.auditor.model.AuditLog
import com.dmallcott.auditor.model.AuditingResult
import com.dmallcott.auditor.model.ChangelogEvent
import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import java.time.Instant

@TestMethodOrder(OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RepositoryIntegrationTest {

    lateinit var underTest: Repository

    private val mongoClient: MongoClient = MongoClients.create(ConnectionString("mongodb://localhost:27017"))

    private val quoteId = getQuoteId()
    private val quote = getQuote(quoteId.id, amount = 20.0, sourceCurrency = "GBP", targetCurrency = "USD")

    @BeforeAll
    fun setUp() {
        underTest = Repository(mongoDatabase = mongoClient.getDatabase("test")) // TODO clean up
    }

    @Test
    @Order(1)
    internal fun insert() {
        assertTrue(underTest.create(AuditLog(quote.id.id, quote.toString(), emptyList(), Instant.now()), Quote::class.java) is AuditingResult.Success)
    }

    @Test
    @Order(2)
    internal fun find() {
        assertNotNull(underTest.find(quoteId, Quote::class.java))
    }

    @Test
    @Order(3)
    internal fun update() {
        val newAmount = quote.amount + 10.0
        val newQuote = quote.copy(amount = newAmount)
        val actor = "Daniel"
        val newLog = AuditLog(quoteId.id, newQuote.toString(), mutableListOf(
                ChangelogEvent(Instant.ofEpochMilli(1588430942), actor, changeAmountPatch(amount = newAmount))
        ), Instant.ofEpochMilli(1588430952))

        val updateResult = underTest.update(newLog, Quote::class.java)
        assert(updateResult is AuditingResult.Success)

        val findResult = underTest.find(quoteId, Quote::class.java)
        assertEquals(findResult, newLog)
    }

    @Test
    @Order(4)
    internal fun delete() {
        assertTrue(underTest.delete(quoteId, Quote::class.java) is AuditingResult.Success)
    }

    @AfterAll
    fun tearDown() {
        assertNull(underTest.find(quoteId, Quote::class.java))
    }
}