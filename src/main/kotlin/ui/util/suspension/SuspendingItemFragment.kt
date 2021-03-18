package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import tornadofx.ItemFragment
import kotlin.coroutines.CoroutineContext

abstract class SuspendingItemFragment<T> private constructor(
    private val scopeImpl: UIScopeImpl
) : ItemFragment<T>(), UIScope by scopeImpl {
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
    override fun registerCoroutineExceptionHandler(coroutineExceptionHandleFunction: (CoroutineContext, Throwable) -> Unit) = scopeImpl.registerCoroutineExceptionHandler(coroutineExceptionHandleFunction)
}