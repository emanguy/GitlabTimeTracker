package edu.erittenhouse.gitlabtimetracker.slack

import com.slack.api.Slack
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import kotlinx.coroutines.future.await

class SlackConnector {
    private val asyncMethodsClient = Slack.getInstance().methodsAsync()
}