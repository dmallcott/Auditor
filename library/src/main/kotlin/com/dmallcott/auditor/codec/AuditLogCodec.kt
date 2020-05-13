package com.dmallcott.auditor.codec

import com.dmallcott.auditor.AuditLog
import com.dmallcott.auditor.ChangelogEvent
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import org.bson.*
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*


class AuditLogCodec(private val objectMapper: ObjectMapper,
                    codecRegistry: CodecRegistry) : Codec<AuditLog<*>> {

    private val rawBsonDocumentCodec: Codec<BsonDocument> = codecRegistry.get(BsonDocument::class.java)

    override fun getEncoderClass(): Class<AuditLog<*>> {
        return AuditLog::class.java
    }

    override fun encode(writer: BsonWriter, value: AuditLog<*>, encoderContext: EncoderContext) {
        try {
            val latestAsString: String = objectMapper.writeValueAsString(value.latestVersion)

            val changelog = value.changelog.map {
                BsonDocument(listOf(
                        BsonElement("date", BsonDateTime(it.date.time)),
                        BsonElement("changes", BsonString(objectMapper.writeValueAsString(value.changelog)))
                ))
            }

            val doc = BsonDocument(listOf(
                    BsonElement("id", BsonString(value.logId)),
                    BsonElement("latestVersion", BsonString(latestAsString)),
                    BsonElement("changelog", BsonArray(changelog))
            ))

            rawBsonDocumentCodec.encode(writer, doc, encoderContext)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): AuditLog<Any> {
        return try {
            val document = rawBsonDocumentCodec.decode(reader, decoderContext)
            val id = document.getString("id").value
            val latest = objectMapper.readValue(document.getString("latestVersion").value, object : TypeReference<Any>() {})
            val changelog = document.getArray("changelog").values.map {
                it as BsonDocument
                ChangelogEvent(
                        date = Date(it.getDateTime("date").value),
                        events = objectMapper.readValue(it.getString("changes").value, JsonPatch::class.java)
                )
            }
            AuditLog(id, latest, changelog)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}
