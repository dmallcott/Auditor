package com.dmallcott.auditor

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

class EventCodec(private val objectMapper: ObjectMapper,
                 codecRegistry: CodecRegistry) : Codec<ChangelogEvent> {

    private val rawBsonDocumentCodec: Codec<BsonDocument> = codecRegistry.get(BsonDocument::class.java)

    override fun getEncoderClass(): Class<ChangelogEvent> {
        return ChangelogEvent::class.java
    }

    override fun encode(writer: BsonWriter, value: ChangelogEvent, encoderContext: EncoderContext) {
        try {
            val json: String = objectMapper.writeValueAsString(value.events)

            val doc = BsonDocument(listOf(
                    BsonElement("date", BsonDateTime(value.date.time)),
//                    BsonElement("changes", RawBsonArray.parse(json))
                    BsonElement("changes", BsonString(json))
            ))

            rawBsonDocumentCodec.encode(writer, doc, encoderContext)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): ChangelogEvent {
        return try {
            val document = rawBsonDocumentCodec.decode(reader, decoderContext)
            val date = Date(document.getDateTime("date").value)
            val changes = objectMapper.readValue(document.getString("changes").value, JsonPatch::class.java)

            ChangelogEvent(date, changes)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}