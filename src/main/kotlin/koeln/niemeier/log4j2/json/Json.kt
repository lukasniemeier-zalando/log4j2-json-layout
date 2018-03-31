package koeln.niemeier.log4j2.json

internal class Json private constructor() {

    interface Starting {
        fun startObject(): Ongoing
        fun nullObject(): Finished
    }

    interface Ongoing {
        fun stringField(key: String, value: String): Ongoing
        fun stringFieldIf(condition: Boolean, key: String, value: () -> String): Ongoing
        fun objectField(key: String, value: (Starting) -> Finished): Ongoing
        fun objectFieldIf(condition: Boolean, key: String, value: (Starting) -> Finished): Ongoing
        fun endObject(): Finished
    }

    interface Finished {
        fun serialize(): String
    }

    companion object {
        fun generate(): Starting = Json.Generator()
    }

    private class Generator : Starting, Ongoing, Finished {

        private val builder: StringBuilder = StringBuilder()
        private var hasSibling = false

        override fun nullObject(): Finished {
            builder.append("null")
            return this
        }

        override fun startObject(): Ongoing {
            builder.append("{")
            return this
        }

        override fun endObject(): Finished {
            builder.append("}")
            return this
        }

        override fun stringField(key: String, value: String): Ongoing {
            writeSiblingSeparator()
            builder.append("\"${escape(key)}\":\"${escape(value)}\"")
            return this
        }

        override fun stringFieldIf(condition: Boolean, key: String, value: () -> String): Ongoing {
            if (condition) {
                stringField(key, value())
            }
            return this
        }

        override fun objectField(key: String, value: (Starting) -> Finished): Ongoing {
            writeSiblingSeparator()
            val fieldGenerator = Generator()
            builder.append("\"${escape(key)}\":${value(fieldGenerator).serialize()}")
            return this
        }

        override fun objectFieldIf(condition: Boolean, key: String, value: (Starting) -> Finished): Ongoing {
            if (condition) {
                objectField(key, value)
            }
            return this
        }


        override fun serialize() = builder.toString()

        private fun writeSiblingSeparator() {
            if (hasSibling) {
                builder.append(',')
            }
            hasSibling = true
        }

        private fun escape(value: String): String {
            return value.map(::escape).joinToString(separator = "")
        }

        // RFC 8259 7.
        // We don't escape extended characters
        private fun escape(value: Char): String {
            return when (value) {
                '"' -> "\\\""
                '\\' -> "\\\\"
                '\b' -> "\\b"
                '\u000C' -> "\\f"
                '\n' -> "\\n"
                '\r' -> "\\r"
                '\t' -> "\\t"
                else -> {
                    if (value.isISOControl() || value == '\u2028' || value == '\u2029') {
                        return String.format("\\u%04x", value.toInt())
                    }
                    value.toString()
                }
            }
        }

    }
}
