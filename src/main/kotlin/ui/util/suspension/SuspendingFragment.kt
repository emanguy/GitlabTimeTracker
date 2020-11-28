package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import javafx.scene.Node
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.Fragment

abstract class SuspendingFragment(title: String? = null, icon: Node? = null) : Fragment(title, icon), UIScope {
    override var backgroundTasksStarted = false
    private val ceh = CoroutineExceptionHandler { coroutineContext, throwable ->
        onUncaughtCoroutineException(coroutineContext, throwable)
    }
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