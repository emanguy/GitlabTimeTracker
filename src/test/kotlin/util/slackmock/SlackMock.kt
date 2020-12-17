package edu.erittenhouse.gitlabtimetracker.util.slackmock

import edu.erittenhouse.gitlabtimetracker.io.error.HttpErrors
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.slack.ISlackAuthHandler
import edu.erittenhouse.gitlabtimetracker.slack.ISlackProfileAPI
import edu.erittenhouse.gitlabtimetracker.slack.result.LoginResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class SlackMock : ISlackAuthHandler, ISlackProfileAPI {
    private var issuedTokenSet = emptySet<String>()
    private val tokenSetMutex = Mutex()

    var shouldFailAuthentication = false
    var teamName = "Sample team"
    var userStatus = ""
        private set
    var userEmoji = ""
        private set

    override suspend fun authenticateSlack(): LoginResult {
        if (shouldFailAuthentication) return LoginResult.LoginFailure
        val newToken = Random.nextInt().toString()
        tokenSetMutex.withLock {
            issuedTokenSet = issuedTokenSet + newToken
        }
        return LoginResult.SuccessfulLogin(SlackCredential(teamName = teamName, authToken = newToken))
    }

    override suspend fun updateStatus(credentials: SlackCredential, newStatus: String, newStatusEmoji: String) {
        if (credentials.authToken !in issuedTokenSet) throw HttpErrors.InvalidResponseError(403, "Forbidden")
        // Status length is limited to 100 characters
        userStatus = newStatus.take(100)
        userEmoji = newStatusEmoji
    }
}