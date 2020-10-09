package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.gitlab.error.catchingErrors
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IGitlabUserAPI {
    /**
     * Retrieves the user represented by the personal gitlab token.
     *
     * @param credentials The credentials that should be used to authenticate this request.
     * @return The signed in user, or null if the user was not found.
     */
    suspend fun getCurrentUser(credentials: GitlabCredential): GitlabUser?
}

class GitlabUserAPI(private val client: HttpClient) : IGitlabUserAPI {
    override suspend fun getCurrentUser(credentials: GitlabCredential): GitlabUser? = withContext(Dispatchers.Default) {
        return@withContext catchingErrors {
            try {
                client.get<GitlabUser>("${credentials.gitlabBaseURL}/api/v4/user") {
                    addGitlabCredentials(credentials)
                }
            } catch (e: ResponseException) {
                if (e.response.status == HttpStatusCode.NotFound) {
                    null
                } else {
                    throw e
                }
            }
        }
    }
}