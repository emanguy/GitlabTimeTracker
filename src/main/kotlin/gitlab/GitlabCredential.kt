package edu.erittenhouse.gitlabtimetracker.gitlab

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders

data class GitlabCredential(
    val gitlabBaseURL: String,
    val personalAccessToken: String
) {
    fun instancePath(path: String) = "$gitlabBaseURL$path"
}

fun HttpRequestBuilder.addGitlabCredentials(credentials: GitlabCredential) {
    headers[HttpHeaders.Authorization] = "Bearer ${credentials.personalAccessToken}"
}
