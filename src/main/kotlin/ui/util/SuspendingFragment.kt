package edu.erittenhouse.gitlabtimetracker.ui.util

import javafx.scene.Node
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.Fragment

abstract class SuspendingFragment(title: String? = null, icon: Node? = null) : Fragment(title, icon), UIScope {
    private val ceh = CoroutineExceptionHandler { coroutineContext, throwable ->
        onUncaughtCoroutineException(coroutineContext, throwable)
    }
    override val coroutineContext = Dispatchers.JavaFx + SupervisorJob() + ceh

    override fun onUndock() {
        super.onUndock()

        // Don't allow async processes to continue running after the fragment is undocked
        this.coroutineContext.cancelChildren()
    }
}