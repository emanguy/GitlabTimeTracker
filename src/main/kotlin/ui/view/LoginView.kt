package edu.erittenhouse.gitlabtimetracker.ui.view

import edu.erittenhouse.gitlabtimetracker.controller.CredentialController
import edu.erittenhouse.gitlabtimetracker.controller.StorageConfig
import edu.erittenhouse.gitlabtimetracker.io.error.HttpErrors
import edu.erittenhouse.gitlabtimetracker.io.error.SettingsErrors
import edu.erittenhouse.gitlabtimetracker.io.migrateSettingsFile
import edu.erittenhouse.gitlabtimetracker.io.result.FileMigrationResult
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showOKModal
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import edu.erittenhouse.gitlabtimetracker.ui.view.timetracking.TimeTrackingView
import edu.erittenhouse.gitlabtimetracker.util.generateMessageForIOExceptions
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

class LoginView : SuspendingView("Gitlab Time Tracker - Login") {
    private val credentialController: CredentialController by inject()
    private val usePreviousCredentialsVisible = SimpleBooleanProperty(false)
    private val credentialIssueText = SimpleStringProperty("")
    private val errorDebouncer = Debouncer()

    init {
        registerBackgroundTaskInit {
            launch {
                val migrationSuccess = performSettingsMigration()

                if (migrationSuccess && !credentialController.hasCredentials) {
                    credentialController.loadCredentials()
                    usePreviousCredentialsVisible.set(credentialController.hasCredentials)
                }
            }
        }
        registerBackgroundTaskInit {
            this.currentStage?.apply {
                width = 500.0
                height = 400.0
            }
        }
    }

    override val root = form {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        text("Gitlab Time Tracker") {
            addClass(TypographyStyles.title)
        }

        label("Gitlab Base URL:")
        val urlField = textfield()

        label("Gitlab Personal API Token:")
        val apiTokenField = textfield()

        button("Log in") {
            imageview("/LogIn.png") {
                fitWidth = 16.0
                fitHeight = 16.0
            }
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

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        errorDebouncer.runDebounced {
            when (exception) {
                is SettingsErrors.DiskIOError -> credentialIssueText.set("${exception.message} If your ${exception.problemFilepath} file still exists, please delete it.")
                is HttpErrors, is SettingsErrors -> showErrorModal(generateMessageForIOExceptions(exception))
                else -> showErrorModal("We had an issue: ${exception.message}")
            }
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

    private suspend fun performSettingsMigration(): Boolean {
        val storageConfig = find<StorageConfig>()
        val migrationMessage = when (val migrationResult = migrateSettingsFile(storageConfig.fileLocation)) {
            is FileMigrationResult.MigrationSucceeded -> null
            is FileMigrationResult.AlreadyOnLatestVersion -> null
            is FileMigrationResult.FileDoesNotExist -> null
            is FileMigrationResult.VersionTooNew -> "Sorry, we couldn't retrieve your settings. It looks like the settings file on disk is from a newer version of Gitlab Time Tracker." +
                    " We recommend you delete the ${storageConfig.fileLocation} file from your hard drive and restart the application. Click OK to quit the application."
            is FileMigrationResult.BadVersion -> "Sorry, we couldn't retrieve your settings. We don't recognize the settings file version. We recommend you delete the ${storageConfig.fileLocation}" +
                    " file from your hard drive. Click OK to quit the application."
            is FileMigrationResult.MigrationProducedUnexpectedModel -> "Sorry, we couldn't retrieve your settings. A developer did something dumb and we couldn't migrate your " +
                    "settings file to the latest format. Tell a developer a migration for version ${migrationResult.modelVersion} failed, then delete your ${storageConfig.fileLocation} file " +
                    "from your hard drive and click OK to quit the application."
            is FileMigrationResult.MigrationMissing -> "Sorry, we couldn't retrieve your settings. A developer did something dumb and we couldn't migrate your settings file to the " +
                    "latest format. Tell a developer the migration from version ${migrationResult.fromVersion} failed, then delete your ${storageConfig.fileLocation} file " +
                    "from your hard drive and click OK to quit the application."
        }

        return if (migrationMessage != null) {
            showOKModal("Settings Migration Failure", migrationMessage) {
                exitProcess(0)
            }
            false
        } else {
            true
        }
    }
}

