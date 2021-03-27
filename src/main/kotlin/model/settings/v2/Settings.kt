package edu.erittenhouse.gitlabtimetracker.model.settings.v2

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.VersionedSettings

data class Settings(
    val gitlabCredentials: GitlabCredential,
    val slackConfig: SlackConfig?,
    val slackEnabled: Boolean,
    val pinnedProjectIDs: Set<Int>,
) : VersionedSettings(2)
