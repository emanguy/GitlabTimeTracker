package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import javafx.event.EventTarget
import tornadofx.ListCellFragment
import tornadofx.UIComponent

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
    override fun <T> EventTarget.scopeAdd(child: T)
            where T : UIComponent,
                  T : UIScope {
        val evtTarget = this
        with (scopeImpl) {
            evtTarget.scopeAdd(child)
        }
    }
}