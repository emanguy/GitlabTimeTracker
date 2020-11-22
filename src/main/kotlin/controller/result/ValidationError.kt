package edu.erittenhouse.gitlabtimetracker.controller.result

data class ValidationError<T>(val invalidField: T, val message: String)