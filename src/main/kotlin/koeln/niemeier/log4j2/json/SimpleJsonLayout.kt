package koeln.niemeier.log4j2.json

import com.fasterxml.jackson.databind.ObjectWriter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.layout.AbstractLayout
import org.apache.logging.log4j.core.layout.AbstractStringLayout
import org.apache.logging.log4j.core.util.StringBuilderWriter
import org.apache.logging.log4j.util.Strings
import java.io.IOException

/**
 * A layout rendering structured JSON log lines.
 */
@Plugin(name = "SimpleJsonLayout", category = "Core", elementType = "layout", printObject = true)
class SimpleJsonLayout(config: Configuration?, ignoredPackages: List<String>)
    : AbstractStringLayout(config, Charsets.UTF_8, null, null) {

    /**
     * Factory object used to create new SimpleJsonLayout.
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

    private val objectWriter: ObjectWriter = ObjectMapperFactory().createObjectMapper(ignoredPackages).writer()

    /**
     * @return The JSON content type.
     */
    override fun getContentType() = "application/json; charset=$charset"

    /**
     * Formats the event as a JSON string.
     *
     * Remark: Stolen from JsonLayout (AbstractJacksonLayout)
     */
    override fun toSerializable(event: LogEvent?): String {
        val writer = StringBuilderWriter()
        return try {
            objectWriter.writeValue(writer, event)
            writer.write("\r\n")
            markEvent()
            writer.toString()
        } catch (e: IOException) {
            // Should this be an ISE or IAE?
            AbstractLayout.LOGGER.error(e)
            Strings.EMPTY
        }
    }

}
