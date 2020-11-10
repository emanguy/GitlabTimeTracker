package edu.erittenhouse.gitlabtimetracker.util

import com.fasterxml.jackson.databind.DeserializationFeature
import edu.erittenhouse.gitlabtimetracker.io.error.HttpErrors
import edu.erittenhouse.gitlabtimetracker.io.error.SettingsErrors
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*

val httpClient = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}

fun generateMessageForIOExceptions(exception: Throwable, generateGenericUnhandledMessage: Boolean = true): String? = when(exception) {
    is SettingsErrors.DiskIOError -> "We couldn't save your settings, please try configuring them again. Here's some extra info: ${exception.message}"
    is SettingsErrors.ParseError -> "Your settings appear to be malformed. Try deleting your .gtt file in your home directory."
    is SettingsErrors.RequiredMissingError -> "Something's wrong, please tell a developer that the time tracker tried to save settings without being logged in."
    is HttpErrors.ConnectivityError -> "We couldn't reach GitLab, sorry! Try logging back in with the correct base URL."
    is HttpErrors.InvalidResponseError -> "GitLab responded with a bad status code, looks like the request failed. For the nerds, the request failed with a status of ${exception.status}"
    else -> if (generateGenericUnhandledMessage) "We had an issue: ${exception.message}" else null
}

