package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.error.WTFError
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.model.IssueWithTime
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingController
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Instant
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder

@UseExperimental(ExperimentalCoroutinesApi::class)
class TimeRecordingController : SuspendingController() {
    // We only want to allow one ticker job to run at a time. No racing to launch the job!
    private val tickerJobMutex = Mutex()
    private var tickerJob: Job? = null
    private val stopTrigger = Channel<CompletableDeferred<IssueWithTime>>()
    private val timeFormatter = PeriodFormatterBuilder()
        .appendDays()
        .appendSuffix(" day ", " days ")
        .minimumPrintedDigits(2)
        .printZeroAlways()
        .appendHours()
        .appendSuffix(":")
        .appendMinutes()
        .appendSuffix(":")
        .appendSeconds()
        .toFormatter()

    // RecordingIssueProperty will be null when not recording
    val recordingIssueProperty = SimpleObjectProperty<Issue?>(null)
    val timeSpentProperty = SimpleStringProperty("00:00:00")

    suspend fun startTiming(issue: Issue) {
        tickerJobMutex.withLock {
            val currentTickerJobValue = tickerJob
            if (currentTickerJobValue != null) return

            tickerJob = launch { updateTime(issue) }
            recordingIssueProperty.set(issue)
        }
    }

    suspend fun stopTiming(): IssueWithTime? {
        tickerJobMutex.withLock {
            val currentTickerJobValue = tickerJob ?: return null
            val recordedTime = CompletableDeferred<IssueWithTime>()
            stopTrigger.send(recordedTime)

            val timingResult = withTimeoutOrNull(2000) {
                recordedTime.await()
            }

            if (timingResult == null) {
                currentTickerJobValue.cancel()
                tickerJob = null
                throw WTFError("Failed to retrieve time from ticker job!")
            } else {

            }
        }
    }

    private suspend fun updateTime(issue: Issue) {
        val startTime = Instant()

        while (isActive) {
            val durationRequest = select<CompletableDeferred<IssueWithTime>?> {
                stopTrigger.onReceive { it }
                onTimeout(1000) { null }
            }

            val recordingTime = Period(startTime, Instant())
            val timeSpent = recordingTime.normalizedStandard().toString(timeFormatter)
            withContext(Dispatchers.JavaFx) {
                timeSpentProperty.set(timeSpent)
            }

            if (durationRequest != null) {
                val forCompletion = IssueWithTime(issue, recordingTime)
                durationRequest.complete(forCompletion)
                break
            }
        }
    }
}