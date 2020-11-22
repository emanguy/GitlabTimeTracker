package edu.erittenhouse.gitlabtimetracker.slack.result

import edu.erittenhouse.gitlabtimetracker.model.SlackCredential

sealed class ServerInitResult {
    object ServerStarted : ServerInitResult()
    object ServerAlreadyRunning : ServerInitResult()
}

sealed class LoginResult {
    data class SuccessfulLogin(val slackCredential: SlackCredential) : LoginResult()
    object LoginFailure : LoginResult()
    object AuthAlreadyInProgress : LoginResult()
}