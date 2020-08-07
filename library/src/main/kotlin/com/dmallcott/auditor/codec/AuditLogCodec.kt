package com.dmallcott.auditor.codec

import com.dmallcott.auditor.model.AuditLog
import com.dmallcott.auditor.model.ChangelogEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import org.bson.*
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import java.io.IOException
import java.io.UncheckedIOException
import java.time.Instant
import java.util.*


class AuditLogCodec(private val objectMapper: ObjectMapper,
                       codecRegistry: CodecRegistry) : Codec<AuditLog> {

    private val rawBsonDocumentCodec: Codec<BsonDocument> = codecRegistry.get(BsonDocument::class.java)

    override fun getEncoderClass(): Class<AuditLog> {
        return AuditLog::class.java
    }

    override fun encode(writer: BsonWriter, value: AuditLog, encoderContext: EncoderContext) {
        try {
            val latestAsString: String = objectMapper.writeValueAsString(value.latestVersion)

            val changelog = value.changelog.map {
                BsonDocument(listOf(
                        BsonElement("date", BsonDateTime(it.timestamp.toEpochMilli())),
                        BsonElement("actor", BsonString(it.actor)),
                        BsonElement("events", BsonString(objectMapper.writeValueAsString(it.events)))
                ))
            }
            // TODO generify
            val doc = BsonDocument(listOf(
                    BsonElement("_id", BsonString(value.logId)),
                    BsonElement("latestVersion", BsonString(latestAsString)),
                    BsonElement("changelog", BsonArray(changelog)),
                    BsonElement("lastUpdated", BsonDateTime(value.created.time))
            ))

            rawBsonDocumentCodec.encode(writer, doc, encoderContext)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): AuditLog {
        return try {
            val document = rawBsonDocumentCodec.decode(reader, decoderContext)
            val id = document.getString("_id").value
            val latest = objectMapper.readValue(document.getString("latestVersion").value, String::class.java)
            val changelog = document.getArray("changelog").values.map {
                it as BsonDocument
                ChangelogEvent(
                        timestamp = Instant.ofEpochMilli(it.getDateTime("date").value),
                        actor = it.getString("actor").value,
                        events = objectMapper.readValue(it.getString("events").value, JsonPatch::class.java)
                )
            }
            val lastUpdated = Date(document.getDateTime("lastUpdated").value)
            AuditLog(id, latest, changelog, lastUpdated)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}
