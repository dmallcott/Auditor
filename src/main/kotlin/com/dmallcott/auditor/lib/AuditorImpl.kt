package com.dmallcott.auditor.lib

class AuditorImpl(val parser: Parser, val repository: Repository) : Auditor {

    fun test(id: LogId) = repository.test(id)

    inline fun <reified T: Any> log(id: LogId, newState: T) {
        val current = repository.find2<T>(id, T::class.java)

        current?.let {
            val differences = parser.differences(it.latestVersion, newState)
            val newLog = AuditLog<T>(logId = id.id(), latestVersion = newState, changelog = it.changelog + differences)
            repository.update(id, newLog)
        } ?: run {
            repository.create2(id, newState, T::class.java)
        }
    }

    // WOOOOOOW :o
    inline fun <reified T: Any> getLatestewqe(id: LogId): T? {
        return repository.find<T>(id)?.latestVersion
    }

    inline fun <reified T: Any> getChanges(id: LogId) {
        TODO()
    }
}