package edu.erittenhouse.gitlabtimetracker.gitlab.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitlabProject(
    val id: Int,
    val description: String?,
    val name: String,
    @JsonProperty("path_with_namespace") val pathWithNamespace: String,
    @JsonProperty("web_url") val webURL: String
)
