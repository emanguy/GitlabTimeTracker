package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.model.Project
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import tornadofx.Controller

class IssueController : Controller() {
    val selectedProject = SimpleObjectProperty<Project>()

    suspend fun selectProject(project: Project) {
        withContext(Dispatchers.JavaFx) {
            selectedProject.set(project)
        }
    }
}