package edu.erittenhouse.gitlabtimetracker.ui.view.settings

import edu.erittenhouse.gitlabtimetracker.controller.SlackController
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexSpacer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.toggleswitch
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.controlsfx.control.ToggleSwitch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class SlackSettingsSubview : SuspendingView() {
    private val errorMessageDebouncer = Debouncer()
    private val buttonText = SimpleStringProperty("Connect to slack")
    private var integrationEnableToggle by singleAssign<ToggleSwitch>()

    private val slackController by inject<SlackController>()

    override val root = vbox {
        form {
            fieldset("Slack") {
                hbox {
                    flexSpacer()
                    button(buttonText)
                }

                field("Status emoji") {
                    textfield {
                        promptText = "Emoji code, i.e. :thinking:"
                    }
                }

                field("Slack status template") {
                    textfield {
                        promptText = "Working issue #{{issueNumber}}"
                    }
                }
                text("Limit 100 characters. You may add the issue title via {{issueTitle}} and number via {{issueNumber}}.") {
                    addClass(TypographyStyles.metadata)
                }

                field("Enable integration") {
                    integrationEnableToggle = toggleswitch()
                }
            }
        }
    }

    private suspend fun doSlackLogin() {
        val loginResult = slackController.slackLogin()
    }

    override fun onDock() {
        super.onDock()

        launch {
            slackController.loadCredentials()
            integrationEnableToggle.isSelected = slackController.enabledState.value

            this@SlackSettingsSubview.launch {
                slackController.enabledState.collect { slackEnabled ->
                    integrationEnableToggle.isSelected = slackEnabled
                }
            }
        }
    }

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        errorMessageDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
    }
}