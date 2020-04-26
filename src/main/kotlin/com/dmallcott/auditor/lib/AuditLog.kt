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

    override fun hashCode(): Int {
        var result = logId.hashCode()
        result = 31 * result + (latestVersion?.hashCode() ?: 0)
        result = 31 * result + changelog.hashCode()
        return result
    }
}
// TODO add date!!!