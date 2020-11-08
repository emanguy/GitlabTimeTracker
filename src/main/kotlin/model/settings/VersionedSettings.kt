package edu.erittenhouse.gitlabtimetracker.model.settings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
open class VersionedSettings(val version: Int)