package edu.erittenhouse.gitlabtimetracker.controller

import tornadofx.Component
import tornadofx.ScopedInstance

class StorageConfig(val fileLocation: String = System.getProperty("user.home") + "/.gtt") : Component(), ScopedInstance