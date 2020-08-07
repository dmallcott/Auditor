package com.dmallcott.auditor.model

import java.time.Instant

data class AuditLog(val logId: String,
                    val latestVersion: String,
                    val changelog: List<ChangelogEvent>,
                    val lastUpdated: Instant) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuditLog

        if (logId != other.logId) return false
        if (latestVersion != other.latestVersion) return false
        if (changelog != other.changelog) return false
        if (lastUpdated != other.lastUpdated) return false

        return true
    }

    override fun hashCode(): Int {
        var result = logId.hashCode()
        result = 31 * result + latestVersion.hashCode()
        result = 31 * result + changelog.hashCode()
        result = 31 * result + lastUpdated.hashCode()
        return result
    }
}