package com.dmallcott.auditor.lib

class AuditorImpl(val parser: Parser, val repository: Repository) : Auditor {

    inline fun <reified T : Any> log(id: LogId, newState: T) {
        val current = repository.find2<T>(id, T::class.java)

        if (current != null) {
            val differences = parser.differences(current.latestVersion, newState)
            val newLog = AuditLog<T>(logId = id.id(), latestVersion = newState, changelog = current.changelog + differences)
            repository.update2(id, newLog, T::class.java)
        } else {
            repository.create2(id, newState, T::class.java)

        }
    }

    inline fun <reified T : Any> getLatest(id: LogId): T? {
        return repository.find<T>(id)?.latestVersion
    }

    inline fun <reified T : Any> getChangelog(id: LogId): List<T> {
        // TODO test for correct order, etc
        return repository.find2(id, T::class.java)?.let {
            parser.changelog(it.latestVersion, it.changelog, T::class.java)
        } ?: emptyList()
    }
}