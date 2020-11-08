package edu.erittenhouse.gitlabtimetracker.io.result

import edu.erittenhouse.gitlabtimetracker.model.settings.VersionedSettings

sealed class FileMigrationResult {
    object MigrationSucceeded : FileMigrationResult()
    object AlreadyOnLatestVersion : FileMigrationResult()
    object VersionTooNew : FileMigrationResult()
    object BadVersion : FileMigrationResult()
    object FileDoesNotExist : FileMigrationResult()
    data class MigrationProducedUnexpectedModel(val modelVersion: Int) : FileMigrationResult()
    data class MigrationMissing(val fromVersion: Int) : FileMigrationResult()
}

sealed class MigrationResult <in T: VersionedSettings> {
    data class MigrationSucceeded<T: VersionedSettings>(val migratedSettings: T) : MigrationResult<T>()
    object AlreadyOnLatestVersion : MigrationResult<VersionedSettings>()
    object VersionTooNew : MigrationResult<VersionedSettings>()
    object BadVersion : MigrationResult<VersionedSettings>()
    data class MigrationProducedUnexpectedModel(val modelVersion: Int) : MigrationResult<VersionedSettings>()
    data class MigrationMissing(val fromVersion: Int) : MigrationResult<VersionedSettings>()
}