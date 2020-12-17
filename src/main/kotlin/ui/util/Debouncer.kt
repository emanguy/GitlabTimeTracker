package edu.erittenhouse.gitlabtimetracker.ui.util

/**
 * Helps debounce invocations of a function.
 *
 * @param debounceTime The amount of time to wait before allowing another invocation of runDebounced()
 */
class Debouncer(private val debounceTime: Long = EXCEPTION_DEBOUNCE_MILLIS) {
    private var lastHandleTime = Long.MIN_VALUE

    /**
     * RunDebounced will prevent invocation of the passed lambda if this function is
     * called too quickly. That is, an invocation of the lambda can only occur once
     * every [debounceTime] milliseconds.
     */
    fun runDebounced(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        if (lastHandleTime + debounceTime <= currentTime) {
            action()
            lastHandleTime = currentTime
        }
    }
}