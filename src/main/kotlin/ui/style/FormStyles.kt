package edu.erittenhouse.gitlabtimetracker.ui.style

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass

class FormStyles : Stylesheet() {
    companion object {
        val fieldInvalid by cssclass()
    }

    init {
        fieldInvalid {
            borderColor += box(Color.ORANGERED)
        }
    }
}