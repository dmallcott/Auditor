package com.dmallcott.auditor

import com.dmallcott.auditor.model.AuditLogFactory
import com.dmallcott.auditor.model.AuditLogFactory.Companion.latest
import com.dmallcott.auditor.model.ChangelogEvent
import com.dmallcott.auditor.model.ChangelogItem
import com.mongodb.client.MongoDatabase
import java.time.Instant
import java.util.*

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

    fun <T:Any> log(id: LogId, newState: T, actor: String) {
        val current = repository.find(id, newState.javaClass)

        if (current != null) {
            val differences = parser.differences(current.latest(newState.javaClass), newState)
            val newChangelog = current.changelog + ChangelogEvent(Instant.now(), actor, differences)
            val newLog = AuditLogFactory.from(id.id(), newState, newChangelog, current.created)
            repository.update(id, newLog, newState.javaClass)
        } else {
            val differences = parser.differences(null, newState)
            val newChangelog = listOf(ChangelogEvent(Instant.now(), actor, differences))
            val newLog = AuditLogFactory.from(id.id(), newState, newChangelog, Date())
            repository.create(id, newLog, newState.javaClass) // You're not storing the actor of the initial creation
        }
    }

    // Returns list newest to oldest
    fun <T> getChangelog(id: LogId, clazz: Class<T>): List<ChangelogItem<T>> {
        return repository.find(id, clazz)?.let {
            parser.changelog(it.latest(clazz), it.changelog, clazz)
        } ?: emptyList()
    }
}