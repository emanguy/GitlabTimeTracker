package edu.erittenhouse.gitlabtimetracker.model

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import io.ktor.http.Url
import org.joda.time.LocalDateTime

data class Issue(
    val idInProject: Int,
    val projectID: Int,
    val title: String,
    val creationTime: LocalDateTime,
    val url: Url,
    val timeSpent: TimeSpend,
    val timeEstimate: TimeSpend?
) {
    companion object {
        fun fromGitlabDto(issue: GitlabIssue) = Issue(
            issue.idInProject,
            issue.projectID,
            issue.title,
            LocalDateTime.parse(issue.creationTime),
            Url(issue.url),
            issue.timeSpend.timeSpent?.let { TimeSpend.fromString(it) } ?: TimeSpend.NONE,
            issue.timeSpend.timeEstimate?.let { TimeSpend.fromString(it) }
        )
    }
}