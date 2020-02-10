package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.error.GitlabError
import edu.erittenhouse.gitlabtimetracker.controller.error.NoCredentialsError
import edu.erittenhouse.gitlabtimetracker.controller.error.WTFError
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.model.IssueWithTime
import edu.erittenhouse.gitlabtimetracker.model.Project
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller
import tornadofx.asObservable

class IssueController : Controller() {
    private val credentialController by inject<CredentialController>()
    private val userController by inject<UserController>()
    val selectedProject = SimpleObjectProperty<Project>()
    val issueList = mutableListOf<Issue>().asObservable()

    suspend fun selectProject(project: Project) {
        withContext(Dispatchers.JavaFx) {
            selectedProject.set(project)
        }

        if (userController.userProperty.get() == null) {
            userController.loadCurrentUser()
        }
        val credentials = credentialController.credentials ?: throw NoCredentialsError()
        val currentUser = userController.userProperty.get() ?: throw WTFError("Tried to load the user but it's still not there")

        val issues = try {
            GitlabAPI.issue.getIssuesForProject(credentials, currentUser.id, project.id)
        } catch (e: Exception) {
            when (e) {
                is InvalidResponseError -> throw GitlabError("Got a bad status from GitLab: ${e.status}", e)
                is ConnectivityError -> throw GitlabError("Could not connect to GitLab.", e)
                else -> throw WTFError("Unknown error occurred when talking to GitLab.", e)
            }
        }

        withContext(Dispatchers.JavaFx) {
            issueList.setAll(issues.map { Issue.fromGitlabDto(it) })
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    suspend fun recordTime(issueWithTime: IssueWithTime) {
        val credentials = credentialController.credentials ?: throw NoCredentialsError()
        val success = GitlabAPI.issue.addTimeSpentToIssue(credentials, issueWithTime.issue.projectID, issueWithTime.issue.idInProject, issueWithTime.elapsedTime.toString())

        if (success) {
            val updatedIssue = issueWithTime.issue.copy(timeSpent = issueWithTime.issue.timeSpent + issueWithTime.elapsedTime)

            withContext(Dispatchers.JavaFx) {
                val issueIdx = issueList.indexOf(issueWithTime.issue)

                if (issueIdx == -1) {
                    issueList.add(updatedIssue)
                } else {
                    issueList.set(issueIdx, updatedIssue)
                }
            }
        }
    }
}