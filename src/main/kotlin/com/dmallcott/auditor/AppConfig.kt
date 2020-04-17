package com.dmallcott.auditor

import com.dmallcott.auditor.lib.AuditorImpl
import com.dmallcott.auditor.lib.Parser
import com.dmallcott.auditor.lib.Repository
import com.github.fge.jsonpatch.JsonPatch
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.ClassModel
import org.bson.codecs.pojo.PojoCodecProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AppConfig {

    @Bean
    fun mongoClient(): MongoClient {
        val quoteClassModel: ClassModel<Quote> = ClassModel.builder(Quote::class.java).enableDiscriminator(false).build()
        val quoteIdClassModel: ClassModel<QuoteId> = ClassModel.builder(QuoteId::class.java).enableDiscriminator(false).build()
        val jsonClassModel: ClassModel<JsonPatch> = ClassModel.builder(JsonPatch::class.java).enableDiscriminator(false).build()
        val pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().register(quoteClassModel, quoteIdClassModel, jsonClassModel).build())
        )

        return MongoClients.create(
                MongoClientSettings.builder()
                        .codecRegistry(pojoCodecRegistry)
                        .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
                        .build()
        )


/*        val pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()))
        val settings: MongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
                .codecRegistry(pojoCodecRegistry)
                .build()
        return MongoClients.create(settings)*/
    }

    /*fun test() {
        val scanner = ClassPathScanningCandidateComponentProvider(true);

        scanner.addIncludeFilter(AnnotationTypeFilter(InheritMe::class.java))

        for (bd in scanner.findCandidateComponents("com.dmallcott.auditor")) {
            bd.cla
        }
        System.out.println(bd.getBeanClassName());
    }*/

    @Bean
    fun repository(mongoClient: MongoClient): Repository {
        return Repository(mongoClient.getDatabase("auditor"))
    }

    @Bean
    fun parser(): Parser {
        return Parser()
    }

    @Bean
    fun auditor(parser: Parser, repository: Repository): AuditorImpl {
        return AuditorImpl(parser, repository)
    }
}