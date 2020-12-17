package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import edu.erittenhouse.gitlabtimetracker.io.error.catchingErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.addGitlabCredentials
import edu.erittenhouse.gitlabtimetracker.model.filter.MilestoneFilterOption
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IGitlabIssueAPI {
    /**
     * Retrieves issues on a project that the user is assigned to and are open.
     *
     * @param credentials The credentials that should be used to retrieve the data
     * @param userID The ID of the user the issues should be assigned to
     * @param projectID The ID of the project to look for issues on
     * @return The list of open issues on the project in order of update time, descending
     */
    suspend fun getIssuesForProject(
        credentials: GitlabCredential,
        userID: Int,
        projectID: Int,
        milestoneFilter: MilestoneFilterOption = MilestoneFilterOption.NoMilestoneOptionSelected
    ): List<GitlabIssue>

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
    suspend fun addTimeSpentToIssue(credentials: GitlabCredential, projectID: Int, issueIDInProject: Int, timeSpent: String): Boolean
}

class GitlabIssueAPI(private val client: HttpClient) : IGitlabIssueAPI {

    override suspend fun getIssuesForProject(
        credentials: GitlabCredential,
        userID: Int,
        projectID: Int,
        milestoneFilter: MilestoneFilterOption
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
                            is MilestoneFilterOption.NoMilestoneOptionSelected -> { /* Don't need to add a parameter, making exhaustive */ }
                            is MilestoneFilterOption.HasNoMilestone -> parameters["milestone"] = "None"
                            is MilestoneFilterOption.HasAssignedMilestone -> parameters["milestone"] = "Any"
                            is MilestoneFilterOption.SelectedMilestone -> parameters["milestone"] = milestoneFilter.milestone.title
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

            return@catchingErrors issues
        }
    }

    override suspend fun addTimeSpentToIssue(credentials: GitlabCredential, projectID: Int, issueIDInProject: Int, timeSpent: String): Boolean = withContext(Dispatchers.Default) {
        val response = catchingErrors {
            client.post<HttpStatement>(credentials.instancePath("/api/v4/projects/$projectID/issues/$issueIDInProject/add_spent_time")) {
                addGitlabCredentials(credentials)

                url {
                    parameters["duration"] = timeSpent
                }
            }.execute()
        }

        return@withContext response.status == HttpStatusCode.Created
    }
}