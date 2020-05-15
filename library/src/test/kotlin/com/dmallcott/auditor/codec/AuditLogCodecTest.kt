package com.dmallcott.auditor.codec

import com.dmallcott.auditor.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.MongoClientSettings
import org.bson.BsonBinaryReader
import org.bson.BsonBinaryWriter
import org.bson.ByteBufNIO
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.io.BasicOutputBuffer
import org.bson.io.ByteBufferBsonInput
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.*

internal class AuditLogCodecTest {

    private val mapper = jacksonObjectMapper()

    @Test
    internal fun `Can encode and decode events`() {
        val quote = getQuote()

        val log = AuditLog(quote.id.id, mapper.writeValueAsString(quote), emptyList(), Date())
        val codec = AuditLogCodec(mapper, MongoClientSettings.getDefaultCodecRegistry())

        val buffer = BasicOutputBuffer()
        val writer = BsonBinaryWriter(buffer)
        codec.encode(writer, log, EncoderContext.builder().build())

        val reader = BsonBinaryReader(ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(buffer.internalBuffer))))
        val result: AuditLog = codec.decode(reader, DecoderContext.builder().build())

        Assertions.assertNotNull(result)
        Assertions.assertEquals(result.logId, quote.id.id)
        Assertions.assertEquals(mapper.readValue(result.latestVersion, Quote::class.java), quote)
    }

    @Test
    internal fun `Can encode and decode events with changes`() {
        val quote = getQuote()
        val newQuote = quote.copy(amount = quote.amount + 10.0)
        val patch = changeAmountPatch(amount = newQuote.amount)

        val newLog = AuditLog(quote.id.id, mapper.writeValueAsString(newQuote), listOf(ChangelogEvent(Date(1588430942), patch)), Date())

        val log = AuditLog(quote.id.id, mapper.writeValueAsString(quote), emptyList(), Date())
        val codec = AuditLogCodec(mapper, MongoClientSettings.getDefaultCodecRegistry())

        val buffer = BasicOutputBuffer()
        val writer = BsonBinaryWriter(buffer)
        codec.encode(writer, newLog, EncoderContext.builder().build())

        val reader = BsonBinaryReader(ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(buffer.internalBuffer))))
        val result: AuditLog = codec.decode(reader, DecoderContext.builder().build())

        Assertions.assertNotNull(result)
        Assertions.assertEquals(result.logId, quote.id.id)
        Assertions.assertEquals(mapper.readValue(result.latestVersion, Quote::class.java), newQuote)
    }
}