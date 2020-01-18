package edu.erittenhouse.gitlabtimetracker.ui

import edu.erittenhouse.gitlabtimetracker.controller.CredentialController
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.timetracking.TimeTrackingView
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.launch
import tornadofx.*

class LoginView : SuspendingView() {
    val credentialController: CredentialController by inject()
    val usePreviousCredentialsVisible = SimpleBooleanProperty(false)

    override val root = form {
        addClass(LayoutStyles.typicalSpacing)

        text("Gitlab Time Tracker") {
            addClass(TypographyStyles.title)
        }

        label("Gitlab Base URL:")
        val urlField = textfield()

        label("Gitlab Personal API Token:")
        val apiTokenField = textfield()

        button("Let's Go!") {
            suspendingAction {
                val newCredential = GitlabCredential(urlField.text, apiTokenField.text)
                val credentialsWork = credentialController.tryAddCredentials(newCredential)

                if (credentialsWork) {
                    replaceWith<TimeTrackingView>()
                }
            }
        }

        text(credentialController.credentialIssue)
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
}

