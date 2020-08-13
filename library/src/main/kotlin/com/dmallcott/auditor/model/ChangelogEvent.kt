package com.dmallcott.auditor.model

import com.github.fge.jsonpatch.JsonPatch
import java.time.Instant

data class ChangelogEvent(val timestamp: Instant = Instant.now(), val actor: String, val events: JsonPatch) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangelogEvent

        if (timestamp != other.timestamp) return false
        if (actor != other.actor) return false
//        TODO Purposely skipped due to not working
//        if (events != other.events) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + events.hashCode()
        return result
    }
}