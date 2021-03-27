package edu.erittenhouse.gitlabtimetracker.io

import com.fasterxml.jackson.module.kotlin.readValue
import edu.erittenhouse.gitlabtimetracker.io.error.SettingsErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.NewestSettings
import edu.erittenhouse.gitlabtimetracker.model.settings.NewestSlackConfig
import edu.erittenhouse.gitlabtimetracker.model.settings.defaultSettings
import edu.erittenhouse.gitlabtimetracker.util.JsonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The settings manager provides an interface for interacting with the settings file in the user's home directory.
 * All functions here should be completely main-thread-safe, as they switch to the IO dispatcher whenever file interaction
 * is required.
 */
class SettingsManager(private val fileLocation: String = System.getProperty("user.home") + "/.gtt") {
    private companion object FileAccess {
        private val fileMutexes = mutableMapOf<String, Mutex>()
        private val mutexMapMutex = Mutex()
        private val statesByFile = mutableMapOf<String, NewestSettings>()

        private suspend inline fun <T> lockingSettingsForFile(fileName: String, action: () -> T): T {
            val mutexForFile = mutexMapMutex.withLock {
                fileMutexes.getOrPut(fileName) { Mutex() }
            }

            return mutexForFile.withLock(action = action)
        }

        /**
         * Clears the in-memory cache for the given file
         */
        suspend fun clearCache(fileName: String) {
            lockingSettingsForFile(fileName) {
                statesByFile.remove(fileName)
            }
        }

        /**
         * Fetches the cached settings from memory, falling back on and caching the value from
         * disk if not in memory, finally returning null if the settings cannot be retrieved
         *
         * @throws SettingsErrors.DiskIOError if the data exists on disk but couldn't be read
         */
        suspend fun fetchSettings(fileName: String): NewestSettings? {
            return lockingSettingsForFile(fileName) {
                if (statesByFile[fileName] != null) return statesByFile[fileName]

                return@lockingSettingsForFile withContext(Dispatchers.IO) {
                    val settingsFile = File(fileName)
                    if (!settingsFile.exists()) return@withContext null

                    val settings = try {
                        JsonMapper.readValue<NewestSettings>(settingsFile)
                    } catch (e: Exception) {
                        throw SettingsErrors.DiskIOError(fileName, "Failed to retrieve saved settings.", e)
                    }

                    statesByFile[fileName] = settings
                    return@withContext settings
                }
            }
        }

        /**
         * Saves new settings to disk and updates the settings in memory.
         *
         * @throws SettingsErrors.DiskIOError if persisting the settings to disk fails
         */
        suspend fun saveSettings(fileName: String, settings: NewestSettings): Unit = withContext(Dispatchers.IO) {
            lockingSettingsForFile(fileName) {
                statesByFile[fileName] = settings

                try {
                    val settingsFile = File(fileName)
                    if (!settingsFile.exists()) settingsFile.createNewFile()

                    JsonMapper.writeValue(settingsFile, settings)
                } catch (e: Exception) {
                    throw SettingsErrors.DiskIOError(fileName, "Failed to save settings to disk.")
                }
            }
        }
    }

    /**
     * Clears the in-memory cache for this settings manager.
     */
    suspend fun clearCache() {
        FileAccess.clearCache(fileLocation)
    }
    /**
     * Saves gitlab credentials to disk.
     *
     * @throws SettingsErrors.DiskIOError if the disk operation fails
     */
    suspend fun setCredential(credential: GitlabCredential) {
        val currentSettings = fetchSettings(fileLocation)

        // If the base URL on the credential changed, we should clear out the list of pinned projects
        val pinnedProjectIDList = if (currentSettings?.gitlabCredentials?.gitlabBaseURL != credential.gitlabBaseURL) {
            emptySet()
        } else {
            currentSettings.pinnedProjectIDs
        }

        val newSettings = currentSettings?.copy(gitlabCredentials = credential, pinnedProjectIDs = pinnedProjectIDList) ?: defaultSettings(credential)
        saveSettings(fileLocation, newSettings)
    }

    /**
     * Saves slack configuration to disk.
     *
     * @throws SettingsErrors.DiskIOError if the disk operation fails
     * @throws SettingsErrors.RequiredMissingError if required settings have not yet been set, such as gitlab credentials
     */
    suspend fun setSlackConfig(slackEnabled: Boolean, credential: NewestSlackConfig? = null) {
        val currentSettings = fetchSettings(fileLocation) ?: throw SettingsErrors.RequiredMissingError()
        val newSettings = currentSettings.copy(slackConfig = credential ?: currentSettings.slackConfig, slackEnabled = slackEnabled)
        saveSettings(fileLocation, newSettings)
    }

    /**
     * Saves the current list of pinned projects to disk.
     *
     * @throws SettingsErrors.DiskIOError if the disk write operation fails
     * @throws SettingsErrors.RequiredMissingError if the required settings have not yet been set, such as gitlab credentials
     */
    suspend fun setPinnedProjects(pinnedProjects: Set<Int>) {
        val currentSettings = fetchSettings(fileLocation) ?: throw SettingsErrors.RequiredMissingError()
        val newSettings = currentSettings.copy(pinnedProjectIDs = pinnedProjects)
        saveSettings(fileLocation, newSettings)
    }

    /**
     * Tries to retrieve gitlab credentials from disk, or in-memory cache if applicable
     *
     * @return Gitlab credentials from disk or null if they were never saved
     * @throws SettingsErrors.DiskIOError if the data exists on disk but couldn't be read
     */
    suspend fun getCredential(): GitlabCredential? = fetchSettings(fileLocation)?.gitlabCredentials

    /**
     * Tries to retrieve slack configuration from disk, or in-memory cache if applicable
     *
     * @return Slack credentials from disk or null if user did not sign in with slack
     * @throws SettingsErrors.DiskIOError if data exists on disk but couldn't be read
     */
    suspend fun getSlackConfig(): NewestSlackConfig? = fetchSettings(fileLocation)?.slackConfig

    /**
     * Tries to retrieve whether or not slack integration is enabled.
     * @return True if slack integration is enabled
     * @throws SettingsErrors.DiskIOError if data exists on disk but couldn't be read
     */
    suspend fun getSlackEnabled(): Boolean = fetchSettings(fileLocation)?.slackEnabled ?: false

    /**
     * Tries to retrieve the list of pinned projects.
     *
     * @return The list of project IDs that are pinned, if any
     * @throws SettingsErrors.DiskIOError if data exists on the disk but couldn't be read
     */
    suspend fun getPinnedProjects(): Set<Int> = fetchSettings(fileLocation)?.pinnedProjectIDs ?: emptySet()
}