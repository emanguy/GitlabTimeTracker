package edu.erittenhouse.gitlabtimetracker.ui.style

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.cssclass

class ProgressStyles : Stylesheet() {
    companion object {
        val redBar by cssclass()
    }

    init {
        redBar {
            accentColor = Color.RED
        }
    }
}