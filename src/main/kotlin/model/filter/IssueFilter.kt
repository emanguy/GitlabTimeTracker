package edu.erittenhouse.gitlabtimetracker.model.filter

/**
 * Defines filters being applied to the list of issues
 */
data class IssueFilter(
    val filterText: String = "",
    val selectedMilestone: MilestoneFilterOption = NoMilestoneOptionSelected
)
