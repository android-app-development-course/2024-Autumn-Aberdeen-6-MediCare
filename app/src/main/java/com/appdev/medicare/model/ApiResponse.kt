package com.appdev.medicare.model

data class ApiResponse (
    val code: Int,
    val success: Boolean,
    val message: String,
    val data: JsonValue?,
    val err: RequestError?,
    val timestamp: String
)

data class RequestError (
    val code: String,
    val description: String
)

sealed class JsonValue {
    data class JsonString(val value: String) : JsonValue()
    data class JsonNumber(val value: Number) : JsonValue()
    data class JsonBoolean(val value: Boolean) : JsonValue()
    data class JsonList(val value: List<JsonValue>) : JsonValue()
    data class JsonObject(val value: Map<String, JsonValue>) : JsonValue()
    data object JsonNull : JsonValue()
}