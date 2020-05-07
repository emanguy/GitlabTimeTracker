package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.error.GitlabError
import edu.erittenhouse.gitlabtimetracker.controller.error.NoCredentialsError
import edu.erittenhouse.gitlabtimetracker.controller.error.WTFError
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.gitlab.error.ConnectivityError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.InvalidResponseError
import edu.erittenhouse.gitlabtimetracker.model.User
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller

class UserController : Controller() {
    private val credentialController by inject<CredentialController>()

    val userProperty = SimpleObjectProperty<User>()

    /**
     * Pulls currently signed-in user data from GitLab and stashes in the user property.
     *
     * @throws NoCredentialsError if there are no credentials to use in the request
     * @throws GitlabError if we have trouble talking to GitLab
     * @throws WTFError if something we didn't account for happens
     */
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

        withContext(Dispatchers.JavaFx) {
            userProperty.set(User.fromGitlabUser(currentUser))
        }
    }

    /**
     * Retrieves the cached local user or pulls user data from GitLab and returns it.
     *
     * @return The data for the currently signed-in user.
     *
     * @throws NoCredentialsError if we don't have credentials for the signed in user
     * @throws GitlabError if we have trouble communicating with GitLab
     * @throws WTFError if something we didn't handle for occurs
     */
    suspend fun getOrLoadCurrentUser(): User {
        if (this.userProperty.get() == null) {
            this.loadCurrentUser()
        }
        return this.userProperty.get() ?: throw WTFError("Tried to load the user but it's still not there")
    }
}