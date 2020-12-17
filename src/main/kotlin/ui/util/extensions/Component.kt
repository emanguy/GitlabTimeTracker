package edu.erittenhouse.gitlabtimetracker.ui.util.extensions

import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.controlsfx.control.ToggleSwitch
import tornadofx.hgrow
import tornadofx.opcr
import tornadofx.region

/**
 * Creates a region that expands to fill space. Useful for pushing UI components against the edges
 * of their parent container.
 */
fun EventTarget.flexspacer(additionalConfig: Region.() -> Unit = {}) = region {
    hgrow = Priority.ALWAYS
    additionalConfig()
}

/**
 * Creates a toggle switch, optionally with a [label].
 */
fun EventTarget.toggleswitch(label: String? = null, additionalConfig: ToggleSwitch.() -> Unit = {}): ToggleSwitch {
    val switch =  if (label == null) {
        ToggleSwitch()
    } else {
        ToggleSwitch(label)
    }

    opcr(this, switch, additionalConfig)
    return switch
}