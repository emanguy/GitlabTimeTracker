package edu.erittenhouse.gitlabtimetracker.model.settings.v0

import edu.erittenhouse.gitlabtimetracker.model.settings.VersionedSettings

data class Settings(
    val gitlabBaseURL: String,
    val personalAccessToken: String,
) : VersionedSettings(0)