package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.result.UserLoadResult
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.model.User
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller

class UserController : Controller() {
    private val gitlabAPI by inject<GitlabAPI>()
    private val credentialController by inject<CredentialController>()

    val userProperty = SimpleObjectProperty<User>()

    /**
     * Pulls currently signed-in user data from GitLab and stashes in the user property.
     */
    suspend fun loadCurrentUser(): UserLoadResult {
        val currentCredentials = credentialController.credentials ?: return UserLoadResult.NoCredentials

        val currentUser = gitlabAPI.user.getCurrentUser(currentCredentials) ?: return UserLoadResult.NotFound
        val userModel = User.fromGitlabUser(currentUser)

        withContext(Dispatchers.JavaFx) {
            userProperty.set(userModel)
        }
        return UserLoadResult.GotUser(userModel)
    }

    /**
     * Retrieves the cached local user or pulls user data from GitLab and returns it.
     *
     * @return The data for the currently signed-in user.
     */
    suspend fun getOrLoadCurrentUser(): UserLoadResult {
        if (this.userProperty.get() == null) {
            return this.loadCurrentUser()
        }
        return UserLoadResult.GotUser(this.userProperty.get())
    }
}