package edu.erittenhouse.gitlabtimetracker.util.gitlabmock

import edu.erittenhouse.gitlabtimetracker.gitlab.*
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabMilestone
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.io.error.HttpErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.TimeSpend
import edu.erittenhouse.gitlabtimetracker.model.filter.MilestoneFilterOption
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class MethodIdentifier {
    TEST_CREDENTIALS,
    LIST_USER_MEMBER_PROJECTS,
    GET_MILESTONES_FOR_PROJECT,
    GET_ISSUES_FOR_PROJECT,
    GET_ISSUE_IN_PROJECT_BY_ID,
    ADD_TIME_SPENT_TO_ISSUE,
    GET_CURRENT_USER,
}

class GitlabMock(var projects: List<ProjectMock> = emptyList(), var users: List<AuthedUser> = emptyList()) : IGitlabTest, IGitlabProjectAPI, IGitlabMilestoneAPI, IGitlabIssueAPI, IGitlabUserAPI {
    private val projectListLock = Mutex()
    private val errorTriggerLock = Mutex()
    private var httpErrorTriggers: Map<MethodIdentifier, HttpErrors> = emptyMap()

    /**
     * Fetches the issue with the given ID within the specified project.
     */
    fun fetchIssue(projectID: Int, issueIDInProject: Int): GitlabIssue? {
        return projects.firstOrNull { it.projectData.id == projectID }
            ?.issues
            ?.firstOrNull { it.issue.idInProject == issueIDInProject }
            ?.issue
    }

    /**
     * Configures the given gitlab API method to fail with the given HTTP error
     */
    suspend fun triggerHttpErrorOnCall(method: MethodIdentifier, error: HttpErrors) {
        errorTriggerLock.withLock {
            val mutableCopy = httpErrorTriggers.toMutableMap()
            mutableCopy[method] = error
            httpErrorTriggers = mutableCopy
        }
    }

    override suspend fun testCredentials(credentials: GitlabCredential): Boolean {
        throwIfNecessary(MethodIdentifier.TEST_CREDENTIALS)

        val localUsersSnapshot = users
        return localUsersSnapshot.any { credentials.personalAccessToken in it.apiCredentials }
    }

    override suspend fun listUserMemberProjects(credentials: GitlabCredential): List<GitlabProject> {
        if (!testCredentials(credentials)) throw HttpErrors.InvalidResponseError(401, "Bad credentials")
        throwIfNecessary(MethodIdentifier.LIST_USER_MEMBER_PROJECTS)

        val localProjectsSnapshot = projects
        return localProjectsSnapshot.map { it.projectData }
    }

    override suspend fun getMilestonesForProject(credentials: GitlabCredential, projectID: Int): List<GitlabMilestone> {
        if (!testCredentials(credentials)) throw HttpErrors.InvalidResponseError(401, "Bad credentials")
        throwIfNecessary(MethodIdentifier.GET_MILESTONES_FOR_PROJECT)

        val localProjectsSnapshot = projects
        return localProjectsSnapshot.find { it.projectData.id == projectID }?.milestones ?: throw HttpErrors.InvalidResponseError(404, "Not found")
    }

    override suspend fun getIssuesForProject(
        credentials: GitlabCredential,
        userID: Int,
        projectID: Int,
        milestoneFilter: MilestoneFilterOption
    ): List<GitlabIssue> {
        if (!testCredentials(credentials)) throw HttpErrors.InvalidResponseError(401, "Bad credentials")
        throwIfNecessary(MethodIdentifier.GET_ISSUES_FOR_PROJECT)

        val localProjectsList = projects
        val projectIssues = localProjectsList.find { it.projectData.id == projectID }?.issues ?: throw HttpErrors.InvalidResponseError(404, "Not found")
        return when (milestoneFilter) {
            is MilestoneFilterOption.NoMilestoneOptionSelected -> projectIssues.filter { it.assignedUserID == userID}.extractIssues()
            is MilestoneFilterOption.HasAssignedMilestone -> projectIssues.filter { it.assignedUserID == userID && it.issue.milestone != null}.extractIssues()
            is MilestoneFilterOption.HasNoMilestone -> projectIssues.filter { it.assignedUserID == userID && it.issue.milestone == null }.extractIssues()
            is MilestoneFilterOption.SelectedMilestone ->
                projectIssues.filter { it.assignedUserID == userID && it.issue.milestone?.idInProject == milestoneFilter.milestone.idInProject }.extractIssues()
        }
    }

    override suspend fun getIssueInProjectByID(
        credentials: GitlabCredential,
        projectID: Int,
        issueIDInProject: Int
    ): GitlabIssue? {
        if (!testCredentials(credentials)) throw HttpErrors.InvalidResponseError(401, "Bad credentials")
        throwIfNecessary(MethodIdentifier.GET_ISSUE_IN_PROJECT_BY_ID)

        val localProjectsList = projects
        val projectWithIssue = localProjectsList.find { it.projectData.id == projectID } ?: return null
        return projectWithIssue.issues.find { it.issue.idInProject == issueIDInProject }?.issue ?: return null
    }

    override suspend fun addTimeSpentToIssue(
        credentials: GitlabCredential,
        projectID: Int,
        issueIDInProject: Int,
        timeSpent: String,
    ): Boolean {
        if (!testCredentials(credentials)) return false
        throwIfNecessary(MethodIdentifier.ADD_TIME_SPENT_TO_ISSUE)

        projectListLock.withLock {
            val projectIdx = projects.indexOfFirst { it.projectData.id == projectID }
            if (projectIdx == -1) return false
            val project = projects[projectIdx]

            val issueIdx = project.issues.indexOfFirst { it.issue.idInProject == issueIDInProject }
            if (issueIdx == -1) return false
            val issue = project.issues[issueIdx].issue
            
            val timePreviouslySpent = issue.timeSpend.timeSpent?.run { TimeSpend.fromString(this) } ?: TimeSpend(0)
            val timeNewlySpent = TimeSpend.fromString(timeSpent)
            val updatedIssue = issue.copy(timeSpend = issue.timeSpend.copy(timeSpent = (timeNewlySpent + timePreviouslySpent).toString()))

            val newIssueList = project.issues.toMutableList()
            newIssueList[issueIdx] = project.issues[issueIdx].copy(issue = updatedIssue)
            val updatedProject = project.copy(issues = newIssueList)

            val updatedProjectList = projects.toMutableList()
            updatedProjectList[projectIdx] = updatedProject
            projects = updatedProjectList
        }
        return true
    }

    override suspend fun getCurrentUser(credentials: GitlabCredential): GitlabUser {
        if (!testCredentials(credentials)) throw HttpErrors.InvalidResponseError(401, "Bad credentials")
        throwIfNecessary(MethodIdentifier.GET_CURRENT_USER)

        return users.find { credentials.personalAccessToken in it.apiCredentials }?.userData ?:
            error("Something's wonky, the credential test passed but we couldn't find the appropriate user.")
    }

    private fun throwIfNecessary(method: MethodIdentifier) {
        val fetchedError = httpErrorTriggers[method] ?: return
        throw fetchedError
    }
}