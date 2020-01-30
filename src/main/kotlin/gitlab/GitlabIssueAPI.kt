package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.catchingErrors
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitlabIssueAPI(private val client: HttpClient) {

    /**
     * Retrieves issues on a project that the user is assigned to and are open.
     *
     * @param credentials The credentials that should be used to retrieve the data
     * @param userID The ID of the user the issues should be assigned to
     * @param projectID The ID of the project to look for issues on
     *
     * @throws InvalidResponseError when a bad HTTP status is encountered
     * @throws ConnectivityError when GitLab cannot be reached
     */
    suspend fun getIssuesForProject(credentials: GitlabCredential, userID: Int, projectID: Int): List<GitlabIssue> = withContext(Dispatchers.Default) {
        return@withContext catchingErrors {
            val issues = mutableListOf<GitlabIssue>()
            var page = 0

            while (true) {
                val pageOfIssues = client.get<List<GitlabIssue>>(credentials.instancePath("/api/v4/projects/$projectID/issues")) {
                    addGitlabCredentials(credentials)

                    url {
                        parameters["assignee_id"] = userID.toString()
                        parameters["state"] = "opened"
                        parameters["page"] = page.toString()
                    }
                }

                if (pageOfIssues.isEmpty()) {
                    break
                } else {
                    issues += pageOfIssues
                    page++
                }
            }

            issues
        }
    }
}