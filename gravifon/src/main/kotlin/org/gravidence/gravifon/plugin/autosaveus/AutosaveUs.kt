package org.gravidence.gravifon.plugin.autosaveus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.domain.notification.Notification
import org.gravidence.gravifon.domain.notification.NotificationLifespan
import org.gravidence.gravifon.domain.notification.NotificationType
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.ApplicationStartedEvent
import org.gravidence.gravifon.event.application.PersistConfigurationEvent
import org.gravidence.gravifon.event.application.PushInnerNotificationEvent
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.plugin.Plugin
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.fixedRateTimer

private val logger = KotlinLogging.logger {}

@Component
class AutosaveUs(
    override val configurationManager: ConfigurationManager,
) : Plugin, EventAware {

    override var pluginEnabled: Boolean
        get() = componentConfiguration.value.enabled
        set(value) {
            componentConfiguration.value = componentConfiguration.value.copy(enabled = value)
            setupIntervalSecondsHandler()
        }
    override val pluginDisplayName: String = "Autosave Us"
    override val pluginDescription: String = "Autosave Us v0.1"

    private var timer: Timer? = null

    override fun consume(event: Event) {
        if (event is ApplicationStartedEvent) {
            setupIntervalSecondsHandler()
        }
    }

    @Synchronized
    private fun setupIntervalSecondsHandler() {
        val cc = componentConfiguration.value

        timer?.cancel()
        if (pluginEnabled && cc.intervalSeconds > 0 ) {
            logger.info { "Register handler: save every ${cc.intervalSeconds} seconds" }
            timer = fixedRateTimer(
                initialDelay = cc.intervalSeconds * 1000,
                period = cc.intervalSeconds * 1000
            ) {
                val message = "Autosave us..."
                logger.debug { message }
                publish(PersistConfigurationEvent())
                publish(
                    PushInnerNotificationEvent(
                        Notification(
                            message = message,
                            type = NotificationType.REGULAR,
                            lifespan = NotificationLifespan.LONG
                        )
                    )
                )
            }
        }
    }

    @Serializable
    data class AutosaveUsComponentConfiguration(
        var enabled: Boolean = true,
        var intervalSeconds: Long = 600,
    ) : ComponentConfiguration

    override val componentConfiguration = mutableStateOf(
        readComponentConfiguration {
            AutosaveUsComponentConfiguration()
        }
    )

    inner class AutosaveUsSettingsState(
        val intervalSeconds: MutableState<Long>,
    ) {

        fun updateIntervalSeconds(input: String) {
            input.toLongOrNull()?.let {
                intervalSeconds.value = it
            }
        }

        fun commitIntervalSeconds() {
            componentConfiguration.value = componentConfiguration.value.copy(intervalSeconds = intervalSeconds.value)
            setupIntervalSecondsHandler()
        }

    }

    @Composable
    fun rememberAutosaveUsSettingsState(
        intervalSeconds: Long,
    ) = remember(intervalSeconds) {
        AutosaveUsSettingsState(
            intervalSeconds = mutableStateOf(intervalSeconds)
        )
    }

    @Composable
    override fun composeSettings() {
        val dialogState = rememberAutosaveUsSettingsState(
            intervalSeconds = componentConfiguration.value.intervalSeconds,
        )

        Box(
            modifier = Modifier
                .widthIn(min = 400.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = dialogState.intervalSeconds.value.toString(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        label = {
                            Text("Interval in seconds")
                        },
                        trailingIcon = {
                            if (dialogState.intervalSeconds.value != componentConfiguration.value.intervalSeconds) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Apply change",
                                    modifier = Modifier
                                        .clickable { dialogState.commitIntervalSeconds() }
                                )
                            }
                        },
                        onValueChange = { dialogState.updateIntervalSeconds(it) }
                    )
                }
            }
        }
    }

}