package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import com.mongodb.client.MongoClient
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestMethodOrder(OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RepositoryIntegrationTest {

    @Autowired // TODO you should provide directly this is too tied to spring
    lateinit var mongoClient: MongoClient
    lateinit var underTest: Repository

    final val quoteId = getQuoteId()
    final val quote = getQuote(quoteId.id, amount = 20.0, sourceCurrency = "GBP", targetCurrency = "USD")

    @BeforeAll
    fun setUp() {
        underTest = Repository(mongoDatabase = mongoClient.getDatabase("test")) // TODO clean up
    }

    @Test
    @Order(1)
    internal fun insert() {
        assertTrue(underTest.create2(quoteId, quote, Quote::class.java))
    }

    @Test
    @Order(2)
    internal fun find() {
        assertNotNull(underTest.find2(quoteId, Quote::class.java))
    }

    @Test
    @Order(3)
    internal fun update() {
        val newAmount = quote.amount + 10.0
        val newQuote = quote.copy(amount = newAmount)
        val newLog = AuditLog<Quote>(quoteId.id, newQuote, mutableListOf(changeAmountPatch(amount = newAmount)))
        underTest.update2<Quote>(quoteId, newLog, Quote::class.java)

        val savedQuote = underTest.find2<Quote>(quoteId, Quote::class.java)
        assertNotNull(savedQuote)
        assertEquals(savedQuote, newLog)
    }

    @Test
    @Order(4)
    internal fun delete() {
        assertTrue(underTest.delete<Quote>(quoteId))
    }

    @AfterAll
    fun tearDown() {
        assertNull(underTest.find2<Quote>(quoteId, Quote::class.java))
    }
}