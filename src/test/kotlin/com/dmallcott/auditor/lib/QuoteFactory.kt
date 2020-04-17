package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import com.dmallcott.auditor.QuoteId
import com.fasterxml.jackson.databind.node.DoubleNode
import com.github.fge.jackson.jsonpointer.JsonPointer
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.ReplaceOperation
import java.util.*
import kotlin.random.Random

fun getQuoteId(id: String? = null) = QuoteId(id ?: UUID.randomUUID().toString())
fun getQuote(id: String? = null, amount: Double? = null, sourceCurrency: String? = null, targetCurrency: String? = null) =
        Quote(getQuoteId(id), amount ?: Random.nextDouble(), sourceCurrency ?: randomCurrency(),  targetCurrency ?: randomCurrency())

private fun randomCurrency() = listOf("GBP", "EUR", "USD", "CAD", "NZD").random()

fun changeAmountPatch(amount: Double) = JsonPatch(listOf(ReplaceOperation(JsonPointer("/amount"), DoubleNode(amount))))