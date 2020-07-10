package edu.erittenhouse.gitlabtimetracker.controller.result

sealed class ProjectFetchResult {
    object ProjectsRetrieved : ProjectFetchResult()
    object NoCredentials : ProjectFetchResult()
}
