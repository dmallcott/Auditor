package com.dmallcott.auditor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

class Auditor(val parser: Parser, val repository: Repository) {

    inline fun <reified T : Any> log(id: LogId, newState: T) {
        val mapper = jacksonObjectMapper()
        val current = repository.find<T>(id, T::class.java)
        val new = mapper.writeValueAsString(newState)

        if (current != null) {
            val differences = parser.differences(current.latestVersion, new)
            val newLog = AuditLog(logId = id.id(), latestVersion = new, changelog = current.changelog + ChangelogEvent(Date(), differences), lastUpdated = Date())
            repository.update(id, newLog, T::class.java)
        } else {
            repository.create(id, newState, T::class.java)
        }
    }

    inline fun <reified T : Any> getLatest(id: LogId): T? {
        val mapper = jacksonObjectMapper()
        return mapper.readValue(repository.find(id, T::class.java)?.latestVersion, T::class.java)
    }

    inline fun <reified T : Any> getChangelog(id: LogId): List<T> {
        val mapper = jacksonObjectMapper()
        return repository.find(id, T::class.java)?.let {
            parser.changelog(mapper.readValue(it.latestVersion, T::class.java), it.changelog.map { it.events }, T::class.java)
        } ?: emptyList()
    }
}