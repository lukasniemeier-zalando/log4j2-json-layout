package koeln.niemeier.log4j2.json

import com.google.gson.Gson
import org.apache.logging.log4j.ThreadContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.CharArrayWriter
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

class PluginIntegrationTest {

    private val writer = CharArrayWriter()
    private val logger = LoggerFactory.logToWriter(writer)

    @Test
    fun eventPerLogMessage() {
        logger.info("one")
        logger.warn("two")
        logger.info("three")

        val events = logEvents()
        assertEquals(3, events.size)
    }

    @Test
    fun timeField() {
        logger.info("message")

        val events = logEvents()

        val value = events[0].time
        val time = DateTimeFormatter.ISO_DATE_TIME.parse(value)
        assertTrue(value.startsWith(time.get(ChronoField.YEAR).toString()))
    }

    @Test
    fun severityField() {
        logger.info("info")
        logger.warn("warn")

        val events = logEvents()
        assertEquals("INFO", events[0].severity)
        assertEquals("WARN", events[1].severity)
    }

    @Test
    fun loggerField() {
        logger.info("info")

        val events = logEvents()
        assertEquals(logger.name, events[0].logger)
    }

    @Test
    fun contextField() {
        logger.info("pre")
        ThreadContext.put("key", "value")
        ThreadContext.put("other", "thing")
        logger.info("post")
        ThreadContext.clearAll()

        val events = logEvents()
        assertNull(events[0].context)
        assertEquals(2, events[1].context?.size ?: -1)
        assertIterableEquals(setOf("key", "other"), events[1].context?.keys)
        assertIterableEquals(setOf("value", "thing"), events[1].context?.values)
    }

    @Test
    fun contextFieldNullKey() {
        ThreadContext.put(null, "value")
        logger.info("context")
        ThreadContext.clearAll()

        val events = logEvents()
        assertEquals(1, events[0].context?.size ?: -1)
        assertIterableEquals(setOf("null"), events[0].context?.keys)
        assertIterableEquals(setOf("value"), events[0].context?.values)
    }

    @Test
    fun contextFieldNullValue() {
        ThreadContext.put("key", null)
        logger.info("context")
        ThreadContext.clearAll()

        val events = logEvents()
        assertEquals(1, events[0].context?.size ?: -1)
        assertIterableEquals(setOf("key"), events[0].context?.keys)
        assertIterableEquals(setOf("null"), events[0].context?.values)
    }

    @Test
    fun messageField() {
        logger.info("Hello {}", "world")

        val events = logEvents()
        assertEquals("Hello world", events[0].message)
    }

    @Test
    fun threadField() {
        logger.info("info");

        val events = logEvents()
        assertEquals(Thread.currentThread().name, events[0].thread)
    }

    @Test
    fun noException() {
        logger.info("info")

        val events = logEvents()
        assertNull(events[0].exception)
    }

    @Test
    fun exceptionThrownField() {
        try {
            throw IllegalStateException()
        } catch (e: IllegalStateException) {
            logger.info("error", e)
        }

        val events = logEvents()
        assertEquals(IllegalStateException::class.java.name, events[0].exception?.thrown ?: "missing")
    }

    @Test
    fun exceptionMessageField() {
        try {
            throw IllegalStateException("message")
        } catch (e: IllegalStateException) {
            logger.info("error", e)
        }

        try {
            throw IllegalStateException()
        } catch (e: IllegalStateException) {
            logger.info("error", e)
        }

        val events = logEvents()
        assertEquals("message", events[0].exception?.message ?: "missing")
        assertNull(events[1].exception?.message)
    }

    @Test
    fun exceptionStackTraceField() {
        try {
            throw IllegalStateException("Outer", IllegalArgumentException("Inner"))
        } catch (e: IllegalStateException) {
            logger.info("error", e)
        }

        val events = logEvents()
        val exception = events[0].exception!!

        val pattern = ".*IllegalArgumentException: Inner.+IllegalStateException: Outer.+PluginIntegrationTest.+"
        assertTrue(exception.stack.contains(Regex(pattern, RegexOption.DOT_MATCHES_ALL)))
        val packagePattern = ".*(org\\.junit\\.|java\\.util\\.|org\\.gradle\\.).+"
        assertTrue(exception.stack.contains(Regex(packagePattern, RegexOption.DOT_MATCHES_ALL)))
    }

    @Test
    fun exceptionStackTraceFieldIgnoresPackages() {
        val ignoringLogger = LoggerFactory.logToWriter(writer, "org.junit.,java.util.,org.gradle.")
        try {
            throw IllegalStateException()
        } catch (e: IllegalStateException) {
            ignoringLogger.info("exceptionStackTraceFieldIgnoresPackages", e)
        }

        val events = logEvents()
        val exception = events[0].exception!!

        val packagePattern = ".*(org\\.junit\\.|java\\.util\\.|org\\.gradle\\.).+"
        assertFalse(exception.stack.contains(Regex(packagePattern, RegexOption.DOT_MATCHES_ALL)))
    }

    private fun logEvents(): List<LogEvent> {
        return writer.toString().trim().split("\r\n")
                .map { Gson().fromJson(it, LogEvent::class.java) }
    }

}
