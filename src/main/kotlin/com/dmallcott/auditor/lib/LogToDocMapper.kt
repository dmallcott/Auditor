package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import com.dmallcott.auditor.QuoteId
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.Document


class LogToDocMapper {
    var mapper = ObjectMapper()

    companion object {

        fun <T> T.toDocument(logId: LogId) : Document = Document(mapOf(
                "id" to logId.id(),
                "latest" to this
        ))

        inline fun <reified T: Any> Document?.fromDocument() : AuditLog<T>? {
            if (this == null) {
                return null
            }

            val id : String = this.getString("id")
            val latestVersion : Document = this.get("latest", Document::class.java)

            // TODO you can't cast Document -> T
// THE HACKS ARE REAL :D
            val quote = Quote(QuoteId(id), latestVersion.getDouble("amount"), latestVersion.getString("source"), latestVersion.getString("target"))

            return AuditLog(id, quote as T)

        }

        inline fun <reified T: Any> test(doc: Document) : T {
            val javaClass = T::class.java
            val test = javaClass.getConstructor().newInstance()


            for (cons in javaClass.constructors) {
                cons.parameterTypes
            }

            for ( field in javaClass.declaredFields) {
                if (doc.containsKey(field.name)) {

                }
            }

            return test
        }
    }

    inline fun <reified T: Any> Document.test2() : T {
        val id : String = this.getString("id")
        val latestVersion : Document = this.get("latest", Document::class.java)

        return mapper.convertValue(latestVersion, T::class.java)
    }
}