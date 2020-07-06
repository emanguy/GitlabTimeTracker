package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.NetworkedFilterResult
import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectSelectResult
import edu.erittenhouse.gitlabtimetracker.controller.result.TimeRecordResult
import edu.erittenhouse.gitlabtimetracker.controller.result.UserLoadResult
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
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
     * @return Whether or not selecting the project worked, and if not why
     */
    suspend fun selectProject(project: Project): ProjectSelectResult {
        val credentials = credentialController.credentials ?: return ProjectSelectResult.NoCredentials

        withContext(Dispatchers.JavaFx) {
            selectedProject.set(project)
        }

        val currentUser = when(val loadUserResult = userController.getOrLoadCurrentUser()) {
            is UserLoadResult.GotUser -> loadUserResult.user
            is UserLoadResult.NotFound -> return ProjectSelectResult.NoUser
            is UserLoadResult.NoCredentials -> return ProjectSelectResult.NoCredentials
        }

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
        return ProjectSelectResult.IssuesLoaded
    }

    suspend fun selectMilestoneFilterOption(milestoneFilter: MilestoneFilterOption) {

    }

    /**
     * Records time for an issue.
     *
     * @return A result stating whether or not the recording was successful, and if not why
     */
    @Suppress("IMPLICIT_CAST_TO_ANY")
    suspend fun recordTime(issueWithTime: IssueWithTime): TimeRecordResult {
        val credentials = credentialController.credentials ?: return TimeRecordResult.NoCredentials
        val success = GitlabAPI.issue.addTimeSpentToIssue(credentials, issueWithTime.issue.projectID, issueWithTime.issue.idInProject, issueWithTime.elapsedTime.toString())

        return if (success) {
            val updatedIssue = issueWithTime.issue.copy(timeSpent = issueWithTime.issue.timeSpent + issueWithTime.elapsedTime)

            withContext(Dispatchers.JavaFx) {
                val issueIdx = issueList.indexOf(issueWithTime.issue)

                if (issueIdx != -1) {
                    issueList[issueIdx] = updatedIssue
                }
            }

            TimeRecordResult.TimeRecorded
        } else {
            TimeRecordResult.TimeFailedToRecord
        }
    }

    /**
     * Apply network-based issue updates based on the filter, followed by a local filter apply
     *
     * @return A result stating whether or not the filter applied successfully, and if not why
     */
    private suspend fun applyFilterWithNetwork(filter: IssueFilter): NetworkedFilterResult {
        val credentials = credentialController.credentials ?: return NetworkedFilterResult.NoCredentials
        val currentProject = this.selectedProject.get() ?: return NetworkedFilterResult.NoProject
        val currentUser = when (val userPullResult = userController.getOrLoadCurrentUser()) {
            is UserLoadResult.GotUser -> userPullResult.user
            is UserLoadResult.NotFound -> return NetworkedFilterResult.NoUser
            is UserLoadResult.NoCredentials -> return NetworkedFilterResult.NoCredentials
        }

        val filteredIssues = GitlabAPI.issue.getIssuesForProject(credentials, currentUser.id, currentProject.id, filter.selectedMilestone)

        withContext(Dispatchers.JavaFx) {
            unfilteredIssueList.clear()
            unfilteredIssueList.addAll(filteredIssues.map { Issue.fromGitlabDto(it) })
        }
        applyFilterLocally(filter)
        return NetworkedFilterResult.FilterApplied
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