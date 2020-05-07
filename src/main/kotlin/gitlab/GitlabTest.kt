package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitlabTest(private val client: HttpClient) {
    /**
     * Tests that the passed credentials are valid
     *
     * @param credentials The GitLab credentials to test
     * @return True if the credentials are valid
     *
     * @throws ConnectivityError if GitLab could not be reached
     * @throws InvalidResponseError if GitLab returned a non-2xx status code
     */
    suspend fun testCredentials(credentials: GitlabCredential): Boolean = withContext(Dispatchers.Default) {
        val response = client.get<HttpStatement>(credentials.instancePath("/api/v4/version")) {
            addGitlabCredentials(credentials)
        }
        return@withContext response.execute().status == HttpStatusCode.OK
    }
}