package com.dmallcott.auditor

import com.dmallcott.auditor.model.AuditLogFactory
import com.dmallcott.auditor.model.AuditLogFactory.Companion.latest
import com.dmallcott.auditor.model.ChangelogEvent
import com.dmallcott.auditor.model.ChangelogItem
import java.time.Instant

class Auditor(val parser: Parser, val repository: Repository) {

    inline fun <reified T : Any> log(id: LogId, newState: T) {
        val current = repository.find<T>(id, T::class.java)

        if (current != null) {
            val differences = parser.differences(current.latest(T::class.java), newState)
            val newLog = AuditLogFactory.from(id.id(), newState, current.changelog + ChangelogEvent(Instant.now(), differences), current.created)
            repository.update(id, newLog, T::class.java)
        } else {
            repository.create(id, newState, T::class.java)
        }
    }

    // Returns list newest to oldest
    inline fun <reified T : Any> getChangelog(id: LogId): List<ChangelogItem<T>> {
        return repository.find(id, T::class.java)?.let {
            parser.changelog(it.latest(T::class.java), it.changelog, T::class.java)
        } ?: emptyList()
    }
}