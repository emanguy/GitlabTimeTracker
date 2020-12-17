package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.RecordingStopResult
import edu.erittenhouse.gitlabtimetracker.controller.result.SlackLoginResult
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabTimeSpent
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.util.CREDENTIAL_FILE_LOCATION
import edu.erittenhouse.gitlabtimetracker.util.generateTestSlackScope
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.AuthedUser
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMock
import edu.erittenhouse.gitlabtimetracker.util.slackmock.SlackMock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import tornadofx.find
import java.io.File

class SlackControllerTest {
    private val issue =  GitlabIssue(
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
    )
    private val userAPIKey = "jdoe-creds"
    private val user = AuthedUser(
        userData = GitlabUser(
            id = 1,
            name = "John Doe",
            username = "jdoe",
            profilePictureURL = "http://fake.url/profilepic.jpg",
        ),
        apiCredentials = setOf(userAPIKey),
    )
    private val gitlabMock = GitlabMock(
        users = listOf(user),
    )
    private val slackMock = SlackMock()

    private val scope = generateTestSlackScope(gitlabMock, slackMock)
    private val controller = find<SlackController>(scope)
    private val timeRecordingController = find<TimeRecordingController>(scope)
    private val credentialController = find<CredentialController>(scope)

    @BeforeEach
    fun `Perform gitlab login`() {
        // The application expects that you've already connected to gitlab so you need to provide credentials first
        runBlocking {
            credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", userAPIKey))
        }
    }

    @AfterEach
    fun `Clean up settings file`() {
        File(CREDENTIAL_FILE_LOCATION).delete()
    }

    @Test
    fun `Slack controller returns failure result when login fails`() {
        runBlocking {
            slackMock.shouldFailAuthentication = true
            val loginResult = controller.slackLogin()
            assertTrue(loginResult == SlackLoginResult.InvalidCredentials)
        }
    }

    @Test
    fun `Slack controller doesn't trigger an update without being enabled`() {
        runBlocking {
            val loginResult = controller.slackLogin()
            assertTrue(loginResult is SlackLoginResult.SuccessfulLogin)
            controller.updateSlackStatus(Issue.fromGitlabDto(issue))

            assertEquals("", slackMock.userStatus)
            assertEquals("", slackMock.userEmoji)
        }
    }

    @Test
    fun `Slack controller can update and clear status when it has been enabled`() {
        runBlocking {
            val loginResult = controller.slackLogin()
            if (loginResult !is SlackLoginResult.SuccessfulLogin) fail("Did not manage to log in")

            controller.enableSlackIntegration(loginResult.credential, ":emoji:", "Working on tasking")
            controller.updateSlackStatus(Issue.fromGitlabDto(issue))

            assertEquals(":emoji:", slackMock.userEmoji)
            assertEquals("Working on tasking", slackMock.userStatus)

            controller.clearSlackStatus()

            assertEquals("", slackMock.userEmoji)
            assertEquals("", slackMock.userStatus)
        }
    }

    @Test
    fun `Slack controller doesn't update if it was enabled then disabled again`() {
        runBlocking {
            val loginResult = controller.slackLogin()
            if (loginResult !is SlackLoginResult.SuccessfulLogin) fail("Did not manage to log in")

            controller.enableSlackIntegration(loginResult.credential, ":emoji:", "Working on tasking")
            controller.disableSlackIntegration()
            controller.updateSlackStatus(Issue.fromGitlabDto(issue))

            assertEquals("", slackMock.userEmoji)
            assertEquals("", slackMock.userStatus)
        }
    }

    @Test
    fun `Slack controller substitutes issue values`() {
        runBlocking {
            val loginResult = controller.slackLogin()
            if (loginResult !is SlackLoginResult.SuccessfulLogin) fail("Did not manage to log in")

            controller.enableSlackIntegration(loginResult.credential, ":emoji:", "Working on #{{issueNumber}}: {{issueTitle}}")
            controller.updateSlackStatus(Issue.fromGitlabDto(issue))

            assertEquals("Working on #${issue.idInProject}: ${issue.title}", slackMock.userStatus)
        }
    }

    @Test
    fun `Slack controller trims status length to 100 characters`() {
        runBlocking {
            val loginResult = controller.slackLogin()
            if (loginResult !is SlackLoginResult.SuccessfulLogin) fail("Did not manage to log in")

            val tooLongString = buildString {
                repeat(200) {
                    append('a')
                }
            }
            val expectedResultString = tooLongString.take(97) + "..."

            controller.enableSlackIntegration(loginResult.credential, ":emoji:", tooLongString)
            controller.updateSlackStatus(Issue.fromGitlabDto(issue))

            assertEquals(expectedResultString, slackMock.userStatus)
        }
    }

    @Test
    fun `Slack controller updates slack when time recording controller records`() {
        runBlocking {
            val loginResult = controller.slackLogin()
            if (loginResult !is SlackLoginResult.SuccessfulLogin) fail("Did not manage to log in")

            controller.enableSlackIntegration(loginResult.credential, ":male-technologist:", "Working issue #{{issueNumber}}")
            val issueToRecord = Issue.fromGitlabDto(issue)
            val startResult = timeRecordingController.startTiming(issueToRecord)

            // Give it a small window of time to send the update to slack
            delay(500)

            assertTrue(startResult is RecordingStopResult.NoIssueBeingRecorded)
            assertEquals(":male-technologist:", slackMock.userEmoji)
            assertEquals("Working issue #${issueToRecord.idInProject}", slackMock.userStatus)

            delay(2000)
            val stopResult = timeRecordingController.stopTiming()

            // Give it a small window of time for the update to propagate
            delay(500)

            assertTrue(stopResult is RecordingStopResult.StoppedTiming)
            assertEquals("", slackMock.userEmoji)
            assertEquals("", slackMock.userStatus)
        }
    }
}
