package com.dmallcott.auditor.model

import com.github.fge.jsonpatch.JsonPatch
import java.time.Instant

data class ChangelogEvent(val timestamp: Instant = Instant.now(), val actor: String, val events: JsonPatch)