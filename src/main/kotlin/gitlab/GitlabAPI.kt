package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.util.httpClient

object GitlabAPI {
    val test = GitlabTest(httpClient)
    val project = GitlabProjectAPI(httpClient)
    val user = GitlabUserAPI(httpClient)
    val issue = GitlabIssueAPI(httpClient)
}