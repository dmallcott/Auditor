package com.dmallcott.auditor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication



@SpringBootApplication
class AuditorApplication

fun main(args: Array<String>) {
	runApplication<AuditorApplication>(*args)
}
