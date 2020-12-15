package edu.erittenhouse.gitlabtimetracker.util.slackmock

import edu.erittenhouse.gitlabtimetracker.slack.SlackAPI

class SlackMockAPI(slackMock: SlackMock) : SlackAPI() {
    override val authHandler = slackMock
    override val profileAPI = slackMock
}