package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.gitlab.error.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitlabUserAPI(private val client: HttpClient) {
    /**
     * Retrieves the user represented by the personal gitlab token.
     *
     * @return The signed in user.
     * @throws InvalidResponseError on bad HTTP responses
     * @throws ConnectivityError when GitLab cannot be reached
     */
    suspend fun getCurrentUser(credentials: GitlabCredential): GitlabUser = withContext(Dispatchers.Default) {
        return@withContext catchingErrors {
            client.get<GitlabUser>("${credentials.gitlabBaseURL}/api/v4/user") {
                addGitlabCredentials(credentials)
            }
        }
    }
}