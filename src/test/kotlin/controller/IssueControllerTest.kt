package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.IssueRefreshResult
import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectSelectResult
import edu.erittenhouse.gitlabtimetracker.controller.result.TimeRecordResult
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.*
import edu.erittenhouse.gitlabtimetracker.io.error.HttpErrors
import edu.erittenhouse.gitlabtimetracker.model.*
import edu.erittenhouse.gitlabtimetracker.model.filter.MilestoneFilterOption
import edu.erittenhouse.gitlabtimetracker.util.CREDENTIAL_FILE_LOCATION
import edu.erittenhouse.gitlabtimetracker.util.generateTestGitlabScope
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tornadofx.find
import java.io.File


class IssueControllerTest {
    // Milestones - Project 1
    private val weekTwoMilestone = GitlabMilestone(
        idInProject = 1,
        projectID = 1,
        title = "Week 2 2020",
        endDate = "2020-01-14T00:00:00.000Z",
    )
    private val weekThreeMilestone = GitlabMilestone(
        idInProject = 2,
        projectID = 1,
        title = "Week 3 2020",
        endDate = "2020-01-21T00:00:00.000Z",
    )

    // Projects
    private val neatProjectData = GitlabProject(
        id = 1,
        name = "Neat Project",
        description = "This is a super neat project, please star it",
        pathWithNamespace = "erittenhouse/neat-project",
        webURL = "https://fake.gitlab/erittenhouse/neat-project",
    )
    private val mediocreProjectData = GitlabProject(
        id = 2,
        name = "Mediocre project",
        description = "This project is only mediocre, not as cool as the first one",
        pathWithNamespace = "erittenhouse/mediocre-project",
        webURL = "https://fake.gitlab/erittenhouse/mediocre-project",
    )

    // Issues - Project 1
    private val reticulateSplinesIssue = AssignedIssueMock(
        issue = GitlabIssue(
            idInProject = 1,
            projectID = neatProjectData.id,
            title = "Reticulate splines",
            creationTime = "2020-01-09T14:30:14.748Z",
            url = "https://fake.gitlab/erittenhouse/neat-project/-/issues/1",
            milestone = null,
            timeSpend = GitlabTimeSpent(
                timeEstimate = null,
                timeSpent = "2h 3m",
            ),
        ),
        assignedUserID = 1,
    )
    private val discombobulateIssue = AssignedIssueMock(
        issue = GitlabIssue(
            idInProject = 2,
            projectID = neatProjectData.id,
            title = "Discombobulate brain",
            creationTime = "2020-01-10T15:30:20.288Z",
            url = "https://fake.gitlab/erittenhouse/neat-project/-/issues/2",
            milestone = weekTwoMilestone,
            timeSpend = GitlabTimeSpent(
                timeEstimate = null,
                timeSpent = null,
            )
        ),
        assignedUserID = 1,
    )
    private val coffeeIssue = AssignedIssueMock(
        issue = GitlabIssue(
            idInProject = 3,
            projectID = neatProjectData.id,
            title = "Make a great cup of coffee",
            creationTime = "2020-01-08T10:13:50.918Z",
            url = "https://fake.gitlab/erittenhouse/neat-project/-/issues/3",
            milestone = weekTwoMilestone,
            timeSpend = GitlabTimeSpent(
                timeEstimate = null,
                timeSpent = "40m",
            ),
        ),
        assignedUserID = 2
    )
    private val whiteWalkerIssue = AssignedIssueMock(
        issue = GitlabIssue(
            idInProject = 4,
            projectID = neatProjectData.id,
            title = "Prepare for the white walkers",
            creationTime = "2020-01-15T18:30:10.828Z",
            url = "https://fake.gitlab/erittenhouse/neat-project/-/issues/4",
            milestone = weekThreeMilestone,
            timeSpend = GitlabTimeSpent(
                timeEstimate = "4d",
                timeSpent = "10m",
            ),
        ),
        assignedUserID = 1,
    )

    // Issues - Project 2
    private val turnToElevenIssue = AssignedIssueMock(
        issue = GitlabIssue(
            idInProject = 1,
            projectID = mediocreProjectData.id,
            title = "Turn it up to 11",
            creationTime = "2020-02-03T04:05:06.789Z",
            url = "https://fake.gitlab/erittenhouse/mediocre-project/-/issues/1",
            timeSpend = GitlabTimeSpent(
                timeEstimate = null,
                timeSpent = "10m",
            ),
            milestone = null,
        ),
    )
    private val ponderingIssue = AssignedIssueMock(
        issue = GitlabIssue(
            idInProject = 2,
            projectID = mediocreProjectData.id,
            title = "Ponder the meaning of life, the universe, and everything",
            creationTime = "2020-02-01T10:12:33.817Z",
            url = "https://fake.gitlab/erittenhouse/mediocre-project/-/issues/2",
            timeSpend = GitlabTimeSpent(
                timeEstimate = null,
                timeSpent = null,
            ),
            milestone = null,
        ),
        assignedUserID = 1,
    )

    // User credentials
    private val currentUser = AuthedUser(
        userData = GitlabUser(
            id = 1,
            name = "John Doe",
            username = "jdoe",
            profilePictureURL = "http://fake-image.site/something.jpg",
        ),
        apiCredentials = setOf("jdoe-creds")
    )

    private val gitlabState = GitlabMock(
        projects = listOf(
            ProjectMock(
                projectData = neatProjectData,
                issues = listOf(
                    reticulateSplinesIssue,
                    discombobulateIssue,
                    coffeeIssue,
                    whiteWalkerIssue,
                ),
                milestones = listOf(
                    weekTwoMilestone,
                    weekThreeMilestone,
                ),
            ),
            ProjectMock(
                projectData = mediocreProjectData,
                issues = listOf(
                    turnToElevenIssue,
                    ponderingIssue,
                ),
            ),
        ),
        users = listOf(
            currentUser,
        ),
    )
    private val scope = generateTestGitlabScope(gitlabState)
    private val controller = find<IssueController>(scope)
    private val credentialController = find<CredentialController>(scope)

    @AfterEach
    fun `Clean up credential file`() {
        File(CREDENTIAL_FILE_LOCATION).delete()
    }

    @Test
    fun `Retrieves issues for current user`() {
        runBlocking {
            // Load credentials
            val credentialSuccess = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialSuccess)

            // Select the project
            val selectResult = controller.selectProject(Project.fromGitlabDto(neatProjectData))
            assert(selectResult == ProjectSelectResult.IssuesLoaded)

            // Verify that correct issues are loaded
            assert(controller.issueList.size == 3)
            assert(controller.issueList[0] == Issue.fromGitlabDto(reticulateSplinesIssue.issue))
            assert(controller.issueList[1] == Issue.fromGitlabDto(discombobulateIssue.issue))
            assert(controller.issueList[2] == Issue.fromGitlabDto(whiteWalkerIssue.issue))

            // Try the same with the other project
            val selectProject2Result = controller.selectProject(Project.fromGitlabDto(mediocreProjectData))
            assert(selectProject2Result == ProjectSelectResult.IssuesLoaded)

            // Verify the correct issues are loaded
            assert(controller.issueList.size == 1)
            assert(controller.issueList[0] == Issue.fromGitlabDto(ponderingIssue.issue))
        }
    }

    @Test
    fun `Can filter by issue title and number`() {
        runBlocking {
            val credentialLoadResult = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialLoadResult)

            val projectLoadResult = controller.selectProject(Project.fromGitlabDto(neatProjectData))
            assert(projectLoadResult == ProjectSelectResult.IssuesLoaded)

            // Test filtering by text
            controller.filterByIssueText("Reticulate")
            assert(controller.issueList.size == 1)
            assert(controller.issueList[0] == Issue.fromGitlabDto(reticulateSplinesIssue.issue))

            // Test filtering by number
            controller.filterByIssueText("#2")
            assert(controller.issueList.size == 1)
            assert(controller.issueList[0] == Issue.fromGitlabDto(discombobulateIssue.issue))
        }
    }

    @Test
    fun `Can filter by milestone`() {
        runBlocking {
            val credentialLoadResult = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialLoadResult)

            val projectLoadResult = controller.selectProject(Project.fromGitlabDto(neatProjectData))
            assert(projectLoadResult == ProjectSelectResult.IssuesLoaded)

            // Filter: issues with any milestone
            controller.selectMilestoneFilterOption(MilestoneFilterOption.HasAssignedMilestone)
            assert(controller.issueList.size == 2)
            assert(controller.issueList[0] == Issue.fromGitlabDto(discombobulateIssue.issue))
            assert(controller.issueList[1] == Issue.fromGitlabDto(whiteWalkerIssue.issue))

            // Filter: issues with no milestone
            controller.selectMilestoneFilterOption(MilestoneFilterOption.HasNoMilestone)
            assert(controller.issueList.size == 1)
            assert(controller.issueList[0] == Issue.fromGitlabDto(reticulateSplinesIssue.issue))

            // Filter: issues with specific milestone
            val milestone = Milestone.fromGitlabDto(weekTwoMilestone)
            controller.selectMilestoneFilterOption(MilestoneFilterOption.SelectedMilestone(milestone))
            assert(controller.issueList.size == 1)
            assert(controller.issueList[0] == Issue.fromGitlabDto(discombobulateIssue.issue))
        }
    }

    @Test
    fun `Filter is retained on refresh`() {
        runBlocking {
            val credentialLoadResult = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialLoadResult)

            val selectProjectResult = controller.selectProject(Project.fromGitlabDto(neatProjectData))
            assert(selectProjectResult == ProjectSelectResult.IssuesLoaded)

            // Apply filter
            controller.filterByIssueText("Reticulate")
            assert(controller.issueList.size == 1)
            assert(controller.issueList[0] == Issue.fromGitlabDto(reticulateSplinesIssue.issue))

            // Refresh, the issues should remain the same
            val refreshResult = controller.refreshIssues()
            assert(refreshResult == IssueRefreshResult.RefreshSuccess)
            assert(controller.issueList.size == 1)
            assert(controller.issueList[0] == Issue.fromGitlabDto(reticulateSplinesIssue.issue))
        }
    }

    @Test
    fun `Refresh fails without selected project`() {
        runBlocking {
            val credentialLoadResult = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialLoadResult)

            val issueRefreshResult = controller.refreshIssues()
            assert(issueRefreshResult == IssueRefreshResult.NoProject)
        }
    }

    @Test
    fun `Time is recorded correctly`() {
        runBlocking {
            val credentialLoadResult = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialLoadResult)

            // Test - recording time with existing spend
            val timeRecordResult = controller.recordTime(IssueWithTime(
                issue = Issue.fromGitlabDto(reticulateSplinesIssue.issue),
                elapsedTime = TimeSpend(10),
            ))
            assert(timeRecordResult == TimeRecordResult.TimeRecorded)
            assert(gitlabState.fetchIssue(neatProjectData.id, reticulateSplinesIssue.issue.idInProject)?.timeSpend?.timeSpent == "2h 13m")

            // Test - recording time with no spend
            val noSpendRecordResult = controller.recordTime(IssueWithTime(
                issue = Issue.fromGitlabDto(discombobulateIssue.issue),
                elapsedTime = TimeSpend(10),
            ))
            assert(noSpendRecordResult == TimeRecordResult.TimeRecorded)
            assert(gitlabState.fetchIssue(neatProjectData.id, discombobulateIssue.issue.idInProject)?.timeSpend?.timeSpent == "10m")
        }
    }

    @Test
    fun `Time does not record if time spent is less than 1m`() {
        runBlocking {
            val credentialLoadResult = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialLoadResult)

            val issueRecordResult = controller.recordTime(IssueWithTime(
                issue = Issue.fromGitlabDto(reticulateSplinesIssue.issue),
                elapsedTime = TimeSpend(0),
            ))
            assert(issueRecordResult == TimeRecordResult.NegligibleTime)
            assert(reticulateSplinesIssue.issue.timeSpend == gitlabState.fetchIssue(neatProjectData.id, reticulateSplinesIssue.issue.idInProject)?.timeSpend)
        }
    }

    @Test
    fun `Time does not record if credentials are missing`() {
        runBlocking {
            val issueRecordResult = controller.recordTime(IssueWithTime(
                issue = Issue.fromGitlabDto(reticulateSplinesIssue.issue),
                elapsedTime = TimeSpend(10),
            ))
            assert(issueRecordResult == TimeRecordResult.NoCredentials)
        }
    }

    @Test
    fun `Time recording returns failed result if gitlab connection fails`() {
        runBlocking {
            // Configure time recording endpoint to throw a connectivity error
            gitlabState.triggerHttpErrorOnCall(MethodIdentifier.ADD_TIME_SPENT_TO_ISSUE, HttpErrors.ConnectivityError("Couldn't connect to Gitlab"))

            val credentialLoadResult = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialLoadResult)

            val timeRecordResult = controller.recordTime(
                IssueWithTime(
                issue = Issue.fromGitlabDto(reticulateSplinesIssue.issue),
                    elapsedTime = TimeSpend(10),
            ))
            assert(timeRecordResult == TimeRecordResult.TimeFailedToRecord)
        }
    }
}
