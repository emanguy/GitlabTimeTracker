package edu.erittenhouse.gitlabtimetracker.gitlab.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitlabTimeSpent(
    @JsonProperty("human_time_estimate") val timeEstimate: String,
    @JsonProperty("human_total_time_spent") val timeSpent: String
)