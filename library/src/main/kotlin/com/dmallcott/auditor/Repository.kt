package com.dmallcott.auditor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.JsonPatchOperation
import com.mongodb.MongoException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import org.bson.Document


class Repository(val mongoDatabase: MongoDatabase) {
    private val mapper = jacksonObjectMapper()

    private companion object {
        const val ID = "_id"
        const val LATEST = "latest"
        const val CHANGELOG = "changelog"
    }

    fun <T : Any> find(logId: LogId, clazz: Class<T>): AuditLog<T>? {
        val doc = clazz.getCollection().find(eq(ID, logId.id())).first() ?: return null

        val id = doc.getString(ID)
        val latest = doc.get(LATEST, Document::class.java)
        val changelog = doc.getString(CHANGELOG)

        val mappedChangelog = if (changelog.isNullOrEmpty().not()) {
            val changes = mapper.readValue<List<List<JsonPatchOperation>>>(changelog)
            changes.map { JsonPatch(it) }
        } else {
            emptyList()
        }
        return AuditLog(id, mapper.convertValue(latest, clazz), mappedChangelog)
    }

    fun <T : Any> create(logId: LogId, item: T, clazz: Class<T>): Boolean {
        val doc = Document(mapOf(
                ID to logId.id(),
                LATEST to mapper.convertValue(item, Document::class.java),
                CHANGELOG to ""
        ))
        return try {
            clazz.getCollection().insertOne(doc)
            true
        } catch (e: MongoException) {
            false // TODO elaborate?
        }
    }

    fun <T : Any> update(logId: LogId, newLog: AuditLog<T>, clazz: Class<T>): Boolean {
        return try {
            clazz.getCollection().replaceOne(eq(ID, logId.id()), mapToDoc2(newLog)).wasAcknowledged()
        } catch (e: MongoException) {
            false
        }
    }

    fun <T> delete(logId: LogId, clazz: Class<T>): Boolean {
        return clazz.getCollection().deleteOne(eq(ID, logId.id())).wasAcknowledged()
    }

    private fun <T> Class<T>.getCollection() = mongoDatabase.getCollection(this.simpleName)

    private fun <T : Any> mapToDoc2(log: AuditLog<T>) = Document(mapOf(
            ID to log.logId,
            LATEST to mapper.convertValue(log.latestVersion, Document::class.java),
            CHANGELOG to mapper.writeValueAsString(log.changelog)
    ))
}