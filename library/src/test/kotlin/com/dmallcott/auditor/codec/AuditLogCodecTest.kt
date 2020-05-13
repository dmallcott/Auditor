package com.dmallcott.auditor.codec

import com.dmallcott.auditor.AuditLog
import com.dmallcott.auditor.ChangelogEvent
import com.dmallcott.auditor.getQuote
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.BsonBinaryReader
import org.bson.BsonBinaryWriter
import org.bson.ByteBufNIO
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.io.BasicOutputBuffer
import org.bson.io.ByteBufferBsonInput
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

internal class AuditLogCodecTest {

    private val mapper = jacksonObjectMapper()
    private val database: MongoDatabase = MongoClients.create(ConnectionString("mongodb://localhost:27017")).getDatabase("test")

    private val coolDb = database.withCodecRegistry(
            CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromCodecs(AuditLogCodec(mapper, database.codecRegistry)))
    ).getCollection(ChangelogEvent::class.java.simpleName)

    @Test
    internal fun `Can encode and decode events`() {
        val codec = AuditLogCodec(mapper, coolDb.codecRegistry)
        val quote = getQuote()
        val log = AuditLog(quote.id.id, quote, emptyList())


        val buffer = BasicOutputBuffer()
        val writer = BsonBinaryWriter(buffer)
        codec.encode(writer, log, EncoderContext.builder().build())

        val reader = BsonBinaryReader(ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(buffer.internalBuffer))))
        val result: AuditLog<Any> = codec.decode(reader, DecoderContext.builder().build())

        Assertions.assertNotNull(result)
        Assertions.assertEquals(result.logId, quote.id.id)
        //Assertions.assertEquals(result.latestVersion, quote)
    }
}