package edu.erittenhouse.gitlabtimetracker.ui.view.settings

import edu.erittenhouse.gitlabtimetracker.controller.SlackController
import edu.erittenhouse.gitlabtimetracker.controller.result.SlackLoginResult
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexSpacer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.toggleswitch
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import io.ktor.html.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextField
import javafx.stage.WindowEvent
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
        val statusMessageText = SimpleStringProperty("")
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
                            textProperty().asFlow()
                                .debounce(500)
                                .collect { newTextValue ->
                                    FormValues.slackEmoji = newTextValue
                                    updateValidationStateAndPersistIfEnabled()
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
                            textProperty().asFlow()
                                .debounce(500)
                                .collect { newTextValue ->
                                    FormValues.slackStatusText = newTextValue
                                    updateValidationStateAndPersistIfEnabled()
                                }
                        }
                    }
                }
                text(ErrorTextBindings.statusMessageText) {
                    addClass(TypographyStyles.errorText)
                    visibleWhen(ErrorTextBindings.statusMessageText.booleanBinding { !it.isNullOrEmpty() })
                }
                text("Limit 100 characters. You may add the issue title via {{issueTitle}} and number via {{issueNumber}}.") {
                    addClass(TypographyStyles.metadata)
                }

                field("Enable integration") {
                    FormControls.integrationEnableToggle = toggleswitch {
                        enableWhen(toggleSwitchEnable)

                        suspendingOnMouseClick {
                            if (this@toggleswitch.isSelected) {
                                val slackCredentialCopy = FormValues.slackCredential
                                if (slackCredentialCopy == null) {
                                    updateValidationStateAndPersistIfEnabled()
                                    return@suspendingOnMouseClick
                                }

                                slackController.enableSlackIntegration(
                                    slackCredentialCopy,
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

    override fun startBackgroundTasks() {
        super.startBackgroundTasks()
        println("Slack - background tasks started")

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

    override fun viewClosing() {
        super.viewClosing()
        println("Subview undocked")
    }

    private suspend fun doSlackLogin() {
        ErrorTextBindings.slackButtonText.set("")
        slackLoginText.set("Connecting to slack...")

        when (val loginResult = slackController.slackLogin()) {
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

        updateValidationStateAndPersistIfEnabled()
    }

    private suspend fun updateValidationStateAndPersistIfEnabled() {
        // Reset error text
        ErrorTextBindings.emojiCodeText.set("")
        ErrorTextBindings.statusMessageText.set("")
        val slackCredentialValue = FormValues.slackCredential
        var formValid = true

        // Can't enable if we don't have slack credentials
        if (slackCredentialValue == null) {
            formValid = false
        }

        // Can't enable if the emoji the user wants isn't surrounded with colons
        with (FormValues.slackEmoji) {
            if (isEmpty() || !startsWith(":") || !endsWith(":")) {
                ErrorTextBindings.emojiCodeText.set("The emoji you provide must start and end with colons (:), like in slack.")
                formValid = false
            }
        }

        // Can't enable if the status field is empty
        if (FormValues.slackStatusText.isEmpty()) {
            ErrorTextBindings.statusMessageText.set("You must provide a status message to enable slack integration.")
            formValid = false
        }

        if (!formValid) {
            toggleSwitchEnable.set(false)
            slackController.disableSlackIntegration()
            return
        }

        toggleSwitchEnable.set(true)
        if (slackController.enabledState.value) {
            slackController.enableSlackIntegration(slackCredentialValue!!, FormValues.slackEmoji, FormValues.slackStatusText)
        }
    }
}