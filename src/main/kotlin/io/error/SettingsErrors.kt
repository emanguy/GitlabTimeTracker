package edu.erittenhouse.gitlabtimetracker.io.error

sealed class SettingsErrors : Exception() {
    class DiskIOError(val problemFilepath: String, override val message: String, override val cause: Exception? = null) : SettingsErrors()
    class ParseError(override val message: String, override val cause: Exception? = null) : SettingsErrors()
    class RequiredMissingError : SettingsErrors() {
        override val message = "Required settings are missing!"
    }
}
