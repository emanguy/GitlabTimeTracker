package edu.erittenhouse.gitlabtimetracker.gitlab.error

import io.ktor.client.features.ResponseException

class InvalidResponseError(val status: Int, override val message: String, override val cause: Exception? = null) : Exception()
class ConnectivityError(override val message: String, override val cause: Exception? = null) : Exception()

inline fun <T> catchingErrors(message: String? = null, httpLogic: () -> T): T {
    return try {
        httpLogic()
    } catch (e: ResponseException) {
        throw InvalidResponseError(
            e.response.status.value,
            message ?: "Got a bad response to an HTTP request.",
            e
        )
    } catch (e: Exception) {
        throw ConnectivityError(
            message ?: "Couldn't connect to GitLab.",
            e
        )
    }
}