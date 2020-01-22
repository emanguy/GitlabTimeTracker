package edu.erittenhouse.gitlabtimetracker.model

import io.ktor.http.Url

data class Issue(
    val idInProject: Int,
    val projectID: Int,
    val title: String,
    val creationTime: String,
    val url: Url
)