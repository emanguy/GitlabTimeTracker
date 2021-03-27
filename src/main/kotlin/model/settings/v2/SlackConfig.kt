package edu.erittenhouse.gitlabtimetracker.model.settings.v2

import edu.erittenhouse.gitlabtimetracker.model.SlackCredential

data class SlackConfig(val credentialAndTeam: SlackCredential, val statusEmoji: String, val slackStatusFormat: String)
