package com.dmallcott.auditor

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation

@TestMethodOrder(OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RepositoryIntegrationTest {

    lateinit var underTest: Repository

    private val mongoClient: MongoClient = MongoClients.create(ConnectionString("mongodb://localhost:27017"))

    private final val quoteId = getQuoteId()
    private final val quote = getQuote(quoteId.id, amount = 20.0, sourceCurrency = "GBP", targetCurrency = "USD")

    @BeforeAll
    fun setUp() {
        underTest = Repository(mongoDatabase = mongoClient.getDatabase("test")) // TODO clean up
    }

    @Test
    @Order(1)
    internal fun insert() {
        assertTrue(underTest.create(quoteId, quote, Quote::class.java))
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
        val newLog = AuditLog(quoteId.id, newQuote, mutableListOf(changeAmountPatch(amount = newAmount)))
        underTest.update(quoteId, newLog, Quote::class.java)

        val savedQuote = underTest.find(quoteId, Quote::class.java)
        assertNotNull(savedQuote)
        assertEquals(savedQuote, newLog)
    }

    @Test
    @Order(4)
    internal fun delete() {
        assertTrue(underTest.delete(quoteId, Quote::class.java))
    }

    @AfterAll
    fun tearDown() {
        assertNull(underTest.find(quoteId, Quote::class.java))
    }
}