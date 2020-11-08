package edu.erittenhouse.gitlabtimetracker.model.settings

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import kotlin.reflect.KClass
import edu.erittenhouse.gitlabtimetracker.model.settings.v0.Settings as V0Settings
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.Settings as V1Settings

/**
 * Represents a conversion from one object into another
 */
typealias Migration<P, C> = (P) -> C

/**
 * The newest migration version
 */
const val newestMigrationVersion = 1

/**
 * Converts a settings version to the actual class for that version
 */
val versionToSettings = mapOf<Int, KClass<out VersionedSettings>>(
    0 to V0Settings::class,
    1 to V1Settings::class,
)

/**
 * Returns an incremental migration for the current settings version into the next.
 *
 * A migration may return null if the incorrect input is provided.
 */
val settingsMigrations = mapOf<Int, Migration<VersionedSettings, out VersionedSettings?>>(
    0 to ::`v0 to v1`,
)

/**
 * Converts from settings V0 to V1
 */
fun `v0 to v1`(previousVersion: VersionedSettings): V1Settings? {
    val convertedSettings = previousVersion as? V0Settings ?: return null
    return V1Settings(
        gitlabCredentials = GitlabCredential(
            gitlabBaseURL = convertedSettings.gitlabBaseURL,
            personalAccessToken = convertedSettings.personalAccessToken,
        ),
        slackCredentials = null,
    )
}
