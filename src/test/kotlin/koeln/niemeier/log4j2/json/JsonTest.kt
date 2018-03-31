package koeln.niemeier.log4j2.json

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonTest {

    private val unit = Json.generate()

    @Test
    fun emptyObject() {
        assertEquals("{}", unit.startObject().endObject().serialize())
    }

    @Test
    fun nullObject() {
        assertEquals("null", unit.nullObject().serialize())
    }

    @Test
    fun singleStringField() {
        assertEquals("""{"key":"value"}""",
                unit.startObject()
                        .stringField("key", "value")
                        .endObject()
                        .serialize())
    }

    @Test
    fun singleNullObjectField() {
        assertEquals("""{"key":null}""",
                unit.startObject()
                        .objectField("key", { it.nullObject() })
                        .endObject()
                        .serialize())
    }

    @Test
    fun multiStringFields() {
        assertEquals("""{"key1":"value1","key2":"value2"}""",
                unit.startObject()
                        .stringField("key1", "value1")
                        .stringField("key2", "value2")
                        .endObject()
                        .serialize())
    }

    @Test
    fun complexObject() {
        assertEquals("""{"key0":{"key1":"value1","key2":"value2","key3":null},"key4":"value4"}""",
                unit.startObject()
                        .objectField("key0", {
                            it.startObject()
                                    .stringField("key1", "value1")
                                    .stringField("key2", "value2")
                                    .objectField("key3", {
                                        it.nullObject()
                                    })
                                    .endObject()
                        })
                        .stringField("key4", "value4")
                        .endObject()
                        .serialize())
    }

    @Test
    fun generalEscaping() {
        assertEquals("""{"key\\":"value\""}""",
                unit.startObject()
                        .stringField("key\\", "value\"")
                        .endObject()
                        .serialize())
    }

    @Test
    fun controlCharEscaping() {
        assertEquals("""{"key\u0007":"\tHello\f\n\rWorld\b"}""",
                unit.startObject()
                        .stringField("key\u0007", "\tHello\u000C\n\rWorld\b")
                        .endObject()
                        .serialize())
    }

    @Test
    fun lineSeparatorEscaping() {
        assertEquals("""{"\u2028":"\u2029"}""",
                unit.startObject()
                        .stringField("\u2028", "\u2029")
                        .endObject()
                        .serialize())
    }

    @Test
    fun unicodeString() {
        assertEquals("""{"key":"𝄞"}""",
                unit.startObject()
                        .stringField("key", "𝄞")
                        .endObject()
                        .serialize())
    }

}
