package org.zalando.log4j2.json

import com.fasterxml.jackson.databind.ObjectWriter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.impl.MutableLogEvent
import org.apache.logging.log4j.core.layout.AbstractLayout
import org.apache.logging.log4j.core.layout.AbstractStringLayout
import org.apache.logging.log4j.core.util.StringBuilderWriter
import org.apache.logging.log4j.util.Strings
import java.io.IOException
import java.io.Writer

@Plugin(name = "SimpleJsonLayout", category = "Core", elementType = "layout", printObject = true)
class SimpleJsonLayout(config: Configuration?, ignoredPackages: List<String>)
    : AbstractStringLayout(config, Charsets.UTF_8, null, null) {

    companion object {
        @JvmStatic
        @PluginFactory
        fun createLayout(
                @PluginConfiguration config: Configuration?,
                @PluginAttribute("ignoredStackTracePackages") ignoredStackTracePackages: String?
        ): SimpleJsonLayout {
            val ignoredPackages = ignoredStackTracePackages
                    ?.split(",")
                    ?.map(String::trim)
                    ?.filter(String::isNotEmpty)
                    ?: emptyList()
            return SimpleJsonLayout(config, ignoredPackages)
        }
    }

    private val objectWriter: ObjectWriter = ObjectMapperFactory().createObjectMapper(ignoredPackages).writer()

    override fun getContentType() = "application/json; charset=$charset"

    // Stolen from JsonLayout (AbstractJacksonLayout)
    override fun toSerializable(event: LogEvent?): String {
        val writer = StringBuilderWriter()
        return try {
            toSerializable(event, writer)
            writer.toString()
        } catch (e: IOException) {
            // Should this be an ISE or IAE?
            AbstractLayout.LOGGER.error(e)
            Strings.EMPTY
        }
    }

    // Stolen from JsonLayout (AbstractJacksonLayout)
    private fun convertMutableToLog4jEvent(event: LogEvent?): LogEvent? {
        return if (event is MutableLogEvent)
            event.createMemento()
        else
            event
    }

    // Stolen from JsonLayout (AbstractJacksonLayout)
    private fun toSerializable(event: LogEvent?, writer: Writer) {
        objectWriter.writeValue(writer, convertMutableToLog4jEvent(event))
        writer.write("\r\n")
        markEvent()
    }

}
