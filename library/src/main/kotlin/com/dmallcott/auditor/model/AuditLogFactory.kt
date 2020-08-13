package com.dmallcott.auditor.model

import com.dmallcott.auditor.Parser
import java.time.Instant

data class AuditLogFactory(private val parser: Parser) {

    fun <T> newLog(logId: String, latestVersion: T, actor: String): AuditLog {
        val now = Instant.now()
        return AuditLog(
                logId = logId,
                latestVersion = parser.asString(latestVersion),
                changelog = listOf(ChangelogEvent(timestamp = now, actor = actor, events = parser.differences(null, latestVersion))),
                lastUpdated = now
        )
    }

    fun <T:Any> newFromExisting(log: AuditLog, latestVersion: T, actor: String): AuditLog {
        val now = Instant.now()
        val currentVersion = parser.asObject(log.latestVersion, latestVersion.javaClass)
        val differences = parser.differences(currentVersion, latestVersion)
        val newChangelog = log.changelog + ChangelogEvent(Instant.now(), actor, differences)
        return AuditLog(
                logId = log.logId,
                latestVersion = parser.asString(latestVersion),
                changelog = newChangelog,
                lastUpdated = now
        )
    }
}