package edu.erittenhouse.gitlabtimetracker.model.settings

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.Settings

/**
 * Constructs settings with default values using only required settings
 */
fun defaultSettings(gitlabCredentials: GitlabCredential): Settings = Settings(
    gitlabCredentials = gitlabCredentials,
    slackCredentials = null,
)