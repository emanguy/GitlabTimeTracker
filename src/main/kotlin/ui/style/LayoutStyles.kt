package edu.erittenhouse.gitlabtimetracker.ui.style

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class LayoutStyles : Stylesheet() {
    companion object {
        val typicalSpacing by cssclass()
    }

    init {
        typicalSpacing {
            padding = box(8.px)
            spacing = 8.px
        }
    }
}