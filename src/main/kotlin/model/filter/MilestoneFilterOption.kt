package edu.erittenhouse.gitlabtimetracker.model.filter

import edu.erittenhouse.gitlabtimetracker.model.Milestone

sealed class MilestoneFilterOption {
    abstract val milestoneOptionText: String

    object NoMilestoneOptionSelected : MilestoneFilterOption() {
        override val milestoneOptionText = "No milestone selected"
    }
    object HasNoMilestone : MilestoneFilterOption() {
        override val milestoneOptionText = "No Assigned Milestone"
    }
    object HasAssignedMilestone : MilestoneFilterOption() {
        override val milestoneOptionText = "Has Assigned Milestone"
    }
    data class SelectedMilestone(val milestone: Milestone) : MilestoneFilterOption() {
        override val milestoneOptionText: String
            get() = milestone.title
    }
}




