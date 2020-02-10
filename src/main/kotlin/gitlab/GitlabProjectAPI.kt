package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.catchingErrors
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitlabProjectAPI(private val client: HttpClient) {
    /**
     * Retrieves a list of the projects a user is a member of from GitLab.
     *
     * @return The list of Gitlab projects the user is a member of
     * @throws ConnectivityError if GitLab could not be reached
     * @throws InvalidResponseError if GitLab returned a non-2xx status code
     */
    suspend fun listUserMemberProjects(credentials: GitlabCredential): List<GitlabProject> = withContext(Dispatchers.Default) {
        return@withContext catchingErrors {
            val allProjects = mutableListOf<GitlabProject>()
            var page = 1

            while (true) {
                val pageOfProjects = client.get<List<GitlabProject>>(credentials.instancePath("/api/v4/projects")) {
                    addGitlabCredentials(credentials)

                    url {
                        parameters["simple"] = "true"
                        parameters["membership"] = "true"
                        parameters["page"] = page.toString()
                    }
                }

                if (pageOfProjects.isEmpty()) {
                    break;
                } else {
                    allProjects += pageOfProjects
                    page++
                }
            }

            return@catchingErrors allProjects
        }
    }
}