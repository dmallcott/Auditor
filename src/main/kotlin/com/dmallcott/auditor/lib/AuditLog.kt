package com.dmallcott.auditor.lib

import com.github.fge.jsonpatch.JsonPatch

data class AuditLog<T>(val logId: String, val latestVersion: T, val changelog: List<JsonPatch> = emptyList())