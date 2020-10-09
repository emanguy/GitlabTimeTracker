package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectFetchResult
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller
import tornadofx.asObservable

class ProjectController : Controller() {
    private val gitlabAPI by inject<GitlabAPI>()
    private val credentialController by inject<CredentialController>()
    val projects = mutableListOf<Project>().asObservable()

    /**
     * Fetches user's projects from GitLab and
     */
    suspend fun fetchProjects(): ProjectFetchResult {
        val credentials = credentialController.credentials ?: return ProjectFetchResult.NoCredentials
        val fullProjectList = gitlabAPI.project.listUserMemberProjects(credentials).map { Project.fromGitlabDto(it) }

        withContext(Dispatchers.JavaFx) {
            projects.setAll(fullProjectList)
        }
        return ProjectFetchResult.ProjectsRetrieved
    }
}