package com.dmallcott.example

import com.dmallcott.auditor.model.LogId

data class Quote(val id: QuoteId, val amount: Double, val source: String, val target: String)

data class QuoteId(val id: String) : LogId {
    override fun id(): String = id
}

