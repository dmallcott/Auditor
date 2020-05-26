package com.dmallcott.auditor.factories

import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.TextNode
import com.github.fge.jackson.jsonpointer.JsonPointer
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.ReplaceOperation
import java.util.*
import kotlin.random.Random

internal fun getQuoteId(id: String? = null) = QuoteId(id
        ?: UUID.randomUUID().toString())

internal fun getQuote(
        id: String? = null,
        amount: Double? = null,
        sourceCurrency: String? = null,
        targetCurrency: String? = null
) = Quote(getQuoteId(id), amount
        ?: Random.nextDouble(), sourceCurrency ?: randomCurrency(),
        targetCurrency ?: randomCurrency())

fun randomCurrency() = listOf("GBP", "EUR", "USD", "CAD", "NZD").random()

fun changeAmountPatch(amount: Double) = JsonPatch(listOf(ReplaceOperation(JsonPointer("/amount"), DoubleNode(amount))))

fun changeSourceCurrencyPatch(currency: String) = JsonPatch(listOf(ReplaceOperation(JsonPointer("/source"), TextNode(currency))))
