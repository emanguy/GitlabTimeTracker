package edu.erittenhouse.gitlabtimetracker.controller.error

class GitlabError(override val message: String, override val cause: Throwable? = null) : Exception()