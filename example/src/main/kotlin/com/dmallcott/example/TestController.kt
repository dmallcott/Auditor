package com.dmallcott.example

import com.dmallcott.auditor.Auditor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class TestController {

    @Autowired lateinit var auditor: Auditor

    @GetMapping("/test")
    fun test() : String {
        val quote = Quote(QuoteId(UUID.randomUUID().toString()), 10.0, "GBP", randomCurrency())
        auditor.log(quote.id, quote)
        auditor.log(quote.id, quote.copy(amount = 20.0))
        auditor.log(quote.id, quote.copy(source = "EUR"))
        return auditor.getLatest<Quote>(quote.id).toString()
    }

    fun randomCurrency() = listOf("GBP", "EUR", "USD", "CAD", "NZD").random()
}