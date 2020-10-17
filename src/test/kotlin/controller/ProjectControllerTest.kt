package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectFetchResult
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import edu.erittenhouse.gitlabtimetracker.util.CREDENTIAL_FILE_LOCATION
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.AuthedUser
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMock
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMockAPI
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.ProjectMock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tornadofx.Scope
import tornadofx.find
import tornadofx.setInScope
import java.io.File

class ProjectControllerTest {
    private val authData = AuthedUser(
        userData = GitlabUser(
            id = 1,
            name = "John Doe",
            username = "jdoe",
            profilePictureURL = "https://fake.website/images/profilepic.jpg",
        ),
        apiCredentials = setOf("jdoe-creds")
    )

    private val neatProject = ProjectMock(
        projectData = GitlabProject(
            id = 1,
            name = "Neat Project",
            description = "This project is really neat",
            pathWithNamespace = "erittenhouse/neat-project",
            webURL = "https://fake.gitlab/erittenhouse/neat-project",
        ),
    )
    private val mediocreProject = ProjectMock(
        projectData = GitlabProject(
            id = 2,
            name = "Mediocre project",
            description = "This project is kinda so-so",
            pathWithNamespace = "erittenhouse/mediocre-project",
            webURL = "https://fake.gitlab/erittenhouse/mediocre-project",
        ),
    )
    private val spaceProject = ProjectMock(
        projectData = GitlabProject(
            id = 3,
            name = "Space Project",
            description = "This project is built in outer space!",
            pathWithNamespace = "erittenhouse/space-project",
            webURL = "https://fake.gitlab/erittenhouse/space-project",
        ),
    )
    private val gitlabState = GitlabMock(
        projects = listOf(
            neatProject,
            mediocreProject,
            spaceProject,
        ),
        users = listOf(
            authData,
        ),
    )
    private val mockScope = Scope().apply {
        setInScope(GitlabMockAPI(gitlabState), this, GitlabAPI::class)
        setInScope(StorageConfig(CREDENTIAL_FILE_LOCATION), this)
    }

    private val credentialController = find<CredentialController>(mockScope)
    private val controller = find<ProjectController>(mockScope)

    @AfterEach
    fun `Clean up config file`() {
        File(CREDENTIAL_FILE_LOCATION).delete()
    }

    @Test
    fun `Fails to retrieve projects without credentials`() {
        runBlocking {
            val projectFetchResult = controller.fetchProjects()
            assert(projectFetchResult == ProjectFetchResult.NoCredentials)
            assert(controller.projects.isEmpty())
        }
    }

    @Test
    fun `Retrieves projects with credentials`() {
        runBlocking {
            val credentialStoreSuccess = credentialController.tryAddCredentials(GitlabCredential("https://fake.gitlab", "jdoe-creds"))
            assertTrue(credentialStoreSuccess)

            val projectFetchResult = controller.fetchProjects()
            assert(projectFetchResult == ProjectFetchResult.ProjectsRetrieved)
            assert(controller.projects.size == gitlabState.projects.size)
        }
    }
}