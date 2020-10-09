package edu.erittenhouse.gitlabtimetracker.util.gitlabmock

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue

data class AssignedIssueMock(val issue: GitlabIssue, val assignedUserID: Int? = null)

fun List<AssignedIssueMock>.extractIssues() = this.map { it.issue }