package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.IssueRefreshResult
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
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tornadofx.Controller
import tornadofx.asObservable

class IssueController : Controller() {
    private val credentialController by inject<CredentialController>()
    private val userController by inject<UserController>()
    private val initialFilterOptions = listOf(MilestoneFilterOption.NoMilestoneOptionSelected, MilestoneFilterOption.HasAssignedMilestone, MilestoneFilterOption.HasNoMilestone)
    private var unfilteredIssueList = listOf<Issue>()
    private val unfilteredIssueListMutex = Mutex()
    val selectedProject = SimpleObjectProperty<Project>()
    val issueList = mutableListOf<Issue>().asObservable()
    val milestoneFilterOptions = initialFilterOptions.toMutableList().asObservable()
    val filter = SimpleObjectProperty<IssueFilter>(IssueFilter())

    private sealed class IssuesAndMilestonesResult {
        object NoCredentials : IssuesAndMilestonesResult()
        object NoUser : IssuesAndMilestonesResult()
        data class FetchedMilestonesAndIssues(val issues: List<Issue>, val milestones: List<Milestone>) : IssuesAndMilestonesResult()
    }

    /**
     * Populate the filters and pull in the list of issues by selecting a project.
     *
     * @param project The project to show issues for
     * @return Whether or not selecting the project worked, and if not why
     */
    suspend fun selectProject(project: Project): ProjectSelectResult {
        val projectIssuesAndMilestones = when (val loadIssMilResult = getIssuesAndMilestonesForProject(project)) {
            is IssuesAndMilestonesResult.NoUser -> return ProjectSelectResult.NoUser
            is IssuesAndMilestonesResult.NoCredentials -> return ProjectSelectResult.NoCredentials
            is IssuesAndMilestonesResult.FetchedMilestonesAndIssues -> loadIssMilResult
        }

        val milestonesAsFilters = projectIssuesAndMilestones.milestones.map { MilestoneFilterOption.SelectedMilestone(it) }

        withContext(Dispatchers.JavaFx) {
            selectedProject.set(project)
            unfilteredIssueListMutex.withLock {
                unfilteredIssueList = projectIssuesAndMilestones.issues
                issueList.setAll(projectIssuesAndMilestones.issues)
            }
            filter.set(IssueFilter())
            milestoneFilterOptions.setAll(initialFilterOptions + milestonesAsFilters)
        }

        return ProjectSelectResult.IssuesLoaded
    }

    suspend fun refreshIssues(): IssueRefreshResult {
        val project = selectedProject.get() ?: return IssueRefreshResult.NoProject
        val issuesAndMilestones = when (val issMilFetchResult = getIssuesAndMilestonesForProject(project)) {
            is IssuesAndMilestonesResult.NoCredentials -> return IssueRefreshResult.NoCredentials
            is IssuesAndMilestonesResult.NoUser -> return IssueRefreshResult.NoUser
            is IssuesAndMilestonesResult.FetchedMilestonesAndIssues -> issMilFetchResult
        }

        val milestonesAsFilters = issuesAndMilestones.milestones.map { MilestoneFilterOption.SelectedMilestone(it) }
        val currentFilter = filter.get() ?: IssueFilter()
        val correctedFilter = if (currentFilter.selectedMilestone is MilestoneFilterOption.SelectedMilestone &&
            !issuesAndMilestones.milestones.any { it == currentFilter.selectedMilestone.milestone }) {
            currentFilter.copy(selectedMilestone = MilestoneFilterOption.NoMilestoneOptionSelected)
        } else {
            currentFilter
        }

        withContext(Dispatchers.JavaFx) {

            filter.set(correctedFilter)
            milestoneFilterOptions.setAll(initialFilterOptions + milestonesAsFilters)
            unfilteredIssueListMutex.withLock {
                unfilteredIssueList = issuesAndMilestones.issues
            }
            applyFilter(correctedFilter)
        }

        return IssueRefreshResult.RefreshSuccess
    }

    /**
     * Updates the current issue filter to filter by the current filter text and updates the list of shown issues.
     */
    suspend fun filterByIssueText(issueText: String) {
        val currentFilter: IssueFilter = filter.value
        val newFilter = currentFilter.copy(filterText = issueText)
        filter.value = newFilter
        applyFilter(newFilter)
    }

    /**
     * Updates the current issue filter to filter by the given milestone filter and updates the list of shown issues.
     */
    suspend fun selectMilestoneFilterOption(milestoneFilter: MilestoneFilterOption) {
        val currentFilter: IssueFilter = filter.value
        val newFilter = currentFilter.copy(selectedMilestone = milestoneFilter)
        filter.value = newFilter
        applyFilter(newFilter)
    }

    /**
     * Records time for an issue.
     *
     * @return A result stating whether or not the recording was successful, and if not why
     */
    suspend fun recordTime(issueWithTime: IssueWithTime): TimeRecordResult {
        if (issueWithTime.elapsedTime.totalMinutes == 0L) return TimeRecordResult.NegligibleTime
        val credentials = credentialController.credentials ?: return TimeRecordResult.NoCredentials
        val success = GitlabAPI.issue.addTimeSpentToIssue(credentials, issueWithTime.issue.projectID, issueWithTime.issue.idInProject, issueWithTime.elapsedTime.toString())

        return if (success) {
            val updatedIssue = issueWithTime.issue.copy(timeSpent = issueWithTime.issue.timeSpent + issueWithTime.elapsedTime)

            withContext(Dispatchers.JavaFx) {
                val issueIdx = issueList.indexOf(issueWithTime.issue)

                if (issueIdx != -1) {
                    issueList[issueIdx] = updatedIssue
                }

                unfilteredIssueListMutex.withLock {
                    val unfilteredIssueIdx = unfilteredIssueList.indexOf(issueWithTime.issue)
                    if (issueIdx != -1) {
                        val mutableListCopy = unfilteredIssueList.toMutableList()
                        mutableListCopy[unfilteredIssueIdx] = updatedIssue
                        unfilteredIssueList = mutableListCopy
                    }
                }
            }

            TimeRecordResult.TimeRecorded
        } else {
            TimeRecordResult.TimeFailedToRecord
        }
    }

    /**
     * Using data already available on the unfiltered list, locally filters the unfiltered issue list
     */
    private suspend fun applyFilter(filter: IssueFilter) = withContext<Unit>(Dispatchers.Default) {
        // Pulling this in in the event something else modifies the list
        val currentIssueList = unfilteredIssueList

        // First filter by milestone
        val milestoneFilteredIssues = when (filter.selectedMilestone) {
            is MilestoneFilterOption.NoMilestoneOptionSelected -> currentIssueList.asSequence()
            is MilestoneFilterOption.HasAssignedMilestone -> currentIssueList.asSequence().filter { it.milestone != null }
            is MilestoneFilterOption.HasNoMilestone -> currentIssueList.asSequence().filter { it.milestone == null }
            is MilestoneFilterOption.SelectedMilestone -> currentIssueList.asSequence().filter {
                it.milestone?.idInProject == filter.selectedMilestone.milestone.idInProject
            }
        }

        // Next filter by filter text
        val filteredIssues = if (filter.filterText.isEmpty()) {
            milestoneFilteredIssues
        } else {
            milestoneFilteredIssues.filter { issue ->
                val searchText = "#${issue.idInProject} ${issue.title}"
                searchText.contains(filter.filterText, ignoreCase = true)
            }
        }
        val filterResult = filteredIssues.toList()

        withContext(Dispatchers.JavaFx) {
            issueList.setAll(filterResult)
        }
    }

    private suspend fun getIssuesAndMilestonesForProject(project: Project): IssuesAndMilestonesResult {
        val credentials = credentialController.credentials ?: return IssuesAndMilestonesResult.NoCredentials
        val currentUser = when(val loadUserResult = userController.getOrLoadCurrentUser()) {
            is UserLoadResult.GotUser -> loadUserResult.user
            is UserLoadResult.NotFound -> return IssuesAndMilestonesResult.NoUser
            is UserLoadResult.NoCredentials -> return IssuesAndMilestonesResult.NoCredentials
        }

        return coroutineScope {
            val issuesDeferred = async { GitlabAPI.issue.getIssuesForProject(credentials, currentUser.id, project.id) }
            val milestonesDeferred = async { GitlabAPI.milestone.getMilestonesForProject(credentials, project.id) }

            val convertedIssues = issuesDeferred.await().map { Issue.fromGitlabDto(it) }
            val convertedMilestones = milestonesDeferred.await().map { Milestone.fromGitlabDto(it) }.sortedBy { it.endDate }

            return@coroutineScope IssuesAndMilestonesResult.FetchedMilestonesAndIssues(convertedIssues, convertedMilestones)
        }
    }
}