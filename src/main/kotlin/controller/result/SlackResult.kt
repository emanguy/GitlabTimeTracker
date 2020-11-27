package edu.erittenhouse.gitlabtimetracker.controller.result

import edu.erittenhouse.gitlabtimetracker.model.SlackCredential

sealed class SlackLoginResult {
    data class SuccessfulLogin(val credential: SlackCredential) : SlackLoginResult()
    object InvalidCredentials : SlackLoginResult()
    object AlreadyLoggingIn : SlackLoginResult()
}
