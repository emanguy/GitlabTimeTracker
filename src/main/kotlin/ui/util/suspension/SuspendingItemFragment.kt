package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.ItemFragment

abstract class SuspendingItemFragment<T> : ItemFragment<T>(), UIScope {
    override var backgroundTasksStarted = false
    private val ceh = CoroutineExceptionHandler(::onUncaughtCoroutineException)
    override val coroutineContext = Dispatchers.JavaFx + SupervisorJob() + ceh

    override fun getChildUiScopes(): List<UIScope> = deepGetChildUIScopes(root)
    override fun onDock() {
        super.onDock()
        startBackgroundTasks()
    }
    override fun onUndock() {
        super.onUndock()
        viewClosing()
    }
}