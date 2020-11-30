package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import tornadofx.ItemFragment

abstract class SuspendingItemFragment<T> private constructor(
    private val scopeImpl: UIScopeImpl
) : ItemFragment<T>(), UIScope by scopeImpl {
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