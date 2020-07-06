package edu.erittenhouse.gitlabtimetracker.controller.result

sealed class ProjectSelectResult {
    object IssuesLoaded : ProjectSelectResult()
    object NoUser : ProjectSelectResult()
    object NoCredentials : ProjectSelectResult()
}

sealed class TimeRecordResult {
    object TimeRecorded : TimeRecordResult()
    object TimeFailedToRecord : TimeRecordResult()
    object NoCredentials : TimeRecordResult()
}

sealed class NetworkedFilterResult {
    object FilterApplied : NetworkedFilterResult()
    object NoCredentials : NetworkedFilterResult()
    object NoProject : NetworkedFilterResult()
    object NoUser : NetworkedFilterResult()
}