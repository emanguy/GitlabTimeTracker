package edu.erittenhouse.gitlabtimetracker.io.error

import io.ktor.client.features.*

sealed class HttpErrors : Exception() {
    class InvalidResponseError(val status: Int, override val message: String, override val cause: Exception? = null) : HttpErrors()
    class ConnectivityError(override val message: String, override val cause: Exception? = null) : HttpErrors()
}

/**
 * Catches common errors thrown by the HTTP client and transforms them to domain errors.
 *
 * @throws HttpErrors.InvalidResponseError when a bad HTTP status is encountered
 * @throws HttpErrors.ConnectivityError when GitLab cannot be reached
 */
inline fun <T> catchingErrors(message: String? = null, httpLogic: () -> T): T {
    return try {
        httpLogic()
    } catch (e: ResponseException) {
        throw HttpErrors.InvalidResponseError(
            e.response.status.value,
            message ?: "Got a bad response to an HTTP request.",
            e
        )
    } catch (e: Exception) {
        throw HttpErrors.ConnectivityError(
            message ?: "Couldn't connect to GitLab.",
            e
        )
    }
}