package edu.erittenhouse.gitlabtimetracker.slack

import com.slack.api.Slack
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.slack.result.LoginResult
import edu.erittenhouse.gitlabtimetracker.slack.result.ServerInitResult
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.html.*
import java.awt.Desktop
import java.net.URI
import java.nio.charset.Charset

interface ISlackAuthHandler {
    /**
     * Opens the default browser and prompts the slack user to authorize the time tracker
     * to access slack on their behalf.
     */
    suspend fun authenticateSlack(): LoginResult
}

@OptIn(KtorExperimentalAPI::class)
class SlackAuthHandler : ISlackAuthHandler {
    private val authChannel = Channel<SlackCredential?>()

    private val oauthAuthorizationURL = "https://slack.com/oauth/v2/authorize"
    private val requestedScopes = listOf("users.profile:write")
    private val clientID = "1451585135635.1464009081105"
    private val clientSecret by lazy {
        this::class.java.getResourceAsStream("/slackClientSecret.txt").readBytes().toString(Charset.defaultCharset())
    }
    private val loginURL by lazy {
        "$oauthAuthorizationURL?client_id=$clientID&user_scope=${requestedScopes.joinToString(",")}"
    }
    private val redirectURL = "http://localhost:7133/slackLogin"

    private var runningAuthServer: ApplicationEngine? = null
    private val authServerLock = Mutex()


    override suspend fun authenticateSlack(): LoginResult {
        when (startAuthServer()) {
            is ServerInitResult.ServerAlreadyRunning -> return LoginResult.AuthAlreadyInProgress
            is ServerInitResult.ServerStarted -> { /* Good to go */ }
        }

        withContext(Dispatchers.IO) {
            Desktop.getDesktop().browse(URI.create(redirectURL))
        }

        // Doing a try here so we terminate the server even if the coroutine gets cancelled
        val loginToken = try {
            val token = authChannel.receive()
            // Delaying for a second to allow the webpage to load
            delay(2000)
            token
        } finally {
            terminateAuthServer()
        }

        return if (loginToken == null) {
            LoginResult.LoginFailure
        } else {
            LoginResult.SuccessfulLogin(loginToken)
        }
    }

    private suspend fun startAuthServer(): ServerInitResult {
        authServerLock.withLock {
            if (runningAuthServer != null) {
                return ServerInitResult.ServerAlreadyRunning
            }

            val server = embeddedServer(CIO, port = 7133) {
                routing {
                    setupLoginRoute()
                    registerStaticContent()
                }
            }

            server.start()
            runningAuthServer = server
        }

        return ServerInitResult.ServerStarted
    }

    private suspend fun terminateAuthServer() {
        authServerLock.withLock {
            try {
                runningAuthServer?.stop(500, 2000)
            } finally {
                runningAuthServer = null
            }
        }
    }

    private fun Route.registerStaticContent() {
        static("res") {
            resource("GTTLogo.png")
        }
    }

    private fun Route.setupLoginRoute() {
        get("/slackLogin") {
            val authorizationCode = call.request.queryParameters["code"] ?: run {
                call.respondRedirect(loginURL)
                return@get
            }

            val tokenResponse = try {
                Slack.getInstance().methodsAsync().oauthV2Access {  req ->
                    req.clientId(clientID)
                    req.clientSecret(clientSecret)
                    req.code(authorizationCode)
                    req.redirectUri(redirectURL)
                }.await()
            } catch (e: Exception) {
                println("Encountered exception during auth - ${e.message}")
                e.printStackTrace()

                call.sendAuthFailPage()
                authChannel.send(null)
                return@get
            }

            val accessToken = tokenResponse.authedUser?.accessToken ?: run {
                println("Did not get access token with response!")
                call.sendAuthFailPage()
                authChannel.send(null)
                return@get
            }
            val teamName = tokenResponse.team?.name ?: "Unknown team"

            call.sendAuthSuccessPage()
            authChannel.send(SlackCredential(teamName = teamName, authToken = accessToken))
        }
    }

    private fun STYLE.addBodyStyles() {
        unsafe {
            +"""
                body > img,div {
                    display: inline-block;
                    vertical-align: middle;
                }
            """
        }
    }

    private suspend fun ApplicationCall.sendAuthFailPage() {
        respondHtml {
            head {
                title("Authentication failed.")
                style {
                    addBodyStyles()
                }
            }
            body {
                img(src = "/res/GTTLogo.png")
                div {
                    h1 {
                        +"Time Tracker authentication failed."
                    }
                    p {
                        +"Slack login didn't work :( Go back to the app and try again."
                    }
                }
            }
        }
    }

    private suspend fun ApplicationCall.sendAuthSuccessPage() {
        respondHtml {
            head {
                title("Authentication complete!")
                style {
                    addBodyStyles()
                }
            }
            body {
                img(src = "/res/GTTLogo.png")
                div {
                    h1 {
                        +"Time tracker authentication successful."
                    }
                    p {
                        +"Successfully authenticated with slack! You may now close this tab."
                    }
                }
            }
        }
    }
}

suspend fun main() {
    val authHandler = SlackAuthHandler()
    withTimeoutOrNull(3000) {
        authHandler.authenticateSlack()
    }
    val loginResult = authHandler.authenticateSlack()
    println("Login result: $loginResult")
}