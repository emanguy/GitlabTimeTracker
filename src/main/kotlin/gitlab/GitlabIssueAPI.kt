package edu.erittenhouse.gitlabtimetracker.gitlab

import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitlabIssueAPI(private val client: HttpClient) {

    suspend fun getIssuesForProject(credentials: GitlabCredential, projectID: Int) = withContext(Dispatchers.Default) {

    }
}