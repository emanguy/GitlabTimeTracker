package edu.erittenhouse.gitlabtimetracker.controller.result

import edu.erittenhouse.gitlabtimetracker.model.User

sealed class UserLoadResult {
    data class GotUser(val user: User) : UserLoadResult()
    object NotFound : UserLoadResult()
    object NoCredentials : UserLoadResult()
}

