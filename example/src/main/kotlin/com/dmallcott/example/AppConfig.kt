package com.dmallcott.example

import com.dmallcott.auditor.Auditor
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

    @Bean
    fun auditor(client: MongoClient): Auditor {
        return Auditor(client.getDatabase("audits"))
    }
}