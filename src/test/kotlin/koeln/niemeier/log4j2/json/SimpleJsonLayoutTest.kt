package koeln.niemeier.log4j2.json

import koeln.niemeier.log4j2.json.SimpleJsonLayout.Companion.createLayout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SimpleJsonLayoutTest {

    private val unit = SimpleJsonLayout(null, listOf())

    @Test
    fun usesUtf8() {
        assertEquals(Charsets.UTF_8, unit.charset)
    }

    @Test
    fun useApplicationJsonUtf8() {
        assertEquals("application/json; charset=UTF-8", unit.contentType)
    }

    @Test
    fun defensiveConfigurationHandling() {
        assertNotNull(createLayout(null, ""))
        assertNotNull(createLayout(null, "  ,"))
        assertNotNull(createLayout(null, null))
    }

    @Test
    fun noHeader() {
        assertNull(unit.header)
    }

    @Test
    fun noFooter() {
        assertNull(unit.footer)
    }

    @Test
    fun newLineAfterEachEvent() {
        val serialized = unit.toSerializable(null)
        assertTrue(serialized.endsWith("\r\n"))
    }
}
