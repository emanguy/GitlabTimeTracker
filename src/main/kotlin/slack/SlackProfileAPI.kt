package edu.erittenhouse.gitlabtimetracker.slack

import com.slack.api.Slack
import com.slack.api.model.User
import edu.erittenhouse.gitlabtimetracker.io.error.HttpErrors
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.slack.error.catchingSlackErrors
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext

interface ISlackProfileAPI {
    /**
     * Updates a user's status on slack
     */
    suspend fun updateStatus(credentials: SlackCredential, newStatus: String, newStatusEmoji: String)
}

class SlackProfileAPI : ISlackProfileAPI {
    private val asyncMethodsClient = Slack.getInstance().methodsAsync()

    override suspend fun updateStatus(credentials: SlackCredential, newStatus: String, newStatusEmoji: String): Unit = withContext(Dispatchers.Default) {
        val response = catchingSlackErrors {
            asyncMethodsClient.usersProfileSet { req ->
                req.token(credentials.authToken)
                req.profile(User.Profile().apply {
                    statusText = newStatus
                    statusEmoji = newStatusEmoji
                })
            }.await()
        }

        if (!response.isOk) {
            throw HttpErrors.InvalidResponseError(HttpStatusCode.BadRequest.value, "Updating status failed, slack error code: ${response.error}")
        }
    }
}
