package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.event.TimeRecordingState
import edu.erittenhouse.gitlabtimetracker.controller.result.SlackLoginResult
import edu.erittenhouse.gitlabtimetracker.io.SettingsManager
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.SlackConfig
import edu.erittenhouse.gitlabtimetracker.slack.SlackAPI
import edu.erittenhouse.gitlabtimetracker.slack.result.LoginResult
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SlackController : SuspendingController() {
    private val slackAPI by inject<SlackAPI>()
    private val timeRecordingController by inject<TimeRecordingController>()
    private val settingsManager = SettingsManager(find<StorageConfig>().fileLocation)
    private val mutableEnabledState = MutableStateFlow(false)

    var slackConfig: SlackConfig? = null
        private set
    val enabledState = mutableEnabledState.asStateFlow()

    init {
        launch(Dispatchers.JavaFx) {
            timeRecordingController.recordingIssueState.collect { recordingState ->
                if (enabledState.value) {
                    try {
                        when (recordingState) {
                            is TimeRecordingState.IssueRecording -> updateSlackStatus(recordingState.issue)
                            is TimeRecordingState.NoIssueRecording -> clearSlackStatus()
                        }
                    } catch (e: Exception) {
                        onUncaughtCoroutineException(coroutineContext, e)
                    }
                }
            }
        }
    }
    /**
     * Loads slack credentials from disk into the controller.
     */
    suspend fun loadCredentials() {
        val loadedConfig = settingsManager.getSlackConfig()
        val enabled = settingsManager.getSlackEnabled()

        slackConfig = loadedConfig
        mutableEnabledState.value = enabled
    }

    /**
     * Log in with slack, returning true if login was successful.
     */
    suspend fun slackLogin(): SlackLoginResult =
        when (val slackCredentialResult = slackAPI.authHandler.authenticateSlack()) {
            is LoginResult.SuccessfulLogin -> SlackLoginResult.SuccessfulLogin(slackCredentialResult.slackCredential)
            is LoginResult.LoginFailure -> SlackLoginResult.InvalidCredentials
            is LoginResult.AuthAlreadyInProgress -> SlackLoginResult.AlreadyLoggingIn
        }

    suspend fun enableSlackIntegration(slackCredential: SlackCredential, emoji: String, messageFormat: String) {
        val newConfig = SlackConfig(slackCredential, emoji, messageFormat)
        settingsManager.setSlackConfig(true, newConfig)
        slackConfig = newConfig
        mutableEnabledState.value = true
    }

    suspend fun disableSlackIntegration() {
        settingsManager.setSlackConfig(slackEnabled = false)
        mutableEnabledState.value = false
    }

    suspend fun updateSlackStatus(issue: Issue) {
        val config = slackConfig ?: return
        if (!enabledState.value) return

        withContext(Dispatchers.Default) {
            val interpolatedStatus = config.slackStatusFormat
                .replace("{{issueTitle}}", issue.title)
                .replace("{{issueNumber}}", issue.idInProject.toString())
            val fullStatus = if (interpolatedStatus.length > 100) {
                interpolatedStatus.take(97) + "..."
            } else {
                interpolatedStatus
            }

            slackAPI.profileAPI.updateStatus(config.credentialAndTeam, fullStatus, config.statusEmoji)
        }
    }

    suspend fun clearSlackStatus() {
        val config = slackConfig ?: return
        if (!enabledState.value) return

        withContext(Dispatchers.Default) {
            slackAPI.profileAPI.updateStatus(config.credentialAndTeam, "", "")
        }
    }
}