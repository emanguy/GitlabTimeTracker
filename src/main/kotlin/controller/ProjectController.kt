package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.error.GitlabError
import edu.erittenhouse.gitlabtimetracker.controller.error.NoCredentialsError
import edu.erittenhouse.gitlabtimetracker.controller.error.WTFError
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller
import tornadofx.asObservable

class ProjectController : Controller() {
    private val credentialController by inject<CredentialController>()
    val projects = mutableListOf<Project>().asObservable()

    /**
     * Fetches user's projects from GitLab and
     */
    suspend fun fetchProjects() {
        val credentials = credentialController.credentials ?: throw NoCredentialsError()
        val fullProjectList = try {
            GitlabAPI.project.listUserMemberProjects(credentials).map { Project.fromGitlabDto(it) }
        } catch (e: Exception) {
            when (e) {
                is InvalidResponseError -> throw GitlabError("Got bad HTTP response from Gitlab: ${e.status}", e)
                is ConnectivityError -> throw GitlabError("Could not connect to GitLab.", e)
                else -> throw WTFError("Something went wrong. Please contact the devs.", e)
            }
        }

        withContext(Dispatchers.JavaFx) {
            projects.setAll(fullProjectList)
        }
    }
}