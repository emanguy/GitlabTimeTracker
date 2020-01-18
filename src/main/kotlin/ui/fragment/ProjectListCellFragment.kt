package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.model.Project
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import tornadofx.*

class ProjectListCellFragment : ListCellFragment<Project>() {
    private val projectText = SimpleStringProperty("")
    private val projectDescription = SimpleStringProperty("")
    private val projectGitlabPath = SimpleStringProperty("")
    private var projectUrl: String = ""

    init {
        itemProperty.onChange { updateProperties(it) }
    }

    private fun updateProperties(newProject: Project?) {
        if (newProject == null) return
        projectText.set("Project: ${newProject.name}")
        setTruncatedDescription(newProject.description)
        projectGitlabPath.set(newProject.gitlabPath)
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
        addClass(LayoutStyles.typicalSpacing)
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
            style {
                prefWidth = 100.percent
            }
            text(projectGitlabPath) {
                addClass(TypographyStyles.subtitle)
            }
            region {
                hgrow = Priority.ALWAYS
            }
            hyperlink("Go to project") {
                action {
                    hostServices.showDocument(projectUrl)
                }
            }
        }
    }
}