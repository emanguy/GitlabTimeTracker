package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.catchingErrors
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitlabUserAPI(private val client: HttpClient) {
    /**
     * Retrieves the user represented by the personal gitlab token.
     *
     * @param credentials The credentials that should be used to authenticate this request.
     * @return The signed in user.
     *
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