package com.dmallcott.auditor

import com.dmallcott.auditor.model.AuditLogFactory
import com.dmallcott.auditor.model.AuditLogFactory.Companion.latest
import com.dmallcott.auditor.model.ChangelogEvent
import java.util.*

class Auditor(val parser: Parser, val repository: Repository) {

    inline fun <reified T : Any> log(id: LogId, newState: T) {
        val current = repository.find<T>(id, T::class.java)

        if (current != null) {
            val differences = parser.differences(current.latestVersion, current.latest(T::class.java))
            val newLog = AuditLogFactory.from(id.id(), newState, current.changelog + ChangelogEvent(Date(), differences), current.created)
            repository.update(id, newLog, T::class.java)
        } else {
            repository.create(id, newState, T::class.java)
        }
    }

    inline fun <reified T : Any> getChangelog(id: LogId): List<T> {
        return repository.find(id, T::class.java)?.let {
            parser.changelog(it.latest(T::class.java), it.changelog.map { it.events }, T::class.java)
        } ?: emptyList()
    }
}