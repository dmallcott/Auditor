package com.dmallcott.auditor.lib

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.diff.JsonDiff


class Parser {
    var mapper = ObjectMapper()

    fun <T> asNode(json: T): JsonNode = mapper.valueToTree(json)

    fun <T> differences(original: T, new: T): JsonPatch {
        val originalNode  = asNode(original)
        val newNode  = asNode(new)

        return JsonDiff.asJsonPatch(originalNode, newNode)
    }
}