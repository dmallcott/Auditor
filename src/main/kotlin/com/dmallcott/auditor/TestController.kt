package com.dmallcott.auditor

import com.dmallcott.auditor.lib.AuditorImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random


@RestController
class TestController {

    @Autowired lateinit var auditor: AuditorImpl

    @GetMapping("/test")
    fun test() : String {
        val quote = Quote(QuoteId(java.util.UUID.randomUUID().toString()), Random.nextDouble(), randomCurrency(), randomCurrency())
        auditor.log(quote.id, quote)
        return auditor.getLatestewqe<Quote>(quote.id).toString()
    }

    fun randomCurrency() = listOf("GBP", "EUR", "USD", "CAD", "NZD").random()
}