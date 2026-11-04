package com.pilotprep.app.util

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Structured logger emitting single-line JSON logs for easier parsing/filtering.
 */
@Singleton
class StructuredLogger @Inject constructor() {

    enum class Level { DEBUG, INFO, WARN, ERROR, WTF }

    fun debug(context: String, message: String, fields: Map<String, Any?> = emptyMap()) =
        log(Level.DEBUG, context, message, fields)

    fun info(context: String, message: String, fields: Map<String, Any?> = emptyMap()) =
        log(Level.INFO, context, message, fields)

    fun warn(context: String, message: String, fields: Map<String, Any?> = emptyMap()) =
        log(Level.WARN, context, message, fields)

    fun error(context: String, message: String, fields: Map<String, Any?> = emptyMap(), throwable: Throwable? = null) =
        log(Level.ERROR, context, message, fields, throwable)

    fun wtf(context: String, message: String, fields: Map<String, Any?> = emptyMap(), throwable: Throwable? = null) =
        log(Level.WTF, context, message, fields, throwable)

    private fun log(
        level: Level,
        context: String,
        message: String,
        fields: Map<String, Any?>,
        throwable: Throwable? = null
    ) {
        val json = buildJson(level, context, message, fields, throwable)
        val tag = "PP-LOG"
        when (level) {
            Level.DEBUG -> Log.d(tag, json)
            Level.INFO -> Log.i(tag, json)
            Level.WARN -> Log.w(tag, json)
            Level.ERROR -> Log.e(tag, json, throwable)
            Level.WTF -> Log.wtf(tag, json, throwable)
        }
    }

    private fun buildJson(
        level: Level,
        context: String,
        message: String,
        fields: Map<String, Any?>,
        throwable: Throwable?
    ): String {
        val sb = StringBuilder(256)
        sb.append('{')
        appendField(sb, "ts", System.currentTimeMillis())
        sb.append(',')
        appendField(sb, "level", level.name)
        sb.append(',')
        appendField(sb, "context", context)
        sb.append(',')
        appendField(sb, "message", message)
        if (fields.isNotEmpty()) {
            sb.append(',')
            sb.append("\"fields\":{")
            var first = true
            for ((k, v) in fields) {
                if (!first) sb.append(',') else first = false
                appendKeyValue(sb, k, v)
            }
            sb.append('}')
        }
        if (throwable != null) {
            sb.append(',')
            appendField(sb, "error", throwable.javaClass.simpleName + ": " + (throwable.message ?: ""))
        }
        sb.append('}')
        return sb.toString()
    }

    private fun appendField(sb: StringBuilder, key: String, value: Any?) {
        sb.append('"').append(escape(key)).append('"').append(':')
        appendValue(sb, value)
    }

    private fun appendKeyValue(sb: StringBuilder, key: String, value: Any?) {
        sb.append('"').append(escape(key)).append('"').append(':')
        appendValue(sb, value)
    }

    private fun appendValue(sb: StringBuilder, value: Any?) {
        when (value) {
            null -> sb.append("null")
            is Number, is Boolean -> sb.append(value.toString())
            else -> sb.append('"').append(escape(value.toString())).append('"')
        }
    }

    private fun escape(s: String): String =
        s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}

