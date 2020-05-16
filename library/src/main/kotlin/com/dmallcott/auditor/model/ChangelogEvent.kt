package com.dmallcott.auditor.model

import com.github.fge.jsonpatch.JsonPatch
import java.util.*

data class ChangelogEvent(val date: Date, val events: JsonPatch) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangelogEvent

        if (date != other.date) return false
        if (events == other.events) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + events.hashCode()
        return result
    }
} // TODO actor