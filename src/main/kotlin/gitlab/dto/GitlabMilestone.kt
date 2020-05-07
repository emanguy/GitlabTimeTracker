package edu.erittenhouse.gitlabtimetracker.gitlab.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitlabMilestone(
    @JsonProperty("iid")
    val idInProject: Int,
    @JsonProperty("project_id")
    val projectID: Int,
    val title: String,
    @JsonProperty("due_date")
    val endDate: String
)