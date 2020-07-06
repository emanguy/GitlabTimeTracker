package edu.erittenhouse.gitlabtimetracker.ui.util

import edu.erittenhouse.gitlabtimetracker.ui.fragment.ErrorFragment
import tornadofx.Fragment
import tornadofx.View

private const val DEFAULT_ERROR_MSG = "Something went wrong... not sure what though"

fun View.showErrorModal(errorMessage: String?) {
    val nonNullMessage = errorMessage ?: DEFAULT_ERROR_MSG
    find<ErrorFragment>("errorMessage" to nonNullMessage).openModal()
}

fun Fragment.showErrorModal(errorMessage: String?) {
    val nonNullMessage = errorMessage ?: DEFAULT_ERROR_MSG
    find<ErrorFragment>("errorMessage" to nonNullMessage).openModal()
}