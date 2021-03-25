package edu.erittenhouse.gitlabtimetracker.ui.util

typealias PopupCallback = suspend () -> Unit
data class NamedCallback(val name: String, val callback: PopupCallback)

infix fun String.triggers(callback: PopupCallback): NamedCallback = NamedCallback(this, callback)
