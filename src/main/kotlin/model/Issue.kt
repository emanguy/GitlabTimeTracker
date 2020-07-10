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
    val timeEstimate: TimeSpend?,
    val milestone: Milestone?
) {
    companion object {
        fun fromGitlabDto(issue: GitlabIssue) = Issue(
            idInProject = issue.idInProject,
            projectID = issue.projectID,
            title = issue.title,
            creationTime = DateTime.parse(issue.creationTime),
            url = Url(issue.url),
            timeSpent = issue.timeSpend.timeSpent?.let { TimeSpend.fromString(it) } ?: TimeSpend.NONE,
            timeEstimate = issue.timeSpend.timeEstimate?.let { TimeSpend.fromString(it) },
            milestone = issue.milestone?.let { Milestone.fromGitlabDto(it) }
        )
    }
}