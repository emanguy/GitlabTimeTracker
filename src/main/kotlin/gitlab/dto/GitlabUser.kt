package edu.erittenhouse.gitlabtimetracker.gitlab.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitlabUser(
    val id: Int,
    val name: String,
    val username: String,
    @JsonProperty("avatar_url") val profilePictureURL: String
)