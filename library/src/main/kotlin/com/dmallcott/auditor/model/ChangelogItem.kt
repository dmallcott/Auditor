package com.dmallcott.auditor.model

import java.time.Instant

data class ChangelogItem<T>(val state: T, val actor: String, val timestamp: Instant)