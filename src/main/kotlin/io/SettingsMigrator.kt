package edu.erittenhouse.gitlabtimetracker.io

import com.fasterxml.jackson.module.kotlin.readValue
import edu.erittenhouse.gitlabtimetracker.io.error.SettingsIOError
import edu.erittenhouse.gitlabtimetracker.io.error.SettingsParseError
import edu.erittenhouse.gitlabtimetracker.io.result.FileMigrationResult
import edu.erittenhouse.gitlabtimetracker.io.result.MigrationResult
import edu.erittenhouse.gitlabtimetracker.model.settings.VersionedSettings
import edu.erittenhouse.gitlabtimetracker.model.settings.newestMigrationVersion
import edu.erittenhouse.gitlabtimetracker.model.settings.settingsMigrations
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.Settings
import edu.erittenhouse.gitlabtimetracker.model.settings.versionToSettings
import edu.erittenhouse.gitlabtimetracker.util.JsonMapper
import java.io.File

fun migrateSettingsFile(fileLocation: String): FileMigrationResult {
    // Set up the file and verify it exists
    val file = File(fileLocation)
    if (!file.exists()) return FileMigrationResult.FileDoesNotExist

    // Read the contents of the file
    val fileContent = try {
        file.readBytes()
    } catch (e: Exception) {
        throw SettingsIOError(fileLocation, "Could not read settings file for migration.", e)
    }

    // Perform migration
    val migratedSettings = when (val result = migrateSettings(fileContent)) {
        is MigrationResult.AlreadyOnLatestVersion -> return FileMigrationResult.AlreadyOnLatestVersion
        is MigrationResult.BadVersion -> return FileMigrationResult.BadVersion
        is MigrationResult.VersionTooNew -> return FileMigrationResult.VersionTooNew
        is MigrationResult.MigrationMissing -> return FileMigrationResult.MigrationMissing(result.fromVersion)
        is MigrationResult.MigrationProducedUnexpectedModel -> return FileMigrationResult.MigrationProducedUnexpectedModel(result.modelVersion)
        is MigrationResult.MigrationSucceeded -> result.migratedSettings
    }

    // Write migrated result to file and continue
    try {
        JsonMapper.writeValue(file, migratedSettings)
    } catch (e: Exception) {
        throw SettingsIOError(fileLocation, "Failed to write migrated settings to file", e)
    }
    return FileMigrationResult.MigrationSucceeded
}

/**
 * Accepts the current contents of the settings file, then attempts to convert to the latest settings
 * format if necessary.
 *
 * @throws SettingsParseError if the settings could not be parsed from the file content
 */
fun migrateSettings(fileContent: ByteArray): MigrationResult<Settings> {
    val versionedSettings = try {
        JsonMapper.readValue<VersionedSettings>(fileContent)
    } catch(e: Exception) {
        throw SettingsParseError("Failed to retrieve settings version.", e)
    }

    // Verify that we're not trying to migrate a newer version
    if (versionedSettings.version > newestMigrationVersion) return MigrationResult.VersionTooNew
    // If we're already on the latest version, report that
    if (versionedSettings.version == newestMigrationVersion) return MigrationResult.AlreadyOnLatestVersion

    // Read in the current settings
    val settingsClass = versionToSettings[versionedSettings.version] ?: return MigrationResult.BadVersion
    var currentSettings = try {
        JsonMapper.readValue(fileContent, settingsClass.java)
    } catch(e: Exception) {
        throw SettingsParseError("Failed to read current version of settings for migration.", e)
    }


    // Migrate to latest settings version (if necessary)
    while (currentSettings.version < newestMigrationVersion) {
        val migration = settingsMigrations[currentSettings.version]
            ?: return MigrationResult.MigrationMissing(currentSettings.version)
        val convertedSettings = migration(currentSettings)
            ?: return MigrationResult.MigrationProducedUnexpectedModel(currentSettings.version)
        currentSettings = convertedSettings
    }

    // Verify we got the correct settings type
    val convertedSettings = currentSettings as? Settings
        ?: return MigrationResult.MigrationProducedUnexpectedModel(currentSettings.version)
    return MigrationResult.MigrationSucceeded(convertedSettings)
}
