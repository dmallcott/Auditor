package com.dmallcott.auditor

import com.dmallcott.auditor.data.Factory
import com.dmallcott.auditor.data.Parser
import com.dmallcott.auditor.data.Repository
import com.dmallcott.auditor.model.AuditingResult
import com.dmallcott.auditor.model.ChangelogItem
import com.dmallcott.auditor.model.LogId
import com.mongodb.client.MongoDatabase

class Auditor {

    private var parser: Parser
    private var factory: Factory
    private var repository: Repository

    internal constructor(parser: Parser, repository: Repository, factory: Factory) {
        this.parser = parser
        this.repository = repository
        this.factory = factory
    }

    constructor(mongoDatabase: MongoDatabase) {
        this.parser = Parser()
        this.factory = Factory(parser)
        this.repository = Repository(mongoDatabase)
    }

    fun <T:Any> log(id: LogId, newState: T, actor: String) : AuditingResult {
        val current = repository.find(id, newState.javaClass)

        return if (current != null) {
            repository.update(factory.newFromExisting(current, newState, actor), newState.javaClass)
        } else {
            repository.create(factory.newLog(id.id(), newState, actor), newState.javaClass)
        }
    }

    // Returns list newest to oldest
    fun <T> getChangelog(id: LogId, clazz: Class<T>): List<ChangelogItem<T>> {
        // TODO you need to add schema versions in a simple way that means devs don't have to care
        return repository.find(id, clazz)?.let {
            parser.changelog(parser.asObject(it.latestVersion, clazz), it.changelog, clazz)
        } ?: emptyList()
    }
}