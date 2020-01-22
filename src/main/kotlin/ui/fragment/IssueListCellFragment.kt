package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabIssue
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import tornadofx.ListCellFragment
import tornadofx.addClass
import tornadofx.hbox

class IssueListCellFragment : ListCellFragment<GitlabIssue>() {

    override val root = hbox {
        addClass(LayoutStyles.typicalSpacing)


    }
}