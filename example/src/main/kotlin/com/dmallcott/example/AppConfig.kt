package com.dmallcott.example

import com.dmallcott.auditor.AuditorImpl
import com.dmallcott.auditor.Parser
import com.dmallcott.auditor.Repository
import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AppConfig {

    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create(ConnectionString("mongodb://localhost:27017"))
    }

    // TODO remove these dependencies

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