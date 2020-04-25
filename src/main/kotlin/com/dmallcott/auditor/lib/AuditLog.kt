package com.dmallcott.auditor.lib

import com.github.fge.jsonpatch.JsonPatch

data class AuditLog<T>(val logId: String, val latestVersion: T, val changelog: List<JsonPatch> = emptyList()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other != null && javaClass == other.javaClass) {
            other as AuditLog<*>
            return other.logId == logId && latestVersion == (other.latestVersion) && changelog.size == other.changelog.size // TODO super weak
        }

        return super.equals(other)
    }
}
// TODO add date!!!