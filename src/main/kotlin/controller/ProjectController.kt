package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectFetchResult
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.io.SettingsManager
import edu.erittenhouse.gitlabtimetracker.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.*

class ProjectController : Controller() {
    private val gitlabAPI by inject<GitlabAPI>()
    private val credentialController by inject<CredentialController>()
    private val settings = SettingsManager(find<StorageConfig>().fileLocation)
    val projects = mutableListOf<Project>().asObservable()

    private var pinnedProjects = listOf<Project>()
    private var unpinnedProjects = listOf<Project>()

    /**
     * Fetches user's projects from GitLab and adds them to the projects list
     */
    suspend fun fetchProjects(): ProjectFetchResult {
        val credentials = credentialController.credentials ?: return ProjectFetchResult.NoCredentials
        val pinnedProjectIDs = settings.getPinnedProjects()
        val (localPinnedProjects, localUnpinnedProjects) = gitlabAPI.project.listUserMemberProjects(credentials).asSequence()
            .map { Project.fromGitlabDto(it, isPinned = it.id in pinnedProjectIDs) }
            .partition { it.pinned }
        pinnedProjects = localPinnedProjects
        unpinnedProjects = localUnpinnedProjects

        withContext(Dispatchers.JavaFx) {
            projects.setAll(localPinnedProjects + localUnpinnedProjects)
        }
        return ProjectFetchResult.ProjectsRetrieved
    }

    /**
     * Marks a project as pinned and moves it to the top of the project list
     */
    suspend fun pinProject(projectID: Int) {
        val localPinnedProjects = pinnedProjects.toMutableList()
        val localUnpinnedProjects = unpinnedProjects.toMutableList()

        val projectToPinIdx = localUnpinnedProjects.indexOfFirst { it.id == projectID }
        // If we can't find the project in question just return
        if (projectToPinIdx == -1) return

        localPinnedProjects.add(localUnpinnedProjects.removeAt(projectToPinIdx).copy(pinned = true))

        val currentPinnedProjectIDs = settings.getPinnedProjects() + projectID
        settings.setPinnedProjects(currentPinnedProjectIDs)
        pinnedProjects = localPinnedProjects
        unpinnedProjects = localUnpinnedProjects

        withContext(Dispatchers.JavaFx) {
           projects.setAll(localPinnedProjects + localUnpinnedProjects)
        }
    }

    /**
     * Marks a project as unpinned and moves it immediately after the project list
     */
    suspend fun unpinProject(projectID: Int) {
        val localPinnedProjects = pinnedProjects.toMutableList()
        val localUnpinnedProjects = unpinnedProjects.toMutableList()

        val projectToUnpinIdx = localPinnedProjects.indexOfFirst { it.id == projectID }
        // If we couldn't find the specified project, just return
        if (projectToUnpinIdx == -1) return

        localUnpinnedProjects.add(0, localPinnedProjects.removeAt(projectToUnpinIdx).copy(pinned = false))

        val currentPinnedProjectIDs = settings.getPinnedProjects() - projectID
        settings.setPinnedProjects(currentPinnedProjectIDs)
        pinnedProjects = localPinnedProjects
        unpinnedProjects = localUnpinnedProjects

        withContext(Dispatchers.JavaFx) {
            projects.setAll(localPinnedProjects + localUnpinnedProjects)
        }
    }
}
