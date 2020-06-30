package com.dmallcott.auditor

import com.dmallcott.auditor.model.AuditLogFactory
import com.dmallcott.auditor.model.AuditLogFactory.Companion.latest
import com.dmallcott.auditor.model.ChangelogEvent
import com.dmallcott.auditor.model.ChangelogItem
import com.mongodb.client.MongoDatabase
import java.time.Instant

class Auditor {

    private var parser: Parser
    private var repository: Repository

    internal constructor(parser: Parser, repository: Repository) {
        this.parser = parser
        this.repository = repository
    }

    constructor(mongoDatabase: MongoDatabase) {
        this.parser = Parser()
        this.repository = Repository(mongoDatabase)
    }

    fun <T:Any> log(id: LogId, newState: T) {
        val current = repository.find(id, newState.javaClass)

        if (current != null) {
            val differences = parser.differences(current.latest(newState.javaClass), newState)
            val newLog = AuditLogFactory.from(id.id(), newState, current.changelog + ChangelogEvent(Instant.now(), differences), current.created)
            repository.update(id, newLog, newState.javaClass)
        } else {
            repository.create(id, newState, newState.javaClass)
        }
    }

    // Returns list newest to oldest
    fun <T> getChangelog(id: LogId, clazz: Class<T>): List<ChangelogItem<T>> {
        return repository.find(id, clazz)?.let {
            parser.changelog(it.latest(clazz), it.changelog, clazz)
        } ?: emptyList()
    }
}