package com.dmallcott.auditor.factories

import com.dmallcott.auditor.LogId

internal data class Quote(val id: QuoteId, val amount: Double, val source: String, val target: String)

internal data class QuoteId(val id: String) : LogId {
    override fun id(): String = id
}

