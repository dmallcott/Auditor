package com.dmallcott.auditor.data

import com.dmallcott.auditor.model.ChangelogEvent
import com.dmallcott.auditor.model.ChangelogItem
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.diff.JsonDiff


internal class Parser {
    private val mapper = jacksonObjectMapper()

    fun <T> asString(json: T): String = mapper.writeValueAsString(json)

    fun <T> asObject(json: String, clazz: Class<T>): T = mapper.readValue(json, clazz);

    fun <T> asNode(json: T): JsonNode = mapper.valueToTree(json)

    fun <T> asObject(node: JsonNode, clazz: Class<T>): T = mapper.treeToValue(node, clazz)

    fun <T> differences(original: T, new: T): JsonPatch {
        val originalNode = asNode(original)
        val newNode = asNode(new)

        return JsonDiff.asJsonPatch(originalNode, newNode)
    }

    fun <T> changelog(latest: T, events: List<ChangelogEvent>, clazz: Class<T>): List<ChangelogItem<T>> {
        val result = mutableListOf<ChangelogItem<T>>()
        var pointer = latest

        for (event in events) {
            pointer = asObject(event.events.apply(asNode(pointer)), clazz)
            result.add(ChangelogItem(pointer, event.actor, event.timestamp))
        }

        return result
    }
}