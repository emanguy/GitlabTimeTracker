package edu.erittenhouse.gitlabtimetracker.util

import com.fasterxml.jackson.databind.DeserializationFeature
import edu.erittenhouse.gitlabtimetracker.gitlab.error.HttpErrors
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature

val httpClient = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}

fun generateMessageForIOExceptions(exception: Throwable, generateGenericUnhandledMessage: Boolean = true): String? = when(exception) {
    is HttpErrors.ConnectivityError -> "We couldn't reach GitLab, sorry! Try logging back in with the correct base URL."
    is HttpErrors.InvalidResponseError -> "GitLab responded with a bad status code, looks like the request failed. For the nerds, the request failed with a status of ${exception.status}"
    else -> if (generateGenericUnhandledMessage) "We had an issue: ${exception.message}" else null
}

