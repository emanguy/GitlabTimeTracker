package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.model.Project
import edu.erittenhouse.gitlabtimetracker.ui.style.Images
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexspacer
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingListCellFragment
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ToggleButton
import tornadofx.*

class ProjectListCellFragment : SuspendingListCellFragment<Project>() {
    private val projectText = SimpleStringProperty("")
    private val projectDescription = SimpleStringProperty("")
    private val projectGitlabPath = SimpleStringProperty("")
    private var projectID: Int = -1
    private var projectUrl: String = ""
    private var projectPinned = false

    private val projectController by inject<ProjectController>()
    private var pinToggle by singleAssign<ToggleButton>()

    init {
        itemProperty.onChange { updateProperties(it) }
    }

    private fun updateProperties(newProject: Project?) {
        if (newProject == null) return
        projectText.set("Project: ${newProject.name}")
        setTruncatedDescription(newProject.description)
        projectGitlabPath.set(newProject.gitlabPath)
        projectPinned = newProject.pinned
        pinToggle.isSelected = newProject.pinned
        projectID = newProject.id
        projectUrl = newProject.url.toString()
    }

    private fun setTruncatedDescription(description: String) {
        val ellipsized = description.length > 200

        if (ellipsized) {
            projectDescription.set(description.substring(0, 197) + "...")
        } else {
            projectDescription.set(description)
        }
    }

    override val root = vbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)
        text(projectText) {
            addClass(TypographyStyles.title)
        }
        text(projectDescription) {
            style {
                wrapText = true
                wrappingWidth = 300.0
            }
        }
        hbox {
            addClass(LayoutStyles.centerAlignLeft)
            style {
                prefWidth = 100.percent
            }
            text(projectGitlabPath) {
                addClass(TypographyStyles.metadata)
            }
            flexspacer()
            pinToggle = togglebutton {
                tooltip("Pin/unpin project")
                imageview(Images.pin)

                suspendingAction {
                    val currentlyPinned = projectPinned
                    if (currentlyPinned) projectController.unpinProject(projectID) else projectController.pinProject(projectID)
                }
            }
            button {
                tooltip("Go to project")
                imageview(Images.newWindow)
                action {
                    hostServices.showDocument(projectUrl)
                }
            }
        }
    }
}