package edu.erittenhouse.gitlabtimetracker.gitlab.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitlabIssue(
    @JsonProperty("iid") val idInProject: Int,
    @JsonProperty("project_id") val projectID: Int,
    val title: String,
    @JsonProperty("created_at") val creationTime: String,
    @JsonProperty("web_url") val url: String,
    @JsonProperty("time_stats") val timeSpend: GitlabTimeSpent
)