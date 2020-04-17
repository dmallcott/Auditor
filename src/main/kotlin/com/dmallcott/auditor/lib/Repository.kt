package com.dmallcott.auditor.lib

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.mongodb.MongoException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import org.bson.Document


class Repository(val mongoDatabase: MongoDatabase) {
    val mapper = jacksonObjectMapper()

     fun test(logId: LogId): AuditLog<Any>? {
      return find<Any>(logId)
    }

    inline fun <reified T : Any> find(logId: LogId): AuditLog<T>? {
        val doc = getCollection<T>().find(eq("id", logId.id())).first() ?: return null

        val id = doc.getString("id")
        val latest = doc.get("latest", Document::class.java)
        val changelog = doc.get("changelog", ArrayList<JsonPatch>().javaClass) // Dodgy

        val test = mapper.convertValue(latest, T::class.java)

        return AuditLog(id, test, changelog)
    }

    fun <T : Any> find2(logId: LogId, clazz: Class<T>): AuditLog<T>? {
        val doc = mongoDatabase.getCollection(clazz.simpleName).find(eq("id", logId.id())).first() ?: return null

        val id = doc.getString("id")
        val latest = doc.get("latest", Document::class.java)
        val changelog = doc.get("changelog", ArrayList<JsonPatch>().javaClass) // Dodgy

        val test = mapper.convertValue(latest, clazz)

        return AuditLog(id, test, changelog)
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
                "changelog" to emptyList<T>()
        ))
        return try {
            mongoDatabase.getCollection(clazz.simpleName).insertOne(doc)
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

    inline fun <reified T : Any> delete(logId: LogId): Boolean {
        return getCollection<T>().deleteOne(eq("id", logId.id())).wasAcknowledged()
    }

    inline fun <reified T : Any> getCollection() = mongoDatabase.getCollection(T::class.java.simpleName)

    inline fun <reified T : Any> mapToDoc(log: AuditLog<T>) = Document(mapOf(
                "id" to log.logId,
                "latest" to mapper.convertValue(log.latestVersion, Document::class.java),
                "changelog" to log.changelog
        ))
}