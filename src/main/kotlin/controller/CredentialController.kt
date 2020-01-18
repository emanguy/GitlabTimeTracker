package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.gitlab.CredentialManager
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.gitlab.error.CredentialRetrieveError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.CredentialSaveError
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller

class CredentialController : Controller() {
    private val credentialManager = CredentialManager()
    var credentials: GitlabCredential? = null
        private set

    val hasCredentials: Boolean
        get() = credentials != null
    val credentialIssue = SimpleStringProperty("")

    suspend fun loadCredentials() {
        credentials = try {
            credentialManager.getCredential()
        } catch (e: CredentialRetrieveError) {
            credentialIssue.set("Failed to read previously saved GitLab credentials. Please delete your ~/.gtt file.")
            return
        }
    }

    suspend fun tryAddCredentials(credential: GitlabCredential): Boolean {
        withContext(Dispatchers.JavaFx) {
            credentialIssue.set("")
        }
        val credentialsWork = GitlabAPI.test.testCredentials(credential)
        if (!credentialsWork) {
            withContext(Dispatchers.JavaFx) {
                credentialIssue.set("GitLab didn't like the credentials.")
            }
            return false
        }

        try {
            credentialManager.setCredential(credential)
        } catch (e: CredentialSaveError) {
            withContext(Dispatchers.JavaFx) {
                credentialIssue.set("Failed to save your credentials to disk.")
            }
            return false
        }

        credentials = credential
        return true
    }
}