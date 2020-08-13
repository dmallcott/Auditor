package com.dmallcott.auditor.model

sealed class AuditingResult {
    object Success : AuditingResult()
    data class Error(val reason: String) : AuditingResult()
}