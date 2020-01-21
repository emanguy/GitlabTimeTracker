package edu.erittenhouse.gitlabtimetracker.gitlab.error

import io.ktor.client.features.ResponseException

class InvalidResponseError(val status: Int, override val message: String, override val cause: Exception? = null) : Exception()
class ConnectivityError(override val message: String, override val cause: Exception? = null) : Exception()

/**
 * Catches common errors thrown by the HTTP client and transforms them to domain errors.
 *
 * @throws InvalidResponseError when a bad HTTP status is encountered
 * @throws ConnectivityError when GitLab cannot be reached
 */
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