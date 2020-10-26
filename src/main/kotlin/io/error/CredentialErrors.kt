package edu.erittenhouse.gitlabtimetracker.io.error

class CredentialIOError(val problemFilepath: String, override val message: String, override val cause: Exception? = null) : Exception()
