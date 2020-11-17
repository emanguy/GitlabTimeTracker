package edu.erittenhouse.gitlabtimetracker.io

import edu.erittenhouse.gitlabtimetracker.io.error.SettingsErrors
import edu.erittenhouse.gitlabtimetracker.model.GitlabCredential
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class SettingsManagerTest {
    private val fileLocation = "./credentials"
    private val manager = SettingsManager(fileLocation)

    @AfterEach
    fun `Remove stored data`() {
        val credsFile = File(fileLocation)
        if (credsFile.exists()) {
            credsFile.delete()
        }
    }

    @Test
    fun `Can write credentials to disk`() {
        runBlocking {
            val credential = GitlabCredential("url", "token")
            manager.setCredential(credential)

            val credsFile = File(fileLocation)
            assertTrue { credsFile.exists() }
            assertTrue { credsFile.readText().isNotEmpty() }
        }
    }

    @Test
    fun `Returns no credentials when nothing saved`() {
        runBlocking {
            assertEquals(null, manager.getCredential())
        }
    }

    @Test
    fun `Throws exception when trying to read an unreadable config file`() {
        val credsFile = File(fileLocation)
        credsFile.writeText("abcde")

        runBlocking {
            try {
                manager.getCredential()
                fail<Unit>("Should have thrown exception")
            } catch (e: SettingsErrors.DiskIOError) {
                println("Exception thrown.")
            } catch (e: Exception) {
                fail<Unit>("Threw wrong exception")
            }
        }
    }
}