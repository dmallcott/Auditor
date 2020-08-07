package com.dmallcott.auditor

import com.dmallcott.auditor.codec.AuditLogCodec
import com.dmallcott.auditor.model.AuditLog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.MongoClientSettings
import com.mongodb.MongoException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import org.bson.codecs.configuration.CodecRegistries


class Repository(mongoDatabase: MongoDatabase) {

    private val mapper = jacksonObjectMapper()
    private val database = mongoDatabase.withCodecRegistry(CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromCodecs(AuditLogCodec(mapper, MongoClientSettings.getDefaultCodecRegistry())))
    )

    private companion object {
        const val ID = "_id"
    }

    fun <T> find(logId: LogId, clazz: Class<T>): AuditLog? {
        return clazz.getCollection().find(eq(ID, logId.id())).first() ?: return null
    }

    fun <T> create(log: AuditLog,  clazz: Class<T>): Boolean {
        return try {
            clazz.getCollection().insertOne(log)
            true
        } catch (e: MongoException) {
            false // TODO elaborate?
        }
    }

    fun <T> update(newLog: AuditLog, clazz: Class<T>): Boolean {
        return try {
            clazz.getCollection().replaceOne(eq(ID, newLog.logId), newLog).wasAcknowledged()
        } catch (e: MongoException) {
            false
        }
    }

    fun <T> delete(logId: LogId, clazz: Class<T>): Boolean {
        return clazz.getCollection().deleteOne(eq(ID, logId.id())).wasAcknowledged()
    }

    private fun <T> Class<T>.getCollection() = database.getCollection(this.simpleName, AuditLog::class.java)
}