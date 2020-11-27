package edu.erittenhouse.gitlabtimetracker.ui.view.settings

import edu.erittenhouse.gitlabtimetracker.controller.SlackController
import edu.erittenhouse.gitlabtimetracker.controller.result.SlackLoginResult
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexSpacer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.toggleswitch
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import io.ktor.html.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.javafx.asFlow
import kotlinx.coroutines.launch
import org.controlsfx.control.ToggleSwitch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SlackSettingsSubview : SuspendingView() {
    private val slackLoginText = SimpleStringProperty("Connect to slack")
    private val toggleSwitchEnable = SimpleBooleanProperty(false)
    private object ErrorTextBindings {
        val slackButtonText = SimpleStringProperty("")
        val emojiCodeText = SimpleStringProperty("")
    }
    private object FormControls {
        var statusEmojiInput by singleAssign<TextField>()
        var slackStatusFormatInput by singleAssign<TextField>()
        var integrationEnableToggle by singleAssign<ToggleSwitch>()
    }

    private val errorMessageDebouncer = Debouncer()
    private val slackController by inject<SlackController>()

    private object FormValues {
        var slackCredential: SlackCredential? = null
        var slackEmoji: String = ""
        var slackStatusText: String = ""
    }

    override val root = vbox {
        form {
            fieldset("Slack") {
                hbox {
                    flexSpacer()
                    button(slackLoginText) {
                        suspendingAction {
                            doSlackLogin()
                        }
                    }
                }
                text(ErrorTextBindings.slackButtonText) {
                    addClass(TypographyStyles.errorText)
                    visibleWhen(ErrorTextBindings.slackButtonText.booleanBinding { !it.isNullOrEmpty() })
                }

                field("Status emoji") {
                    FormControls.statusEmojiInput = textfield {
                        promptText = "Emoji code, i.e. :thinking:"
                        launch {
                            textProperty.asFlow()
                                .debounce(500)
                                .collect { newTextValue ->
                                    FormValues.slackEmoji = newTextValue
                                    updateValidationState()
                                }
                        }
                    }
                }
                text(ErrorTextBindings.emojiCodeText) {
                    addClass(TypographyStyles.errorText)
                    visibleWhen(ErrorTextBindings.emojiCodeText.booleanBinding { !it.isNullOrEmpty() })
                }

                field("Slack status template") {
                    FormControls.slackStatusFormatInput = textfield {
                        promptText = "Working issue #{{issueNumber}}"

                        launch {
                            textProperty.asFlow()
                                .debounce(500)
                                .collect { newTextValue ->
                                    FormValues.slackStatusText = newTextValue
                                    updateValidationState()
                                }
                        }
                    }
                }
                text("Limit 100 characters. You may add the issue title via {{issueTitle}} and number via {{issueNumber}}.") {
                    addClass(TypographyStyles.metadata)
                }

                field("Enable integration") {
                    FormControls.integrationEnableToggle = toggleswitch {
                        enableWhen(toggleSwitchEnable)

                        suspendingOnMouseClick {
                            if (this@toggleswitch.isSelected) {
                                slackController.enableSlackIntegration(
                                    FormValues.slackCredential!!,
                                    FormValues.slackEmoji,
                                    FormValues.slackStatusText
                                )
                            } else {
                                slackController.disableSlackIntegration()
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun doSlackLogin() {
        ErrorTextBindings.slackButtonText.set("")
        slackLoginText.set("Connecting to slack...")
        val loginResult = slackController.slackLogin()

        when (loginResult) {
            is SlackLoginResult.AlreadyLoggingIn -> { /* Do nothing */ }
            is SlackLoginResult.InvalidCredentials -> {
                ErrorTextBindings.slackButtonText.set("Login failed. Please try again.")

                val newSlackButtonText = FormValues.slackCredential?.let { credential ->
                    "Slack Authenticated: ${credential.teamName}"
                } ?: "Connect to slack"
                
                slackLoginText.set(newSlackButtonText)
            }
            is SlackLoginResult.SuccessfulLogin -> {
                FormValues.slackCredential = loginResult.credential
                slackLoginText.set("Slack Authenticated: ${loginResult.credential.teamName}")
            }
        }

        updateValidationState()
    }

    override fun onDock() {
        super.onDock()

        launch {
            slackController.loadCredentials()
            slackController.slackConfig?.let { config ->
                slackLoginText.set("Slack Authenticated: ${config.credentialAndTeam.teamName}")
                FormControls.statusEmojiInput.text = config.statusEmoji
                FormControls.slackStatusFormatInput.text = config.slackStatusFormat

                FormValues.slackCredential = config.credentialAndTeam
                FormValues.slackEmoji = config.statusEmoji
                FormValues.slackStatusText = config.slackStatusFormat
            }
            FormControls.integrationEnableToggle.isSelected = slackController.enabledState.value

            this@SlackSettingsSubview.launch {
                slackController.enabledState.collect { slackEnabled ->
                    FormControls.integrationEnableToggle.isSelected = slackEnabled
                }
            }
        }
    }

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        errorMessageDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
    }

    private fun updateValidationState() {

    }
}