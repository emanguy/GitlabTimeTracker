package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.gitlab.CredentialManager
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabCredential
import tornadofx.Controller

class CredentialController : Controller() {
    private val gitlabAPI by inject<GitlabAPI>()
    private val credentialManager = if (params["testMode"] == true) CredentialManager("./.gtt") else CredentialManager()
    var credentials: GitlabCredential? = null
        private set

    val hasCredentials: Boolean
        get() = credentials != null

    suspend fun loadCredentials() {
        credentials = credentialManager.getCredential()
    }

    suspend fun tryAddCredentials(credential: GitlabCredential): Boolean {
        val credentialsWork = gitlabAPI.test.testCredentials(credential)
        if (!credentialsWork) {
            return false
        }

        credentialManager.setCredential(credential)
        credentials = credential

        return true
    }
}