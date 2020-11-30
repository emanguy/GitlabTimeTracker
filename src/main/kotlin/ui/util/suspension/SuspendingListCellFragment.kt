package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import tornadofx.ListCellFragment

abstract class SuspendingListCellFragment<T> private constructor(
    private val scopeImpl: UIScopeImpl
) : ListCellFragment<T>(), UIScope by scopeImpl {
    private var backgroundTasksStarted = false

    constructor() : this(UIScopeImpl()) {
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