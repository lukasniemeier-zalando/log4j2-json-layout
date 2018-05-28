package koeln.niemeier.log4j2.json

import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.impl.ThrowableProxy
import org.apache.logging.log4j.util.ReadOnlyStringMap
import java.time.Instant


internal class LogEventSerializer(private val ignoredPackages: List<String>) {

    fun serialize(event: LogEvent?): String {
        val json = Json.generate()
        if (event == null) {
            return json.nullObject().serialize()
        }

        return json.startObject()
                .stringField("time", Instant.ofEpochMilli(event.timeMillis).toString())
                .stringField("severity", event.level.name())
                .stringField("logger", event.loggerName)
                .objectFieldIf(!event.contextData.isEmpty, "context") { serializeContextData(it, event.contextData) }
                .stringField("message", event.message.formattedMessage)
                .objectFieldIf(event.thrownProxy != null, "exception") { serializeException(it, event.thrownProxy) }
                .stringField("thread", event.threadName)
                .endObject()
                .serialize()
    }

    private fun serializeContextData(generator: Json.Starting, value: ReadOnlyStringMap): Json.Finished {
        val json = generator.startObject()
        value.forEach { k: String?, v: Any? ->
            json.stringField(k.toString(), v.toString())
        }
        return json.endObject()
    }

    private fun serializeException(generator: Json.Starting, value: ThrowableProxy): Json.Finished {
        return generator.startObject()
                .stringField("thrown", value.throwable.javaClass.name)
                .stringFieldIf(value.message != null, "message") { value.message }
                .stringField("stack", value.getCauseStackTraceAsString(ignoredPackages))
                .endObject()
    }

}
