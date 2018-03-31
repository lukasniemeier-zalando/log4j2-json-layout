package koeln.niemeier.log4j2.json

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LogEventSerializerTest {

    private val unit = LogEventSerializer(listOf())

    @Test
    fun nullEvent() {
        assertEquals("null", unit.serialize(null))
    }
}
