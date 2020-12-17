package edu.erittenhouse.gitlabtimetracker.model.settings

import edu.erittenhouse.gitlabtimetracker.io.migrateSettings
import edu.erittenhouse.gitlabtimetracker.io.result.MigrationResult
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsVersionTest {

    @Test
    fun `Can update settings from v0`() {
        val settingsV0Content = """{"gitlabBaseURL": "https://fake.gitlab", "personalAccessToken": "jdoe-token"}""".toByteArray()
        val migratedSettingsResult = migrateSettings(settingsV0Content)
        assertTrue(migratedSettingsResult is MigrationResult.MigrationSucceeded)
    }

}