package koeln.niemeier.log4j2.json

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
