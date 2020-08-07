package com.dmallcott.example

import com.dmallcott.auditor.Auditor
import com.dmallcott.auditor.model.ChangelogItem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.random.Random


@RestController
class TestController {

    @Autowired lateinit var auditor: Auditor

    private val quote = Quote(QuoteId(UUID.randomUUID().toString()), 10.0, "GBP", randomCurrency())
    private final fun randomCurrency() = listOf("GBP", "EUR", "USD", "CAD", "NZD").random()
    private final fun randomProfile() = Random.nextLong(0, 100000).toString()

    @GetMapping(value = ["/get"], produces = ["application/json"])
    fun get() : ResponseEntity<List<ChangelogItem<Quote>>> {
        return ResponseEntity.ok().body(auditor.getChangelog(quote.id, Quote::class.java))
    }

    @PostMapping(value = ["/create"])
    fun create() : ResponseEntity<Unit> {
        auditor.log(quote.id, quote, randomProfile())
        println("created: $quote")

        return ResponseEntity.ok().build()
    }

    @PatchMapping(value = ["/update"], produces = ["application/json"])
    fun update() : ResponseEntity<Unit> {
        auditor.log(quote.id, quote.copy(amount = 20.0), randomProfile())
        println("updated: ${quote.copy(amount = 20.0)}")

        return ResponseEntity.ok().build()
    }
}