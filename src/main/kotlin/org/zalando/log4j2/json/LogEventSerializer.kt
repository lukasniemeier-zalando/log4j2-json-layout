package org.zalando.log4j2.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.impl.ThrowableProxy
import java.time.Instant


internal class LogEventSerializer(private val ignoredPackages: List<String>) : StdSerializer<LogEvent>(LogEvent::class.java) {

    override fun serialize(value: LogEvent?, generator: JsonGenerator, provider: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
            return
        }

        generator.writeStartObject()
        generator.writeStringField("time", Instant.ofEpochMilli(value.timeMillis).toString())
        generator.writeStringField("severity", value.level.name())
        generator.writeStringField("logger", value.loggerName)
        if (!value.contextData.isEmpty) {
            generator.writeObjectField("context", value.contextData)
        }
        generator.writeStringField("message", value.message.formattedMessage)
        value.thrownProxy?.let {
            serializeThrowable(it, generator)
        }
        generator.writeStringField("thread", value.threadName)
        generator.writeEndObject()
    }

    private fun serializeThrowable(proxy: ThrowableProxy, generator: JsonGenerator) {
        generator.writeObjectFieldStart("exception")

        proxy.throwable?.let {
            generator.writeStringField("thrown", it.javaClass.name)
            it.message?.let {
                generator.writeStringField("message", it)
            }
        }
        val trace = proxy.getCauseStackTraceAsString(ignoredPackages)
        generator.writeStringField("stack", trace)

        generator.writeEndObject()
    }

}
