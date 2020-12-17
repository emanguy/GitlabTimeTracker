package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import javafx.scene.Node
import tornadofx.Fragment

abstract class SuspendingFragment private constructor(
    title: String?,
    icon: Node?,
    private val scopeImpl: UIScopeImpl
) : Fragment(title, icon), UIScope by scopeImpl {
    constructor(title: String? = null, icon: Node? = null) : this(title, icon, UIScopeImpl()) {
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