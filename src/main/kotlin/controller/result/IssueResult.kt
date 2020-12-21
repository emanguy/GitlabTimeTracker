package edu.erittenhouse.gitlabtimetracker.controller.result

import edu.erittenhouse.gitlabtimetracker.model.Issue

sealed class ProjectSelectResult {
    object IssuesLoaded : ProjectSelectResult()
    object NoCredentials : ProjectSelectResult()
}

sealed class TimeRecordResult {
    object TimeRecorded : TimeRecordResult()
    object NegligibleTime : TimeRecordResult()
    object TimeFailedToRecord : TimeRecordResult()
    object NoCredentials : TimeRecordResult()
}

sealed class IssueRefreshResult {
    object NoProject : IssueRefreshResult()
    object NoCredentials : IssueRefreshResult()
    object RefreshSuccess : IssueRefreshResult()
}

sealed class IssueFetchResult {
    data class IssueFound(val issue: Issue) : IssueFetchResult()
    object IssueNotFound : IssueFetchResult()
    object NoProject : IssueFetchResult()
    object NoCredentials : IssueFetchResult()
}
