package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.util.CREDENTIAL_FILE_LOCATION
import edu.erittenhouse.gitlabtimetracker.util.generateTestScope
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.AuthedUser
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tornadofx.find
import java.io.File

class CredentialControllerTest {
    private val gitlabState = GitlabMock(users = listOf(
        AuthedUser(
            userData = GitlabUser(
                id = 1,
                name = "John Doe",
                username = "someuser",
                profilePictureURL = "http://fake-site.com/sample.jpg"
            ),
            apiCredentials = setOf("abc123")
        )
    ))
    private val scope = generateTestScope(gitlabState)
    private val controller = find<CredentialController>(scope)

    @AfterEach
    fun `Remove credentials file`() {
        File(CREDENTIAL_FILE_LOCATION).delete()
    }

    @Test
    fun `Can save credentials`() {
        runBlocking {
            val addResult = controller.tryAddCredentials(GitlabCredential("https://fake.gitlab", "abc123"))
            assertTrue(addResult)
            assertNotNull(controller.credentials)
        }
    }

    @Test
    fun `Error reported when credentials are bad`() {
        runBlocking {
            val addResult = controller.tryAddCredentials(GitlabCredential("https://fake.gitlab", "bad-creds"))
            assertFalse(addResult)
            assertNull(controller.credentials)
        }
    }
}