package com.dmallcott.auditor

import com.github.fge.jsonpatch.JsonPatch
import java.util.*

data class ChangelogEvent(val date: Date, val events: JsonPatch)