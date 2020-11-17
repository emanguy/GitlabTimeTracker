package edu.erittenhouse.gitlabtimetracker.ui.style

import tornadofx.*

class TypographyStyles : Stylesheet() {
    companion object {
        val title by cssclass()
        val subtitle by cssclass()
        val metadata by cssclass()
    }

    init {
        hyperlink {
            padding = box(0.px)
        }
        title {
            fontSize = 20.px
        }
        subtitle {
            fontSize = 15.px
        }
        metadata {
            fill = c("#717171")
        }
    }
}