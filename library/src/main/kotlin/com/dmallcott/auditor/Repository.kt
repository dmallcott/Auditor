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

    fun <T : Any> find(logId: LogId, clazz: Class<T>): AuditLog<T>? {
        val doc = clazz.getCollection().find(eq("id", logId.id())).first() ?: return null

        val id = doc.getString("id")
        val latest = doc.get("latest", Document::class.java)
        val changelog = doc.getString("changelog")  // Dodgy

        val fin = if (!changelog.isNullOrEmpty()) {
            val changes = mapper.readValue<List<List<JsonPatchOperation>>>(changelog)
            changes.map { JsonPatch(it) }
        } else {
            emptyList()
        }
        return AuditLog(id, mapper.convertValue(latest, clazz), fin)
    }

    fun <T : Any> create(logId: LogId, item: T, clazz: Class<T>): Boolean {
        val doc = Document(mapOf(
                "id" to logId.id(),
                "latest" to mapper.convertValue(item, Document::class.java),
                "changelog" to ""
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
            clazz.getCollection().replaceOne(eq("id", logId.id()), mapToDoc2(newLog)).wasAcknowledged()
        } catch (e: MongoException) {
            false
        }
    }

    fun <T> delete(logId: LogId, clazz: Class<T>): Boolean {
        return clazz.getCollection().deleteOne(eq("id", logId.id())).wasAcknowledged()
    }

    private fun <T> Class<T>.getCollection() = mongoDatabase.getCollection(this.simpleName)

    private fun <T : Any> mapToDoc2(log: AuditLog<T>) = Document(mapOf(
            "id" to log.logId,
            "latest" to mapper.convertValue(log.latestVersion, Document::class.java),
            "changelog" to mapper.writeValueAsString(log.changelog)
    ))
}