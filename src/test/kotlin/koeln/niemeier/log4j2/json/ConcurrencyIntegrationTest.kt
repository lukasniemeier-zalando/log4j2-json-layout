package koeln.niemeier.log4j2.json

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Collections
import java.util.stream.Collectors.joining
import kotlin.concurrent.thread


/**
 * Testing LOG4J2-1769
 * See ConcurrentLoggingWithJsonLayoutTest (log4j 2.8)
 */
class ConcurrencyIntegrationTest {

    private val logger = LoggerFactory.logAsConfigured("test-configuration.xml")

    @BeforeEach
    fun reset() {
        logFile().writeText("")
    }

    @Test
    fun logsConcurrently() {
        val exceptions = Collections.synchronizedList(mutableListOf<Throwable>())
        val threads = Collections.synchronizedSet(mutableSetOf<Thread>())

        val numberOfThreads = Runtime.getRuntime().availableProcessors() * 8
        val numberOfMessages = 64

        repeat(numberOfThreads) {
            val thread = thread(start = false) {
                try {
                    repeat(numberOfMessages) {
                        logger.info("First message.")
                        logger.info("Second message.")
                    }
                } finally {
                    threads.remove(Thread.currentThread())
                }
            }
            thread.setUncaughtExceptionHandler { _, e ->
                exceptions.add(e)
            }
            threads.add(thread)
            thread.start()
        }

        while (!threads.isEmpty()) {
            Thread.sleep(10)
        }

        assertTrue(exceptions.isEmpty()) {
            exceptions.stream().map { e -> e.message }.collect(joining("\n"))
        }
        assertEquals(numberOfThreads * numberOfMessages * 2, logEvents().size)
    }

    private fun logEvents(): List<LogEvent> {
        return logFile().readLines()
                .map { it.trim('\u0000') }  // hack to mask null bytes of log file reset
                .map { Gson().fromJson(it, LogEvent::class.java) }
    }

    private fun logFile() = File("test-log.txt")

}
