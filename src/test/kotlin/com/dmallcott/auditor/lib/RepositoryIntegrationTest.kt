package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import com.mongodb.client.MongoClient
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
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
        underTest = Repository(mongoDatabase = mongoClient.getDatabase("auditor")) // TODO clean up
    }

    @Test
    @Order(1)
    internal fun insert() {
        assertTrue(underTest.create(quoteId, quote))
    }

    @Test
    @Order(2)
    internal fun find() {
        assert(underTest.find<Quote>(quoteId) != null)
    }

    @Test
    @Order(3)
    internal fun update() {
        val newAmount = quote.amount + 10.0
        val newQuote = quote.copy(amount = newAmount)
        val newLog = AuditLog<Quote>(quoteId.id, newQuote, listOf(changeAmountPatch(amount = newAmount)))
        underTest.update<Quote>(quoteId, newLog)

        assert(underTest.find<Quote>(quoteId)!!.latestVersion.amount == newAmount)
    }

    @Test
    @Order(4)
    internal fun delete() {
        assertTrue(underTest.delete<Quote>(quoteId))
    }

    @AfterAll
    fun tearDown() {
        assert(underTest.find<Quote>(quoteId) == null)
    }
}