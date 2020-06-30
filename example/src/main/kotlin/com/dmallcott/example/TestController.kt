package com.dmallcott.example

import com.dmallcott.auditor.Auditor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class TestController {

    @Autowired lateinit var auditor: Auditor

    private val mapper = jacksonObjectMapper()

    @GetMapping(value = ["/test"], produces = ["application/json"])
    fun test() : String {
        var quote = Quote(QuoteId(UUID.randomUUID().toString()), 10.0, "GBP", randomCurrency())
        auditor.log(quote.id, quote)
        println("logged: $quote")

        quote = quote.copy(amount = 20.0)
        auditor.log(quote.id, quote.copy(amount = 20.0))
        println("logged: $quote")

        quote = quote.copy(source = "EUR")
        auditor.log(quote.id, quote.copy(source = "EUR"))
        println("logged: $quote")

        return mapper.writeValueAsString(auditor.getChangelog(quote.id, Quote::class.java))
    }

    fun randomCurrency() = listOf("GBP", "EUR", "USD", "CAD", "NZD").random()
}