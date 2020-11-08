package edu.erittenhouse.gitlabtimetracker.model.settings.v1

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.SlackCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.VersionedSettings

data class Settings(
    val gitlabCredentials: GitlabCredential,
    val slackCredentials: SlackCredential?
) : VersionedSettings(1)