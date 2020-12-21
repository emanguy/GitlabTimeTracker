package edu.erittenhouse.gitlabtimetracker.ui.view.settings

import edu.erittenhouse.gitlabtimetracker.controller.SlackController
import edu.erittenhouse.gitlabtimetracker.controller.result.SlackLoginResult
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.ui.style.FormStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.Images
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexspacer
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
        val statusMessageText = SimpleStringProperty("")
    }
    private object FormControls {
        var statusEmojiInput by singleAssign<TextField>()
        var slackStatusFormatInput by singleAssign<TextField>()
        var integrationEnableToggle by singleAssign<ToggleSwitch>()
    }
    private object FormValues {
        var slackCredential: SlackCredential? = null
        var slackEmoji: String = ""
        var slackStatusText: String = ""
    }

    private val errorMessageDebouncer = Debouncer()
    private val slackController by inject<SlackController>()

    init {
        registerBackgroundTaskInit {
            launch {
                slackController.loadCredentials()
                val controllerConfig = slackController.slackConfig
                if (controllerConfig != null) {
                    slackLoginText.set("Slack Authenticated: ${controllerConfig.credentialAndTeam.teamName}")
                    FormControls.statusEmojiInput.text = controllerConfig.statusEmoji
                    FormControls.slackStatusFormatInput.text = controllerConfig.slackStatusFormat

                    FormValues.slackCredential = controllerConfig.credentialAndTeam
                    FormValues.slackEmoji = controllerConfig.statusEmoji
                    FormValues.slackStatusText = controllerConfig.slackStatusFormat
                } else {
                    slackLoginText.set("Connect to slack")
                }
                FormControls.integrationEnableToggle.isSelected = slackController.enabledState.value

                this@SlackSettingsSubview.launch {
                    slackController.enabledState.collect { slackEnabled ->
                        FormControls.integrationEnableToggle.isSelected = slackEnabled
                    }
                }
            }
        }

        registerBackgroundTaskInit {
            launch {
                FormControls.statusEmojiInput.textProperty().asFlow()
                    .debounce(500)
                    .collect { newTextValue ->
                        FormValues.slackEmoji = newTextValue
                        updateValidationStateAndPersistIfEnabled()
                    }
            }
        }

        registerBackgroundTaskInit {
            launch {
                FormControls.slackStatusFormatInput.textProperty().asFlow()
                    .debounce(500)
                    .collect { newTextValue ->
                        FormValues.slackStatusText = newTextValue
                        updateValidationStateAndPersistIfEnabled()
                    }
            }
        }
    }

    override val root = vbox {
        form {
            fieldset("Slack") {
                hbox {
                    flexspacer()
                    button(slackLoginText) {
                        imageview(Images.newWindow)
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
                    }
                }
                text(ErrorTextBindings.emojiCodeText) {
                    addClass(TypographyStyles.errorText)
                    visibleWhen(ErrorTextBindings.emojiCodeText.booleanBinding { !it.isNullOrEmpty() })
                }

                field("Slack status template") {
                    FormControls.slackStatusFormatInput = textfield {
                        promptText = "Working issue #{{issueNumber}}"
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

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        errorMessageDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
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
        FormControls.statusEmojiInput.removeClass(FormStyles.fieldInvalidBorder)
        ErrorTextBindings.statusMessageText.set("")
        FormControls.slackStatusFormatInput.removeClass(FormStyles.fieldInvalidBorder)

        val slackCredentialValue = FormValues.slackCredential
        var formValid = true

        // Can't enable if we don't have slack credentials
        if (slackCredentialValue == null) {
            formValid = false
        }

        // Can't enable if the emoji the user wants isn't surrounded with colons
        with (FormValues.slackEmoji) {
            if (isEmpty()) {
                ErrorTextBindings.emojiCodeText.set("You must provide an emoji for your status to enable slack integration.")
                FormControls.statusEmojiInput.addClass(FormStyles.fieldInvalidBorder)
                formValid = false
            } else if (!startsWith(":") || !endsWith(":") || length == 1) {
                ErrorTextBindings.emojiCodeText.set("The emoji you provide must start and end with colons (:), like in slack.")
                FormControls.statusEmojiInput.addClass(FormStyles.fieldInvalidBorder)
                formValid = false
            }
        }

        // Can't enable if the status field is empty
        if (FormValues.slackStatusText.isEmpty()) {
            ErrorTextBindings.statusMessageText.set("You must provide a status message to enable slack integration.")
            FormControls.slackStatusFormatInput.addClass(FormStyles.fieldInvalidBorder)
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