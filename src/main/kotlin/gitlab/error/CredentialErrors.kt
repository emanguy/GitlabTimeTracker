package edu.erittenhouse.gitlabtimetracker.gitlab.error

class CredentialSaveError(override val cause: Exception? = null) : Exception("Failed to save credentials.")
class CredentialRetrieveError(override val cause: Exception? = null) : Exception("Could not find credentials.")
