package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.ItemFragment

abstract class SuspendingItemFragment<T> : ItemFragment<T>(), UIScope {
    private val ceh = CoroutineExceptionHandler(::onUncaughtCoroutineException)
    override val coroutineContext = Dispatchers.JavaFx + SupervisorJob() + ceh

    override fun onUndock() {
        super.onUndock()
        coroutineContext.cancelChildren()
    }
}