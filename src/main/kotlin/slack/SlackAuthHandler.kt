package edu.erittenhouse.gitlabtimetracker.slack

import com.sun.security.ntlm.Server
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.slack.result.LoginResult
import edu.erittenhouse.gitlabtimetracker.slack.result.ServerInitResult
import edu.erittenhouse.gitlabtimetracker.util.httpClient
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.*
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.TimeUnit

class SlackAuthHandler {
    private val authChannel = Channel<SlackCredential?>()

    private var runningAuthServer: ApplicationEngine? = null
    private val authServerLock = Mutex()

    suspend fun authenticateSlack(): LoginResult {
        when (startAuthServer()) {
            is ServerInitResult.ServerAlreadyRunning -> return LoginResult.AuthAlreadyInProgress
            is ServerInitResult.ServerStarted -> { /* Good to go */ }
        }

        // TODO switch to IO context
        Desktop.getDesktop().browse(URI.create("http://localhost:7133/slackLogin"))
        val loginToken = try {
            authChannel.receive()
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

            val server = embeddedServer(Netty, port = 7133) {
                setupAuth()

                routing {
                    authenticate {
                        setupLoginRoute()
                    }
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
                runningAuthServer?.stop(gracePeriod = 5, timeout = 15, timeUnit = TimeUnit.SECONDS)
            } finally {
                runningAuthServer = null
            }
        }
    }

    private fun Route.setupLoginRoute() {
        get("/slackLogin") {
            val authPrincipal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>() ?: run {
                call.respondHtml {
                    head {
                        title("Authentication failed.")
                    }
                    body {
                        h1 {
                            +"Time Tracker authentication failed."
                        }
                        p {
                            +"Slack login didn't work :( Go back to the app and try again."
                        }
                    }
                }

                authChannel.send(null)
                return@get
            }

            call.respondHtml {
                head {
                    title("Authentication complete!")
                }
                body {
                    h1 {
                        +"Time tracker authentication successful."
                    }
                    p {
                        +"Successfully authenticated with slack! You may now close this tab, or it will close automatically in 10 seconds."
                    }
                    script(type = ContentType.Application.JavaScript.toString()) {
                        +"""
                                            window.setTimeout(() => {
                                                window.close()
                                            }, 10000)
                                        """.trimIndent()
                    }
                }
            }

            authChannel.send(SlackCredential(authToken = authPrincipal.accessToken))
        }
    }

    private fun Application.setupAuth() {
        install(Authentication) {
            oauth {
                client = httpClient
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "Slack",
                        authorizeUrl = "https://slack.com/oauth/v2/authorize",
                        accessTokenUrl = "https://slack.com/api/oauth.v2.access",

                        clientId = "1451585135635.1464009081105",
                        clientSecret = "",

                        passParamsInURL = true,
                        defaultScopes = listOf("emoji:read", "users.profile:write"),
                    )
                }
                urlProvider = { "http://localhost:7133/slackLogin" }

            }
        }
    }
}

suspend fun main() {
    val authHandler = SlackAuthHandler()
    val loginResult = authHandler.authenticateSlack()
    println("Login result: $loginResult")
}