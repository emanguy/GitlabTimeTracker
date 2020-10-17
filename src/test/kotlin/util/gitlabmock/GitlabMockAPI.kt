package edu.erittenhouse.gitlabtimetracker.util.gitlabmock

import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI

class GitlabMockAPI(gitlabMock: GitlabMock) : GitlabAPI() {
    override val test = gitlabMock
    override val project = gitlabMock
    override val user = gitlabMock
    override val issue = gitlabMock
    override val milestone = gitlabMock
}