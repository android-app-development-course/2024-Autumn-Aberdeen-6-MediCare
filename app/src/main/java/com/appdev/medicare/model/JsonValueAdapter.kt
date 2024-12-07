package com.appdev.medicare.model

import com.google.gson.*
import java.lang.reflect.Type

class JsonValueAdapter : JsonDeserializer<JsonValue>, JsonSerializer<JsonValue> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): JsonValue {
        return when {
            json.isJsonNull -> JsonValue.JsonNull
            json.isJsonPrimitive -> {
                val primitive = json.asJsonPrimitive
                when {
                    primitive.isString -> JsonValue.JsonString(primitive.asString)
                    primitive.isNumber -> JsonValue.JsonNumber(primitive.asNumber)
                    primitive.isBoolean -> JsonValue.JsonBoolean(primitive.asBoolean)
                    else -> throw JsonParseException("Unsupported JSON primitive: $primitive")
                }
            }
            json.isJsonArray -> {
                val list = json.asJsonArray.map { context.deserialize<JsonValue>(it, JsonValue::class.java)}
                JsonValue.JsonList(list)
            }
            json.isJsonObject -> {
                val map = json.asJsonObject.entrySet().associate { it.key to context.deserialize<JsonValue>(it.value, JsonValue::class.java) }
                JsonValue.JsonObject(map)
            }
            else -> throw JsonParseException("Unsupported JSON element: $json")
        }
    }

    override fun serialize(
        src: JsonValue,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return when (src) {
            is JsonValue.JsonString -> JsonPrimitive(src.value)
            is JsonValue.JsonNumber -> JsonPrimitive(src.value)
            is JsonValue.JsonBoolean -> JsonPrimitive(src.value)
            is JsonValue.JsonList -> JsonArray().apply { src.value.forEach { add(context.serialize(it)) } }
            is JsonValue.JsonObject -> JsonObject().apply { src.value.forEach { add(it.key, context.serialize(it.value)) } }
            JsonValue.JsonNull -> JsonNull.INSTANCE
        }
    }
}