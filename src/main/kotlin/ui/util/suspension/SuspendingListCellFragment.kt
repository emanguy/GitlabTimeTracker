package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.ListCellFragment

abstract class SuspendingListCellFragment<T> : ListCellFragment<T>(), UIScope {
    private val ceh = CoroutineExceptionHandler { context, exception ->
        this.onUncaughtCoroutineException(context, exception)
    }
    override val coroutineContext = Dispatchers.JavaFx + SupervisorJob() + ceh

    override fun onUndock() {
        super.onUndock()

        // Cancel running coroutines, but not the job on the SuspendingView
        // That way this scope's job is still valid, so if the view is reattached we can still run new coroutines
        this.coroutineContext.cancelChildren()
    }
}