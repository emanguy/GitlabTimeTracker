@file:Suppress("FunctionName")

package edu.erittenhouse.gitlabtimetracker.model.settings

import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import kotlin.reflect.KClass
import edu.erittenhouse.gitlabtimetracker.model.settings.v0.Settings as V0Settings
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.Settings as V1Settings
import edu.erittenhouse.gitlabtimetracker.model.settings.v2.Settings as V2Settings
import edu.erittenhouse.gitlabtimetracker.model.settings.v2.SlackConfig as V2SlackConfig

/**
 * Represents a conversion from one object into another
 */
typealias Migration<P, C> = (P) -> C

/**
 * The newest migration version
 */
const val newestMigrationVersion = 2

/**
 * Converts a settings version to the actual class for that version
 */
@Suppress("RemoveExplicitTypeArguments")
val versionToSettings = mapOf<Int, KClass<out VersionedSettings>>(
    0 to V0Settings::class,
    1 to V1Settings::class,
    2 to V2Settings::class,
)

/**
 * Returns an incremental migration for the current settings version into the next.
 *
 * A migration may return null if the incorrect input is provided.
 */
val settingsMigrations = mapOf<Int, Migration<VersionedSettings, out VersionedSettings?>>(
    0 to ::`v0 to v1`,
    1 to ::`v1 to v2`,
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
        slackConfig = null,
        slackEnabled = false,
    )
}

/**
 * Converts from settings V1 to V2
 */
fun `v1 to v2`(previousVersion: VersionedSettings): V2Settings? {
    val convertedSettings = previousVersion as? V1Settings ?: return null
    return V2Settings(
        gitlabCredentials = convertedSettings.gitlabCredentials,
        slackConfig = convertedSettings.slackConfig?.let { previousSlackCfg ->
            V2SlackConfig(
                credentialAndTeam = previousSlackCfg.credentialAndTeam,
                statusEmoji = previousSlackCfg.statusEmoji,
                slackStatusFormat = previousSlackCfg.slackStatusFormat,
            )
        },
        slackEnabled = convertedSettings.slackEnabled,
        pinnedProjectIDs = emptySet(),
    )
}
