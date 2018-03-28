package koeln.niemeier.log4j2.json

import com.google.gson.Gson
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.temporal.ChronoField.YEAR

class LayoutTest {

    data class ExceptionEvent(
            val thrown: String,
            val message: String?,
            val stack: String
    )

    data class LogEvent(
            val time: String,
            val severity: String,
            val logger: String,
            val context: Map<String, String>?,
            val message: String,
            val thread: String,
            val exception: ExceptionEvent?
    )

    private val logger = LogManager.getLogger("logger")!!

    @BeforeEach
    fun reset() {
        logFile().writeText("")
    }

    @Test
    fun testEvents() {
        logger.info("one")
        logger.warn("two")
        logger.info("three")

        val events = logEvents()
        assertEquals(3, events.size)
    }

    @Test
    fun testTime() {
        logger.info("message")

        val events = logEvents()

        val value = events[0].time
        val time = ISO_DATE_TIME.parse(value)
        assertTrue(value.startsWith(time.get(YEAR).toString()))
    }

    @Test
    fun testSeverity() {
        logger.info("info")
        logger.warn("warn")

        val events = logEvents()
        assertEquals("INFO", events[0].severity)
        assertEquals("WARN", events[1].severity)
    }

    @Test
    fun testLogger() {
        logger.info("info")

        val events = logEvents()
        assertEquals("logger", events[0].logger)
    }

    @Test
    fun testContext() {
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
    fun testMessage() {
        logger.info("Hello {}", "world")

        val events = logEvents()
        assertEquals("Hello world", events[0].message)
    }

    @Test
    fun testThread() {
        logger.info("info");

        val events = logEvents()
        assertEquals(Thread.currentThread().name, events[0].thread)
    }

    @Test
    fun testNoException() {
        logger.info("info")

        val events = logEvents()
        assertNull(events[0].exception)
    }

    @Test
    fun testExceptionThrown() {
        try {
            throw IllegalStateException()
        } catch (e: IllegalStateException) {
            logger.info("error", e)
        }

        val events = logEvents()
        assertEquals(IllegalStateException::class.java.name, events[0].exception?.thrown ?: "missing")
    }

    @Test
    fun testExceptionMessage() {
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
    fun testExceptionStackTrace() {
        try {
            throw IllegalStateException("Outer", IllegalArgumentException("Inner"))
        } catch (e: IllegalStateException) {
            logger.info("error", e)
        }

        val events = logEvents()
        val positivePattern = ".*IllegalArgumentException: Inner.+IllegalStateException: Outer.+LayoutTest.+"

        val exception = events[0].exception!!
        assertTrue(exception.stack.contains(Regex(positivePattern, RegexOption.DOT_MATCHES_ALL)))

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
