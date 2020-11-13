package edu.erittenhouse.gitlabtimetracker.io

import com.fasterxml.jackson.module.kotlin.readValue
import edu.erittenhouse.gitlabtimetracker.io.error.SettingsErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.defaultSettings
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.Settings
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.SlackConfig
import edu.erittenhouse.gitlabtimetracker.util.JsonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class SettingsManager(private val fileLocation: String = System.getProperty("user.home") + "/.gtt") {
    private companion object FileAccess {
        private val fileMutexes = mutableMapOf<String, Mutex>()
        private val mutexMapMutex = Mutex()
        private val statesByFile = mutableMapOf<String, Settings>()

        private suspend inline fun <T> lockingSettingsForFile(fileName: String, action: () -> T): T {
            val mutexForFile = mutexMapMutex.withLock {
                fileMutexes.getOrPut(fileName) { Mutex() }
            }

            return mutexForFile.withLock(action = action)
        }

        /**
         * Fetches the cached settings from memory, falling back on and caching the value from
         * disk if not in memory, finally returning null if the settings cannot be retrieved
         *
         * @throws SettingsErrors.DiskIOError if the data exists on disk but couldn't be read
         */
        suspend fun fetchSettings(fileName: String): Settings? {
            return lockingSettingsForFile(fileName) {
                if (statesByFile[fileName] != null) return statesByFile[fileName]

                return@lockingSettingsForFile withContext(Dispatchers.IO) {
                    val settingsFile = File(fileName)
                    if (!settingsFile.exists()) return@withContext null

                    val settings = try {
                        JsonMapper.readValue<Settings>(settingsFile)
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
        suspend fun saveSettings(fileName: String, settings: Settings): Unit = withContext(Dispatchers.IO) {
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
     * Saves gitlab credentials to disk.
     *
     * @throws SettingsErrors.DiskIOError if the disk operation fails
     */
    suspend fun setCredential(credential: GitlabCredential) {
        val currentSettings = fetchSettings(fileLocation)
        val newSettings = currentSettings?.copy(gitlabCredentials = credential) ?: defaultSettings(credential)
        saveSettings(fileLocation, newSettings)
    }

    /**
     * Saves slack configuration to disk.
     *
     * @throws SettingsErrors.DiskIOError if the disk operation fails
     * @throws SettingsErrors.RequiredMissingError if required settings have not yet been set, such as gitlab credentials
     */
    suspend fun setSlackConfig(credential: SlackConfig?) {
        val currentSettings = fetchSettings(fileLocation) ?: throw SettingsErrors.RequiredMissingError()
        val newSettings = currentSettings.copy(slackConfig = credential)
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
    suspend fun getSlackConfig(): SlackConfig? = fetchSettings(fileLocation)?.slackConfig
}