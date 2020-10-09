package edu.erittenhouse.gitlabtimetracker.gitlab

import edu.erittenhouse.gitlabtimetracker.util.httpClient
import tornadofx.Component
import tornadofx.ScopedInstance

open class GitlabAPI : Component(), ScopedInstance {
    open val test: IGitlabTest = GitlabTest(httpClient)
    open val project: IGitlabProjectAPI = GitlabProjectAPI(httpClient)
    open val user: IGitlabUserAPI = GitlabUserAPI(httpClient)
    open val issue: IGitlabIssueAPI = GitlabIssueAPI(httpClient)
    open val milestone: IGitlabMilestoneAPI = GitlabMilestoneAPI(httpClient)
}