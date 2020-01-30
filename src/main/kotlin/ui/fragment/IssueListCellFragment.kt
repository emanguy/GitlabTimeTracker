package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ListCellFragment
import tornadofx.addClass
import tornadofx.hbox
import tornadofx.onChange

class IssueListCellFragment : ListCellFragment<Issue>() {
    private var issue: Issue? = null
    private val idProperty = SimpleStringProperty("#0")
    private val issueTitleProperty = SimpleStringProperty("")
    private val createTimeProperty = SimpleStringProperty("")
    private val progressPercentageProperty = SimpleIntegerProperty(0)
    private val timeSummaryProperty = SimpleStringProperty("")

    override val root = hbox {
        addClass(LayoutStyles.typicalSpacing)
    }

    init {
        itemProperty.onChange {
            handleIssueUpdate(it)
        }
    }

    private fun handleIssueUpdate(updatedIssue: Issue?) {
        if (updatedIssue == null) return

        issue = updatedIssue
        idProperty.set("#${updatedIssue.idInProject}")
        issueTitleProperty.set(updatedIssue.title)
        createTimeProperty.set("Created ${updatedIssue.creationTime.toString("MM/dd/yyyy")}")
        val timeSummaryFirstHalf = "Spent ${updatedIssue.timeSpent}"

        if (updatedIssue.timeEstimate != null) {
            val minutesEstimated = updatedIssue.timeEstimate.totalMinutes
            val minutesSpent = updatedIssue.timeSpent.totalMinutes.coerceAtMost(minutesEstimated)
            val percentComplete = ((minutesSpent * 100) / minutesEstimated).toInt()

            timeSummaryProperty.set("$timeSummaryFirstHalf of estimated ${updatedIssue.timeEstimate}")
            progressPercentageProperty.set(percentComplete)
        } else {
            timeSummaryProperty.set(timeSummaryFirstHalf)
            progressPercentageProperty.set(0)
        }
    }

}