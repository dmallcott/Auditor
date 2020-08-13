package com.dmallcott.auditor.model

import java.time.Instant

data class AuditLog(val logId: String,
                    val latestVersion: String,
                    val changelog: List<ChangelogEvent>,
                    val lastUpdated: Instant)