package edu.erittenhouse.gitlabtimetracker.slack.error

import com.slack.api.methods.SlackApiException
import edu.erittenhouse.gitlabtimetracker.io.error.HttpErrors

inline fun <T> catchingSlackErrors(message: String? = null, httpLogic: () -> T): T {
    return try {
        httpLogic()
    } catch(e: SlackApiException) {
        throw HttpErrors.InvalidResponseError(
            e.response.code,
            message ?: "Got a bad response from the Slack API.",
            e,
        )
    } catch (e: Exception) {
        throw HttpErrors.ConnectivityError(
            message ?: "Could not connect to slack!",
            e,
        )
    }
}