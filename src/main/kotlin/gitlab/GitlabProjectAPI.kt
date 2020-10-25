package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject
import edu.erittenhouse.gitlabtimetracker.gitlab.error.catchingErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.addGitlabCredentials
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IGitlabProjectAPI {
    /**
     * Retrieves a list of the projects a user is a member of from GitLab.
     *
     * @param credentials The credentials to use to authenticate the request
     * @return The list of Gitlab projects the user is a member of
     */
    suspend fun listUserMemberProjects(credentials: GitlabCredential): List<GitlabProject>
}

class GitlabProjectAPI(private val client: HttpClient) : IGitlabProjectAPI {
    override suspend fun listUserMemberProjects(credentials: GitlabCredential): List<GitlabProject> = withContext(Dispatchers.Default) {
        return@withContext catchingErrors {
            val allProjects = mutableListOf<GitlabProject>()
            var page = 1

            while (true) {
                val pageOfProjects = client.get<List<GitlabProject>>(credentials.instancePath("/api/v4/projects")) {
                    addGitlabCredentials(credentials)

                    url {
                        parameters["simple"] = "true"
                        parameters["membership"] = "true"
                        parameters["order_by"] = "updated_at"
                        parameters["page"] = page.toString()
                    }
                }

                if (pageOfProjects.isEmpty()) {
                    break
                } else {
                    allProjects += pageOfProjects
                    page++
                }
            }

            return@catchingErrors allProjects
        }
    }
}