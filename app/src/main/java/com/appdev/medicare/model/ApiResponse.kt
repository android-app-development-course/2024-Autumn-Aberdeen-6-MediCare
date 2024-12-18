package com.appdev.medicare.model

data class ApiResponse (
    val code: Int,
    val success: Boolean,
    val message: String,
    val data: JsonValue?,
    val error: RequestError?,
    val timestamp: Int
)

data class RequestError (
    val code: String,
    val description: String
)

sealed class JsonValue {
    data class JsonString(val value: String) : JsonValue()
    data class JsonNumber(val value: Number) : JsonValue()
    data class JsonBoolean(val value: Boolean) : JsonValue()
    data class JsonList(val value: List<JsonValue>) : JsonValue() {
        operator fun iterator(): Iterator<JsonValue> = value.iterator()
    }
    data class JsonObject(val value: Map<String, JsonValue>) : JsonValue()
    data object JsonNull : JsonValue()
}