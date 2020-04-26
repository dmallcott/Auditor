package com.dmallcott.auditor.lib

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.diff.JsonDiff


class Parser {
    private var mapper = jacksonObjectMapper()

    fun <T> asNode(json: T): JsonNode = mapper.valueToTree(json)

    fun <T> asObject(node: JsonNode, clazz: Class<T>): T = mapper.treeToValue(node, clazz)

    fun <T> differences(original: T, new: T): JsonPatch {
        val originalNode = asNode(original)
        val newNode = asNode(new)

        return JsonDiff.asJsonPatch(originalNode, newNode)
    }

    fun <T> changelog(latest: T, patches: List<JsonPatch>, clazz: Class<T>): List<T> {
        val result = mutableListOf(latest)
        var pointer = latest

        for (patch in patches) {
            pointer = asObject(patch.apply(asNode(pointer)), clazz)
            result.add(pointer)
        }

        return result
    }
}