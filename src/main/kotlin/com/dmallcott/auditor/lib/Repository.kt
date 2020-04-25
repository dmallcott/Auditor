package com.dmallcott.auditor.lib

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.JsonPatchOperation
import com.mongodb.MongoException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import org.bson.Document


class Repository(val mongoDatabase: MongoDatabase) {
    val mapper = jacksonObjectMapper()

    inline fun <reified T : Any> find(logId: LogId): AuditLog<T>? {
        val doc = getCollection<T>().find(eq("id", logId.id())).first() ?: return null

        val id = doc.getString("id")
        val latest = doc.get("latest", Document::class.java)
        val changelog = doc.getList("changelog", String::class.java) // Dodgy

        val test = mapper.convertValue(latest, T::class.java)

        return AuditLog(id, test, changelog.map { JsonPatch.fromJson(mapper.readTree(it)) })
    }

    fun <T : Any> find2(logId: LogId, clazz: Class<T>): AuditLog<T>? {
        val doc = clazz.getCollection2().find(eq("id", logId.id())).first() ?: return null

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

    // TODO create a T extension
    inline fun <reified T : Any> create(logId: LogId, item: T): Boolean {
        val doc = Document(mapOf(
                "id" to logId.id(),
                "latest" to mapper.convertValue(item, Document::class.java),
                "changelog" to emptyList<T>()
        ))
        return try {
            getCollection<T>().insertOne(doc)
            true
        } catch (e: MongoException) {
            false // TODO elaborate?
        }
    }

    fun <T : Any> create2(logId: LogId, item: T, clazz: Class<T>): Boolean {
        val doc = Document(mapOf(
                "id" to logId.id(),
                "latest" to mapper.convertValue(item, Document::class.java),
                "changelog" to ""
        ))
        return try {
            clazz.getCollection2().insertOne(doc)
            true
        } catch (e: MongoException) {
            false // TODO elaborate?
        }
    }

    inline fun <reified T : Any> update(logId: LogId, newLog: AuditLog<T>): Boolean {
        return try {
            getCollection<T>().replaceOne(eq("id", logId.id()), mapToDoc(newLog)).wasAcknowledged()
        } catch (e: MongoException) {
            false
        }
    }

    fun <T : Any> update2(logId: LogId, newLog: AuditLog<T>, clazz: Class<T>): Boolean {
        return try {
            clazz.getCollection2().replaceOne(eq("id", logId.id()), mapToDoc2(newLog)).wasAcknowledged()
        } catch (e: MongoException) {
            false
        }
    }

    inline fun <reified T : Any> delete(logId: LogId): Boolean {
        return getCollection<T>().deleteOne(eq("id", logId.id())).wasAcknowledged()
    }

    fun <T> Class<T>.getCollection2() = mongoDatabase.getCollection(this.simpleName)

    inline fun <reified T : Any> getCollection() = mongoDatabase.getCollection(T::class.java.simpleName)

    fun <T : Any> mapToDoc2(log: AuditLog<T>) = Document(mapOf(
            "id" to log.logId,
            "latest" to mapper.convertValue(log.latestVersion, Document::class.java),
//            "changelog" to log.changelog
            "changelog" to mapper.writeValueAsString(log.changelog)
//            "changelog" to log.changelog.map { mapper.convertValue(it, object : TypeReference<ArrayList<Document>>() {}) }
    ))

    inline fun <reified T : Any> mapToDoc(log: AuditLog<T>) = Document(mapOf(
            "id" to log.logId,
            "latest" to mapper.convertValue(log.latestVersion, Document::class.java),
            "changelog" to mapper.writeValueAsString(log.changelog)
    ))
}