package com.dmallcott.auditor.lib

class AuditorImpl(val parser: Parser, val repository: Repository) : Auditor {

    inline fun <reified T : Any> log(id: LogId, newState: T) {
        val current = repository.find<T>(id, T::class.java)

        if (current != null) {
            val differences = parser.differences(current.latestVersion, newState)
            val newLog = AuditLog<T>(logId = id.id(), latestVersion = newState, changelog = current.changelog + differences)
            repository.update(id, newLog, T::class.java)
        } else {
            repository.create(id, newState, T::class.java)
        }
    }

    inline fun <reified T : Any> getLatest(id: LogId): T? {
        return repository.find(id, T::class.java)?.latestVersion
    }

    inline fun <reified T : Any> getChangelog(id: LogId): List<T> {
        return repository.find(id, T::class.java)?.let {
            parser.changelog(it.latestVersion, it.changelog, T::class.java)
        } ?: emptyList()
    }
}