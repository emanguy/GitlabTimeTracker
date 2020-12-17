package edu.erittenhouse.gitlabtimetracker.model.settings.v1

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.VersionedSettings

data class Settings(
    val gitlabCredentials: GitlabCredential,
    val slackConfig: SlackConfig?,
    val slackEnabled: Boolean
) : VersionedSettings(1)