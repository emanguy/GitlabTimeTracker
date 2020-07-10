package edu.erittenhouse.gitlabtimetracker.gitlab.result

import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabCredential

sealed class CredentialRetrieveResult {
    data class RetrievedCredential(val credential: GitlabCredential) : CredentialRetrieveResult()
    object NoPreviouslySavedCredentials : CredentialRetrieveResult()
}



