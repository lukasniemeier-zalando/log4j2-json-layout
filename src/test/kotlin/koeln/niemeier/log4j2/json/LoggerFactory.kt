package koeln.niemeier.log4j2.json

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.WriterAppender
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import java.io.Writer
import java.util.UUID

class LoggerFactory {

    companion object {

        fun logToWriter(writer: Writer, ignoredPackages: String? = null): Logger {
            val builder = ConfigurationBuilderFactory.newConfigurationBuilder()
            val configuration = builder
                    .setStatusLevel(Level.INFO)
                    .setPackages(SimpleJsonLayout::class.java.`package`.name)
                    .setConfigurationName(UUID.randomUUID().toString())
                    .add(builder.newRootLogger(Level.INFO))
                    .build(false)
            val context = Configurator.initialize(configuration)
            val logger = context.getLogger(UUID.randomUUID().toString())

            val appender = WriterAppender.newBuilder()
                    .setName(UUID.randomUUID().toString())
                    .setLayout(SimpleJsonLayout.createLayout(null, ignoredPackages))
                    .setTarget(writer)
                    .build()
            appender.start()
            logger.addAppender(appender)
            return logger
        }

        fun logAsConfigured(pathToConfig: String): Logger {
            return Configurator
                    .initialize("test-context", pathToConfig)
                    .getLogger("logger")
        }

    }

}
