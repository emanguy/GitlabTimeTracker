package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.error.GitlabError
import edu.erittenhouse.gitlabtimetracker.controller.error.NoCredentialsError
import edu.erittenhouse.gitlabtimetracker.controller.error.NoProjectError
import edu.erittenhouse.gitlabtimetracker.controller.error.WTFError
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.model.IssueWithTime
import edu.erittenhouse.gitlabtimetracker.model.Milestone
import edu.erittenhouse.gitlabtimetracker.model.Project
import edu.erittenhouse.gitlabtimetracker.model.filter.IssueFilter
import edu.erittenhouse.gitlabtimetracker.model.filter.MilestoneFilterOption
import edu.erittenhouse.gitlabtimetracker.model.filter.NoMilestoneOptionSelected
import edu.erittenhouse.gitlabtimetracker.model.filter.SelectedMilestone
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller
import tornadofx.asObservable

class IssueController : Controller() {
    private val credentialController by inject<CredentialController>()
    private val userController by inject<UserController>()
    val selectedProject = SimpleObjectProperty<Project>()
    val issueList = mutableListOf<Issue>().asObservable()
    val unfilteredIssueList = mutableListOf<Issue>()
    val milestoneFilterOptions = mutableListOf<MilestoneFilterOption>(NoMilestoneOptionSelected).asObservable()
    val filter = SimpleObjectProperty(IssueFilter())

    /**
     * Populate the filters and pull in the list of issues by selecting a project.
     *
     * @param project The project to show issues for
     *
     * @throws NoCredentialsError if gitlab credentials don't exist yet for some reason
     * @throws GitlabError if there was a problem talking to GitLab
     * @throws WTFError if some invalid state occurs or if an unexpected error is thrown
     */
    suspend fun selectProject(project: Project) {
        withContext(Dispatchers.JavaFx) {
            selectedProject.set(project)
        }

        val credentials = credentialController.credentials ?: throw NoCredentialsError()
        val currentUser = userController.getOrLoadCurrentUser()

        try {
            coroutineScope {
                val issuesDeferred = async { GitlabAPI.issue.getIssuesForProject(credentials, currentUser.id, project.id) }
                val milestonesDeferred = async { GitlabAPI.milestone.getMilestonesForProject(credentials, project.id) }

                val issues = issuesDeferred.await()
                val milestones = milestonesDeferred.await()

                val orderedConvertedMilestones = milestones.map {
                    SelectedMilestone(Milestone.fromGitlabDto(it))
                }.sortedBy { it.milestone.endDate }

                withContext(Dispatchers.JavaFx) {
                    unfilteredIssueList.clear()
                    unfilteredIssueList.addAll(issues.map { Issue.fromGitlabDto(it) })
                    issueList.setAll(unfilteredIssueList)
                    filter.set(IssueFilter())
                    milestoneFilterOptions.setAll(listOf(NoMilestoneOptionSelected) + orderedConvertedMilestones)
                }
            }
        } catch (e: Exception) {
            when (e) {
                is InvalidResponseError -> throw GitlabError("Got a bad status from GitLab: ${e.status}", e)
                is ConnectivityError -> throw GitlabError("Could not connect to GitLab.", e)
                else -> throw WTFError("Unknown error occurred when talking to GitLab.", e)
            }
        }
    }

    suspend fun selectMilestoneFilterOption(milestoneFilter: MilestoneFilterOption) {

    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    suspend fun recordTime(issueWithTime: IssueWithTime) {
        val credentials = credentialController.credentials ?: throw NoCredentialsError()
        val success = GitlabAPI.issue.addTimeSpentToIssue(credentials, issueWithTime.issue.projectID, issueWithTime.issue.idInProject, issueWithTime.elapsedTime.toString())

        if (success) {
            val updatedIssue = issueWithTime.issue.copy(timeSpent = issueWithTime.issue.timeSpent + issueWithTime.elapsedTime)

            withContext(Dispatchers.JavaFx) {
                val issueIdx = issueList.indexOf(issueWithTime.issue)

                if (issueIdx != -1) {
                    issueList[issueIdx] = updatedIssue
                }
            }
        }
    }

    /**
     * Apply network-based issue updates based on the filter, followed by a local filter apply
     *
     * @throws NoCredentialsError if there are no credentials to use for network requests
     * @throws NoProjectError if a project is not currently selected to pull issues from
     * @throws GitlabError if we have trouble talking to GitLab
     * @throws WTFError in the event something catastrophic happens that we didn't handle for
     */
    private suspend fun applyFilterWithNetwork(filter: IssueFilter) {
        val credentials = credentialController.credentials ?: throw NoCredentialsError()
        val currentProject = this.selectedProject.get() ?: throw NoProjectError()
        val currentUser = userController.getOrLoadCurrentUser()

        val filteredIssues = try {
            GitlabAPI.issue.getIssuesForProject(credentials, currentUser.id, currentProject.id, filter.selectedMilestone)
        } catch (e: Exception) {
            when (e) {
                is InvalidResponseError -> throw GitlabError("Failed to retrieve filtered issue list. Got a bad response from GitLab: ${e.status}", e)
                is ConnectivityError -> throw GitlabError("Could not reach GitLab.", e)
                else -> throw WTFError("Something unexpected happened when filtering issues.")
            }
        }

        withContext(Dispatchers.JavaFx) {
            unfilteredIssueList.clear()
            unfilteredIssueList.addAll(filteredIssues.map { Issue.fromGitlabDto(it) })
        }
        applyFilterLocally(filter)
    }

    /**
     * Using data already available on the unfiltered list, locally filters the unfiltered issue list
     */
    private suspend fun applyFilterLocally(filter: IssueFilter) {
        val filteredIssues = if (filter.filterText.isEmpty()) {
            unfilteredIssueList
        } else {
            unfilteredIssueList.filter {  issue ->
                val searchText = "#${issue.idInProject} ${issue.title}"
                searchText.contains(filter.filterText, ignoreCase = true)
            }
        }

        withContext(Dispatchers.JavaFx) {
            issueList.setAll(filteredIssues)
        }
    }
}