package com.dmallcott.auditor.model

import java.util.*

data class AuditLog(val logId: String,
                    val latestVersion: String,
                    val changelog: List<ChangelogEvent>,
                    val created: Date) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuditLog

        if (logId != other.logId) return false
        if (latestVersion != other.latestVersion) return false
        if (changelog != other.changelog) return false
        if (created != other.created) return false

        return true
    }

    override fun hashCode(): Int {
        var result = logId.hashCode()
        result = 31 * result + latestVersion.hashCode()
        result = 31 * result + changelog.hashCode()
        result = 31 * result + created.hashCode()
        return result
    }
}