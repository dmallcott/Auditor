package com.dmallcott.auditor.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

class AuditLogFactory {

    companion object {
        private val mapper = jacksonObjectMapper()

        fun <T> from(logId: String, latestVersion: T, changelog: List<ChangelogEvent>, lastUpdated: Date): AuditLog {
            return AuditLog(
                    logId = logId,
                    latestVersion = mapper.writeValueAsString(latestVersion),
                    changelog = changelog,
                    created = lastUpdated
            )
        }

        fun <T> AuditLog.latest(clazz: Class<T>): T = mapper.readValue(this.latestVersion, clazz)

        fun <T> T.asString(): String = mapper.writeValueAsString(this)
    }
}