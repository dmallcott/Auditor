package com.dmallcott.auditor.model

import java.time.Instant

data class ChangelogItem<T>(val state: T, val timestamp: Instant)