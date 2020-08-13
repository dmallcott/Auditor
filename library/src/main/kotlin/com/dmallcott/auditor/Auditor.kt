package com.dmallcott.auditor

import com.dmallcott.auditor.model.AuditLogFactory
import com.dmallcott.auditor.model.ChangelogItem
import com.mongodb.client.MongoDatabase

class Auditor {

    private var parser: Parser
    private var auditLogFactory: AuditLogFactory
    private var repository: Repository

    internal constructor(parser: Parser, repository: Repository, auditLogFactory: AuditLogFactory) {
        this.parser = parser
        this.repository = repository
        this.auditLogFactory = auditLogFactory
    }

    constructor(mongoDatabase: MongoDatabase) {
        this.parser = Parser()
        this.auditLogFactory = AuditLogFactory(parser)
        this.repository = Repository(mongoDatabase)
    }

    fun <T:Any> log(id: LogId, newState: T, actor: String) : AuditingResult {
        val current = repository.find(id, newState.javaClass)

        return if (current != null) {
            repository.update(auditLogFactory.newFromExisting(current, newState, actor), newState.javaClass)
        } else {
            repository.create(auditLogFactory.newLog(id.id(), newState, actor), newState.javaClass)
        }
    }

    // Returns list newest to oldest
    fun <T> getChangelog(id: LogId, clazz: Class<T>): List<ChangelogItem<T>> {
        return repository.find(id, clazz)?.let {
            parser.changelog(parser.asObject(it.latestVersion, clazz), it.changelog, clazz)
        } ?: emptyList()
    }
}