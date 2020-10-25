package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.error.catchingErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.addGitlabCredentials
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

interface IGitlabTest {
    /**
     * Tests that the passed credentials are valid
     *
     * @param credentials The GitLab credentials to test
     * @return True if the credentials are valid
     */
    suspend fun testCredentials(credentials: GitlabCredential): Boolean
}

class GitlabTest(private val client: HttpClient) : IGitlabTest {
    override suspend fun testCredentials(credentials: GitlabCredential): Boolean = withContext(Dispatchers.Default) {
        val response = withTimeoutOrNull(10_000) {
            catchingErrors {
                client.get<HttpStatement>(credentials.instancePath("/api/v4/version")) {
                    addGitlabCredentials(credentials)
                }.execute()
            }
        }

        return@withContext response?.let {
            it.status == HttpStatusCode.OK
        } ?: false
    }
}