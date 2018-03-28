package koeln.niemeier.log4j2.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule

internal class ObjectMapperFactory {

    fun createObjectMapper(ignoredPackages: List<String>) = ObjectMapper().registerModule(CustomModule(ignoredPackages))!!

    private class CustomModule(ignoredPackages: List<String>) : SimpleModule() {
        init {
            addSerializer(ContextDataSerializer())
            addSerializer(LogEventSerializer(ignoredPackages))
        }
    }
}
