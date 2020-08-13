package com.dmallcott.auditor

sealed class AuditingResult {
    object Success : AuditingResult()
    data class Error(val reason: String) : AuditingResult()
}