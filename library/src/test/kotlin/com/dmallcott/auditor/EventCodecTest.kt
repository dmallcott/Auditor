package com.dmallcott.auditor

import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jackson.jsonpointer.JsonPointer
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.ReplaceOperation
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
import java.util.*


internal class EventCodecTest {

    private val mapper = jacksonObjectMapper()
    private val database: MongoDatabase = MongoClients.create(ConnectionString("mongodb://localhost:27017")).getDatabase("test")

    private val coolDb = database.withCodecRegistry(
            CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(EventCodec(mapper, database.codecRegistry)))
    ).getCollection(ChangelogEvent::class.java.simpleName)

    @Test
    internal fun `Can encode and decode events`() {
        val codec = EventCodec(mapper, coolDb.codecRegistry)
        val event = ChangelogEvent(Date(), JsonPatch(listOf(ReplaceOperation(JsonPointer("/amount"), DoubleNode(10.0)))))

        val buffer = BasicOutputBuffer()
        val writer = BsonBinaryWriter(buffer)
        codec.encode(writer, event, EncoderContext.builder().build())

        val reader = BsonBinaryReader(ByteBufferBsonInput(ByteBufNIO(ByteBuffer.wrap(buffer.internalBuffer))))
        val result: ChangelogEvent = codec.decode(reader, DecoderContext.builder().build())

        Assertions.assertNotNull(result)
        Assertions.assertEquals(event.date, result.date)
        Assertions.assertEquals(event.events.toString(), result.events.toString())
    }

}