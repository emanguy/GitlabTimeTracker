package edu.erittenhouse.gitlabtimetracker.controller.error

class WTFError(override val message: String, override val cause: Throwable? = null) : Exception()