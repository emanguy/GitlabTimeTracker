package edu.erittenhouse.gitlabtimetracker.io

import com.fasterxml.jackson.module.kotlin.readValue
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import edu.erittenhouse.gitlabtimetracker.gitlab.error.CredentialIOError
import edu.erittenhouse.gitlabtimetracker.util.JsonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CredentialManager(private val fileLocation: String = System.getProperty("user.home") + "/.gtt") {
    /**
     * Saves gitlab credentials to disk.
     *
     * @return The status of the credential save operation
     * @throws CredentialIOError if the disk operation fails
     */
    suspend fun setCredential(credential: GitlabCredential): Unit = withContext(Dispatchers.IO) {
        val credentialFile = File(fileLocation)
        try {
            if (!credentialFile.exists()) {
                credentialFile.createNewFile()
            }

            JsonMapper.writeValue(credentialFile, credential)
        } catch (e: Exception) {
            throw CredentialIOError(fileLocation, "Failed to save your credentials.", e)
        }
    }

    /**
     * Tries to retrieve gitlab credentials from disk
     *
     * @return Gitlab credentials from disk or null if they were never saved
     * @throws CredentialIOError if the data exists on disk but couldn't be read
     */
    suspend fun getCredential(): GitlabCredential? = withContext(Dispatchers.IO) {
        val credentialsFile = File(fileLocation)
        if (!credentialsFile.exists()) {
            return@withContext null
        }

        try {
            return@withContext JsonMapper.readValue<GitlabCredential>(credentialsFile)
        } catch (e: Exception) {
            throw CredentialIOError(fileLocation, "Failed to retrieve saved GitLab credentials.", e)
        }
    }
}