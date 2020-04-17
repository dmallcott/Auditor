package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import com.dmallcott.auditor.QuoteId
import org.bson.Document
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

internal class LogToDocMapperTest {


    final val quoteId = QuoteId(UUID.randomUUID().toString())
    final val quote = Quote(quoteId, Random.nextDouble(), randomCurrency(), randomCurrency())
    final fun randomCurrency() = listOf("GBP", "EUR", "USD", "CAD", "NZD").random()

    @Test
    internal fun name() {
        val newQuote = LogToDocMapper.test<Quote>(Document(mapOf("id" to quoteId.id, "latest" to quote)))
        assert(newQuote == quote)
    }
}