package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabMilestone
import edu.erittenhouse.gitlabtimetracker.io.error.catchingErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.addGitlabCredentials
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IGitlabMilestoneAPI {
    /**
     * Fetches the active milestones on a given project.
     *
     * @param credentials The credentials to be used to authenticate the request to GitLab
     * @param projectID The project to look the credentials up from
     * @return The list of milestones active on the given project
     */
    suspend fun getMilestonesForProject(credentials: GitlabCredential, projectID: Int): List<GitlabMilestone>
}

class GitlabMilestoneAPI(private val client: HttpClient) : IGitlabMilestoneAPI {

    override suspend fun getMilestonesForProject(credentials: GitlabCredential, projectID: Int): List<GitlabMilestone> = withContext(Dispatchers.Default){
        return@withContext catchingErrors {
            val milestones = mutableListOf<GitlabMilestone>()
            var currentPage = 1

            while(true) {
                val milestonesPage = client.get<List<GitlabMilestone>>(credentials.instancePath("/api/v4/projects/$projectID/milestones")) {
                    addGitlabCredentials(credentials)

                    url {
                        parameters["page"] = currentPage.toString()
                        parameters["state"] = "active"
                    }
                }

                if (milestonesPage.isEmpty()) {
                    break
                } else {
                    milestones += milestonesPage
                    currentPage++
                }
            }

            return@catchingErrors milestones
        }
    }
}