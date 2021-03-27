package edu.erittenhouse.gitlabtimetracker.model.settings

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.v2.Settings
import edu.erittenhouse.gitlabtimetracker.model.settings.v2.SlackConfig

typealias NewestSettings = Settings
typealias NewestSlackConfig = SlackConfig
/**
 * Constructs settings with default values using only required settings
 */
fun defaultSettings(gitlabCredentials: GitlabCredential): NewestSettings = NewestSettings(
    gitlabCredentials = gitlabCredentials,
    slackConfig = null,
    slackEnabled = false,
    pinnedProjectIDs = emptySet(),
)