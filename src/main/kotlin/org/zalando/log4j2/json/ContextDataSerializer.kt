package org.zalando.log4j2.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.apache.logging.log4j.util.ReadOnlyStringMap

internal class ContextDataSerializer : StdSerializer<ReadOnlyStringMap>(ReadOnlyStringMap::class.java) {

    override fun serialize(value: ReadOnlyStringMap, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeStartObject()
        value.forEach { k: String, v: Any ->
            generator.writeStringField(k, v.toString())
        }
        generator.writeEndObject()
    }
}
