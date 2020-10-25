package edu.erittenhouse.gitlabtimetracker.ui.view

import edu.erittenhouse.gitlabtimetracker.controller.CredentialController
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.gitlab.error.CredentialIOError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.HttpErrors
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import edu.erittenhouse.gitlabtimetracker.ui.util.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.view.timetracking.TimeTrackingView
import edu.erittenhouse.gitlabtimetracker.util.generateMessageForIOExceptions
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class LoginView : SuspendingView("Gitlab Time Tracker - Login") {
    private val credentialController: CredentialController by inject()
    private val usePreviousCredentialsVisible = SimpleBooleanProperty(false)
    private val credentialIssueText = SimpleStringProperty("")

    override val root = form {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        text("Gitlab Time Tracker") {
            addClass(TypographyStyles.title)
        }

        label("Gitlab Base URL:")
        val urlField = textfield()

        label("Gitlab Personal API Token:")
        val apiTokenField = textfield()

        button("Let's Go!") {
            suspendingAction {
                attemptLogin(urlField.text, apiTokenField.text)
            }
        }

        text(credentialIssueText) {
            wrappingWidth = 450.0
        }
        hyperlink("Or, use your credentials from last time >>") {
            visibleWhen(usePreviousCredentialsVisible)
            action {
                replaceWith<TimeTrackingView>()
            }
        }
    }

    override fun onDock() {
        super.onDock()

        if (!credentialController.hasCredentials) {
            launch {
                credentialController.loadCredentials()
                usePreviousCredentialsVisible.set(credentialController.hasCredentials)
            }
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        this.currentStage?.apply {
            width = 500.0
            height = 400.0
        }
    }

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        when (exception) {
            is CredentialIOError -> credentialIssueText.set("${exception.message} If your ${exception.problemFilepath} file still exists, please delete it.")
            is HttpErrors -> showErrorModal(generateMessageForIOExceptions(exception))
            else -> showErrorModal("We had an issue: ${exception.message}")
        }
    }

    private suspend fun attemptLogin(url: String, apiToken: String) {
        credentialIssueText.set("Trying to log you in...")
        val newCredential = GitlabCredential(url, apiToken)
        val credentialsWork = credentialController.tryAddCredentials(newCredential)

        if (credentialsWork) {
            this.replaceWith<TimeTrackingView>()
        } else {
            credentialIssueText.set("Hmm. Looks like those credentials didn't work.")
        }
    }
}

