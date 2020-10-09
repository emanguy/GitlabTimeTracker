package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.AuthedUser
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMock
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMockAPI
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tornadofx.Scope
import tornadofx.find
import tornadofx.setInScope
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
    private val scope = Scope().apply {
        setInScope(GitlabMockAPI(gitlabState), this, GitlabAPI::class)
    }
    private val controller = find<CredentialController>(scope, params = mapOf("testMode" to true))

    @AfterEach
    fun `Remove credentials file`() {
        File("./.gtt").delete()
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