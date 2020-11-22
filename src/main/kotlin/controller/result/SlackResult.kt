package edu.erittenhouse.gitlabtimetracker.controller.result

import edu.erittenhouse.gitlabtimetracker.model.SlackCredential

sealed class SlackLoginResult {
    data class SuccessfulLogin(val credential: SlackCredential) : SlackLoginResult()
    object InvalidCredentials : SlackLoginResult()
    object AlreadyLoggingIn : SlackLoginResult()
}

enum class SlackFields {
    SLACK_CREDENTIALS,
    EMOJI,
    STATUS_FORMAT,
}

sealed class SlackEnableResult {
    object SuccessfullyEnabled : SlackEnableResult()
    data class FieldsInvalid(val invalidFields: List<ValidationError<SlackFields>>)
}