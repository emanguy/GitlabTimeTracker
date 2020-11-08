package edu.erittenhouse.gitlabtimetracker.io

import com.fasterxml.jackson.module.kotlin.readValue
import edu.erittenhouse.gitlabtimetracker.io.error.SettingsIOError
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.model.settings.v1.Settings
import edu.erittenhouse.gitlabtimetracker.util.JsonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class SettingsManager(private val fileLocation: String = System.getProperty("user.home") + "/.gtt") {
    private var currentSettingsState: Settings? = null
    private val settingsStateMutex = Mutex()

    /**
     * Saves gitlab credentials to disk.
     *
     * @return The status of the credential save operation
     * @throws SettingsIOError if the disk operation fails
     */
    suspend fun setCredential(credential: GitlabCredential): Unit = withContext(Dispatchers.IO) {
        val credentialFile = File(fileLocation)
        try {
            if (!credentialFile.exists()) {
                credentialFile.createNewFile()
            }

            JsonMapper.writeValue(credentialFile, credential)
        } catch (e: Exception) {
            throw SettingsIOError(fileLocation, "Failed to save your credentials.", e)
        }
    }

    /**
     * Tries to retrieve gitlab credentials from disk
     *
     * @return Gitlab credentials from disk or null if they were never saved
     * @throws SettingsIOError if the data exists on disk but couldn't be read
     */
    suspend fun getCredential(): GitlabCredential? = withContext(Dispatchers.IO) {
        val credentialsFile = File(fileLocation)
        if (!credentialsFile.exists()) {
            return@withContext null
        }

        try {
            return@withContext JsonMapper.readValue<GitlabCredential>(credentialsFile)
        } catch (e: Exception) {
            throw SettingsIOError(fileLocation, "Failed to retrieve saved GitLab credentials.", e)
        }
    }

    /**
     * Fetches the cached settings from memory, falling back on and caching the value from
     * disk if not in memory, finally returning null if the settings cannot be retrieved
     *
     * @throws SettingsIOError if the data exists on disk but couldn't be read
     */
    private suspend fun fetchSettings(): Settings? {
        if (currentSettingsState != null) return currentSettingsState

        return withContext(Dispatchers.IO) {
            val settingsFile = File(fileLocation)
            if (!settingsFile.exists()) return@withContext null

            return@withContext settingsStateMutex.withLock {
                val settings = try {
                    JsonMapper.readValue<Settings>(settingsFile)
                } catch (e: Exception) {
                    throw SettingsIOError(fileLocation, "Failed to retrieve saved settings.", e)
                }

                currentSettingsState = settings
                return@withLock settings
            }
        }
    }

    private suspend fun saveSettings(settings: Settings): Unit = withContext(Dispatchers.IO) {

    }
}