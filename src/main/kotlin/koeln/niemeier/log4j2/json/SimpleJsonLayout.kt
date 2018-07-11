package koeln.niemeier.log4j2.json

import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.layout.AbstractStringLayout
import org.apache.logging.log4j.core.layout.ByteBufferDestination
import org.apache.logging.log4j.core.util.StringBuilderWriter

/**
 * A layout rendering structured JSON log lines.
 */
@Plugin(name = "SimpleJsonLayout", category = "Core", elementType = "layout", printObject = true)
class SimpleJsonLayout(config: Configuration?, ignoredPackages: List<String>)
    : AbstractStringLayout(config, Charsets.UTF_8, null, null) {

    /**
     * Factory object used to generate new SimpleJsonLayout.
     */
    companion object {

        /**
         * Creates a SimpleJsonLayout.
         * @param[config] The plugin configuration.
         * @param[ignoredStackTracePackages] A comma-separated list of packages to be ignored on rendering the stack trace.
         * @return A SimpleJsonLayout.
         */
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

    private val serializer = LogEventSerializer(ignoredPackages)

    /**
     * @return The JSON content type.
     */
    override fun getContentType() = "application/json; charset=$charset"

    /**
     * Formats the event as a JSON string.
     */
    override fun toSerializable(event: LogEvent?): String {
        val writer = StringBuilderWriter()
        writer.write(serializer.serialize(event))
        writer.write("\r\n")
        markEvent()
        return writer.toString()
    }

    override fun encode(event: LogEvent?, destination: ByteBufferDestination) {
        val data = toByteArray(event)
        synchronized(destination) {
            writeTo(data, 0, data.size, destination)
        }
    }
}
