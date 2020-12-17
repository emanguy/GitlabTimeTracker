package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import tornadofx.ListCellFragment

abstract class SuspendingListCellFragment<T> private constructor(
    private val scopeImpl: UIScopeImpl
) : ListCellFragment<T>(), UIScope by scopeImpl {
    constructor() : this(UIScopeImpl()) {
        @Suppress("LeakingThis")
        scopeImpl.registerComponent(this)
    }

    override fun onDock() {
        super.onDock()
        triggerBackgroundTasks()
    }
    override fun onUndock() {
        super.onUndock()
        triggerViewClosing()
    }
    override fun registerBackgroundTaskInit(backgroundTaskInitFunction: () -> Unit) = scopeImpl.registerBackgroundTaskInit(backgroundTaskInitFunction)
    override fun registerBackgroundTaskCleanup(backgroundTaskCleanupFunction: () -> Unit) = scopeImpl.registerBackgroundTaskCleanup(backgroundTaskCleanupFunction)
}