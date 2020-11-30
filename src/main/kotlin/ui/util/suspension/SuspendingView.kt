package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import javafx.scene.Node
import tornadofx.View

abstract class SuspendingView private constructor(
    title: String?,
    icon: Node?,
    private val scopeImpl: UIScopeImpl,
) : View(title, icon), UIScope by scopeImpl {
    private var backgroundTasksStarted = false

    constructor(title: String? = null, icon: Node? = null) : this(title, icon, UIScopeImpl()) {
        scopeImpl.registerComponent(this)
    }

    override fun onDock() {
        super.onDock()
        if (backgroundTasksStarted) return
        backgroundTasksStarted = true
        startBackgroundTasks()
    }
    override fun onUndock() {
        super.onUndock()
        if (!backgroundTasksStarted) return
        backgroundTasksStarted = false
        viewClosing()
    }
}