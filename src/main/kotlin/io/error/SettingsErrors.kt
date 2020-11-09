package edu.erittenhouse.gitlabtimetracker.io.error

class SettingsIOError(val problemFilepath: String, override val message: String, override val cause: Exception? = null) : Exception()
class SettingsParseError(override val message: String, override val cause: Exception? = null) : Exception()
class SettingsMissingError() : Exception("Required settings are missing!")