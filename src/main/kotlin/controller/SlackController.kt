package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.SlackLoginResult
import edu.erittenhouse.gitlabtimetracker.io.SettingsManager
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.SlackConfig
import edu.erittenhouse.gitlabtimetracker.slack.SlackAPI
import edu.erittenhouse.gitlabtimetracker.slack.result.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tornadofx.Controller

class SlackController : Controller() {
    private val slackAPI by inject<SlackAPI>()
    private val settingsManager = SettingsManager(find<StorageConfig>().fileLocation)
    private val mutableEnabledState = MutableStateFlow(false)

    var slackConfig: SlackConfig? = null
        private set
    val enabledState = mutableEnabledState.asStateFlow()


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
        settingsManager.setSlackConfig(newConfig, true)
        slackConfig = newConfig
        mutableEnabledState.value = true
    }

    suspend fun disableSlackIntegration() {
    }
}