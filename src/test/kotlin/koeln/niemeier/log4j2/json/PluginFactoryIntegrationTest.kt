package koeln.niemeier.log4j2.json

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class PluginFactoryIntegrationTest {

    private val logger = LoggerFactory.logAsConfigured("src/test/resources/test-configuration.xml")

    @BeforeEach
    fun reset() {
        logFile().writeText("")
    }

    @Test
    fun initializesPlugin() {
        logger.info("one")
        logger.warn("two")
        logger.info("three")

        val events = logEvents()
        assertEquals(3, events.size)
    }

    @Test
    fun ignoresPackagesAsConfigured() {
        try {
            throw IllegalStateException("Outer", IllegalArgumentException("Inner"))
        } catch (e: IllegalStateException) {
            logger.info("error", e)
        }

        val events = logEvents()
        val exception = events[0].exception!!

        val negativePattern = ".*(org\\.junit\\.|java\\.util\\.|org\\.gradle\\.).+"
        assertFalse(exception.stack.contains(Regex(negativePattern, RegexOption.DOT_MATCHES_ALL)))
    }

    private fun logEvents(): List<LogEvent> {
        return logFile().readLines()
                .map { it.trim('\u0000') }  // hack to mask null bytes of log file reset
                .map { Gson().fromJson(it, LogEvent::class.java) }
    }

    private fun logFile() = File("test-log.txt")

}
