package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabTimeSpent
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.util.CREDENTIAL_FILE_LOCATION
import edu.erittenhouse.gitlabtimetracker.util.generateTestSlackScope
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.AssignedIssueMock
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.AuthedUser
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMock
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.ProjectMock
import edu.erittenhouse.gitlabtimetracker.util.slackmock.SlackMock
import org.junit.jupiter.api.AfterEach
import tornadofx.find
import java.io.File

class SlackControllerTest {
    private val issue = AssignedIssueMock(
        issue = GitlabIssue(
            idInProject = 1,
            projectID = 1,
            title = "A useful issue",
            creationTime = "2020-01-09T14:30:14.748Z",
            url = "https://fake.gitlab/jdoe/neat-project/-/issues/1",
            timeSpend = GitlabTimeSpent(
                timeEstimate = null,
                timeSpent = null,
            ),
            milestone = null,
        ),
        assignedUserID = 1,
    )
    private val project = ProjectMock(
        projectData = GitlabProject(
            id = 1,
            name = "Neat Project",
            description = "My neat project",
            pathWithNamespace = "jdoe/neat-project",
            webURL = "https://fake.gitlab/jdoe/neat-project",
        ),
        issues = listOf(issue),
    )
    private val userAPIKey = "jdoe-creds"
    private val user = AuthedUser(
        userData = GitlabUser(
            id = 1,
            name = "John Doe",
            username = "jdoe",
            profilePictureURL = "http://fake.url/profilepic.jpg",
        ),
        apiCredentials = setOf(userAPIKey)
    )
    private val gitlabMock = GitlabMock(
        projects = listOf(project),
        users = listOf(user),
    )
    private val slackMock = SlackMock()

    private val scope = generateTestSlackScope(gitlabMock, slackMock)
    private val controller = find<SlackController>()
    private val projectController = find<ProjectController>(scope)
    private val issueController = find<IssueController>(scope)
    private val timeRecordingController = find<TimeRecordingController>(scope)

    @AfterEach
    fun `Clean up settings file`() {
        File(CREDENTIAL_FILE_LOCATION).delete()
    }
}