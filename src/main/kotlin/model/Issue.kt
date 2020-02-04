package edu.erittenhouse.gitlabtimetracker.model

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import io.ktor.http.Url
import org.joda.time.DateTime

data class Issue(
    val idInProject: Int,
    val projectID: Int,
    val title: String,
    val creationTime: DateTime,
    val url: Url,
    val timeSpent: TimeSpend,
    val timeEstimate: TimeSpend?
) {
    companion object {
        fun fromGitlabDto(issue: GitlabIssue) = Issue(
            issue.idInProject,
            issue.projectID,
            issue.title,
            DateTime.parse(issue.creationTime),
            Url(issue.url),
            issue.timeSpend.timeSpent?.let { TimeSpend.fromString(it) } ?: TimeSpend.NONE,
            issue.timeSpend.timeEstimate?.let { TimeSpend.fromString(it) }
        )
    }
}