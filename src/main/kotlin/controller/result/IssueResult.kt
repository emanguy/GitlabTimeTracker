package edu.erittenhouse.gitlabtimetracker.controller.result

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
