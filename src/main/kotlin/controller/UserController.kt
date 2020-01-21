package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.error.GitlabError
import edu.erittenhouse.gitlabtimetracker.controller.error.NoCredentialsError
import edu.erittenhouse.gitlabtimetracker.controller.error.WTFError
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.model.User
import javafx.beans.property.SimpleObjectProperty
import tornadofx.Controller
import java.lang.Exception

class UserController : Controller() {
    private val credentialController by inject<CredentialController>()

    val userProperty = SimpleObjectProperty<User>()

    suspend fun loadCurrentUser() {
        val currentCredentials = credentialController.credentials ?: throw NoCredentialsError()

        val currentUser = try {
            GitlabAPI.user.getCurrentUser(currentCredentials)
        } catch (e: Exception) {
            when (e) {
                is InvalidResponseError -> throw GitlabError("Got a bad HTTP response from GitLab: ${e.status}", e)
                is ConnectivityError -> throw GitlabError("Could not connect to GitLab.", e)
                else -> throw WTFError("Unknown issue occurred when fetching users. Please contact the devs.", e)
            }
        }
        userProperty.set(User.fromGitlabUser(currentUser))
    }
}