package org.gravidence.gravifon.configuration

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.jul.LevelChangePropagator
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.util.FileSize
import java.nio.file.Path
import kotlin.io.path.createDirectories

class LogbackConfigurator : ContextAwareBase(), Configurator {

    private val logDir: Path = ConfigUtil.configHomeDir.resolve("log")
    private val logFile: Path = logDir.resolve("gravifon.log")

    /**
     * Pattern for log file [rollover](https://logback.qos.ch/manual/appenders.html#TimeBasedRollingPolicy).
     */
    private val logFileArchivePattern: Path = logDir.resolve("gravifon.%d{yyyy-MM-dd}.gz")

    init {
        logDir.createDirectories()
    }

    override fun configure(lc: LoggerContext?) {
        addInfo("Setting up default configuration.")

        if (lc != null) {
            resetJUL(lc)

            val gravidenceLogger = lc.getLogger("org.gravidence")
            gravidenceLogger.level = Level.DEBUG

            val rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
            rootLogger.level = Level.WARN
            rootLogger.addAppender(consoleAppender(lc))
            rootLogger.addAppender(fileAppender(lc))
        }
    }

    /**
     * Enable performance optimization for [JUL](https://www.slf4j.org/legacy.html#jul-to-slf4j), which is used by some dependencies.
     * It is done via [LevelChangePropagator](https://logback.qos.ch/manual/configuration.html#LevelChangePropagator) listener.
     */
    private fun resetJUL(context: LoggerContext) {
        val levelChangePropagator = LevelChangePropagator()
        levelChangePropagator.context = context
        levelChangePropagator.setResetJUL(true)
        levelChangePropagator.start()

        context.addListener(levelChangePropagator)
    }

    private fun consoleAppender(context: LoggerContext): ConsoleAppender<ILoggingEvent> {
        val layout = PatternLayout()
        val encoder = LayoutWrappingEncoder<ILoggingEvent>()
        val appender = ConsoleAppender<ILoggingEvent>()

        layout.pattern = PATTERN
        layout.context = context
        layout.start()

        encoder.layout = layout
        encoder.context = context
        encoder.start()

        appender.name = "STDOUT"
        appender.encoder = encoder
        appender.context = context
        appender.start()

        return appender
    }

    private fun fileAppender(context: LoggerContext): RollingFileAppender<ILoggingEvent> {
        val layout = PatternLayout()
        val encoder = LayoutWrappingEncoder<ILoggingEvent>()
        val rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>()
        val appender = RollingFileAppender<ILoggingEvent>()

        layout.pattern = PATTERN
        layout.context = context
        layout.start()

        encoder.layout = layout
        encoder.context = context
        encoder.start()

        rollingPolicy.fileNamePattern = logFileArchivePattern.toString()
        rollingPolicy.maxHistory = 10
        rollingPolicy.context = context
        rollingPolicy.setParent(appender)
        rollingPolicy.setTotalSizeCap(FileSize(30 * FileSize.MB_COEFFICIENT))

        appender.name = "FILE"
        appender.file = logFile.toString()
        appender.rollingPolicy = rollingPolicy
        appender.encoder = encoder
        appender.context = context

        rollingPolicy.start()
        appender.start()

        return appender
    }

    companion object {

        const val PATTERN = "%date{ISO8601} %-5level [%-27thread] %-40logger{40}| %message%n"

    }

}