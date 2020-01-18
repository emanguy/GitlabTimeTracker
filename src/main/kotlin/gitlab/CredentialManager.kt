package edu.erittenhouse.gitlabtimetracker.gitlab

import com.fasterxml.jackson.module.kotlin.readValue
import edu.erittenhouse.gitlabtimetracker.gitlab.error.CredentialRetrieveError
import edu.erittenhouse.gitlabtimetracker.gitlab.error.CredentialSaveError
import edu.erittenhouse.gitlabtimetracker.util.JsonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CredentialManager(private val fileLocation: String = System.getProperty("user.home") + "/.gtt") {
    /**
     * Saves gitlab credentials to disk.
     *
     * @throws CredentialSaveError if there was a problem writing the credentials
     */
    suspend fun setCredential(credential: GitlabCredential) = withContext(Dispatchers.IO) {
        val credentialFile = File(fileLocation)
        try {
            if (!credentialFile.exists()) {
                credentialFile.createNewFile()
            }

            JsonMapper.writeValue(credentialFile, credential)
        } catch (e: Exception) {
            throw CredentialSaveError(e)
        }
    }

    /**
     * Tries to retrieve gitlab credentials from disk
     *
     * @return Gitlab credentials from disk or null if they were never saved
     * @throws CredentialRetrieveError if the data exists on disk but couldn't be read
     */
    suspend fun getCredential(): GitlabCredential? = withContext(Dispatchers.IO) {
        val credentialsFile = File(fileLocation)
        if (!credentialsFile.exists()) {
            return@withContext null
        }

        return@withContext try {
            JsonMapper.readValue<GitlabCredential>(credentialsFile)
        } catch (e: Exception) {
            throw CredentialRetrieveError(e)
        }
    }
}