package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.catchingErrors
import edu.erittenhouse.gitlabtimetracker.model.filter.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitlabIssueAPI(private val client: HttpClient) {

    /**
     * Retrieves issues on a project that the user is assigned to and are open.
     *
     * @param credentials The credentials that should be used to retrieve the data
     * @param userID The ID of the user the issues should be assigned to
     * @param projectID The ID of the project to look for issues on
     * @return The list of open issues on the project in order of update time, descending
     *
     * @throws InvalidResponseError when a bad HTTP status is encountered
     * @throws ConnectivityError when GitLab cannot be reached
     */
    suspend fun getIssuesForProject(
        credentials: GitlabCredential,
        userID: Int,
        projectID: Int,
        milestoneFilter: MilestoneFilterOption = NoMilestoneOptionSelected
    ): List<GitlabIssue> = withContext(Dispatchers.Default) {
        return@withContext catchingErrors {
            val issues = mutableListOf<GitlabIssue>()
            var page = 1

            while (true) {
                val pageOfIssues = client.get<List<GitlabIssue>>(credentials.instancePath("/api/v4/projects/$projectID/issues")) {
                    addGitlabCredentials(credentials)

                    url {
                        parameters["assignee_id"] = userID.toString()
                        parameters["state"] = "opened"
                        parameters["order_by"] = "updated_at"
                        parameters["page"] = page.toString()

                        when(milestoneFilter) {
                            is NoMilestoneOptionSelected -> { /* Don't need to add a parameter, making exhaustive */ }
                            is HasNoMilestone -> parameters["milestone"] = "None"
                            is HasAssignedMilestone -> parameters["milestone"] = "Any"
                            is SelectedMilestone -> parameters["milestone"] = milestoneFilter.milestone.title
                        }
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

    /**
     * Add an amount of time spend to an issue. Format should be that of model.TimeSpend.toString().
     *
     * @param credentials Authentication information for the request.
     * @param projectID The ID of the project where the issue lives
     * @param issueIDInProject The project-specific ID of the issue to add time to
     * @param timeSpent The amount of time spent, formatted the way the /spend command in GitLab is
     *
     * @return True if the time spent was successfully applied
     */
    suspend fun addTimeSpentToIssue(credentials: GitlabCredential, projectID: Int, issueIDInProject: Int, timeSpent: String): Boolean = withContext(Dispatchers.Default) {
       val response = client.post<HttpStatement>(credentials.instancePath("/api/v4/projects/$projectID/issues/$issueIDInProject/add_spent_time")) {
           addGitlabCredentials(credentials)

           url {
               parameters["duration"] = timeSpent
           }
       }

        return@withContext response.execute().status == HttpStatusCode.Created
    }
}