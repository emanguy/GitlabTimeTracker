package edu.erittenhouse.gitlabtimetracker.controller

import edu.erittenhouse.gitlabtimetracker.controller.event.TimeRecordingState
import edu.erittenhouse.gitlabtimetracker.controller.result.RecordingStopResult
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.model.IssueWithTime
import edu.erittenhouse.gitlabtimetracker.model.TimeSpend
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingController
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Instant
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val mutableRecordingIssueFlow = MutableStateFlow<TimeRecordingState>(TimeRecordingState.NoIssueRecording)
    val recordingIssueState = mutableRecordingIssueFlow.asStateFlow()
    val recordingIssueProperty = SimpleObjectProperty<Issue?>(null)
    val timeSpentProperty = SimpleStringProperty("00:00:00")

    init {
        launch(Dispatchers.JavaFx) {
            recordingIssueState.collect {
                when (it) {
                    is TimeRecordingState.IssueRecording -> recordingIssueProperty.set(it.issue)
                    is TimeRecordingState.NoIssueRecording -> recordingIssueProperty.set(null)
                }
            }
        }
    }

    suspend fun startTiming(issue: Issue): RecordingStopResult {
        tickerJobMutex.withLock {
            val currentTickerJobValue = tickerJob
            val previousTimingResult = if (currentTickerJobValue != null) {
                // Invoke the unsafe version because we've already acquired the lock
                unsafeStopTiming()
            } else {
                RecordingStopResult.NoIssueBeingRecorded
            }

            tickerJob = launch { updateTime(issue) }
            mutableRecordingIssueFlow.value = TimeRecordingState.IssueRecording(issue)
            return previousTimingResult
        }
    }

    suspend fun stopTiming(): RecordingStopResult {
        tickerJobMutex.withLock {
            val timedIssue = unsafeStopTiming()
            mutableRecordingIssueFlow.value = TimeRecordingState.NoIssueRecording
            return timedIssue
        }
    }

    /**
     * Stops the timing job without acquiring the lock first. You MUST acquire the
     * lock before invoking this function!
     */
    private suspend fun unsafeStopTiming(): RecordingStopResult {
        val currentTickerJobValue = tickerJob ?: return RecordingStopResult.NoIssueBeingRecorded
        val recordedTime = CompletableDeferred<IssueWithTime>()
        stopTrigger.send(recordedTime)

        val timingResult = withTimeoutOrNull(2000) {
            recordedTime.await()
        }

        withContext(Dispatchers.JavaFx) {
            timeSpentProperty.set("00:00:00")
        }

        return if (timingResult == null) {
            currentTickerJobValue.cancel()
            tickerJob = null
            RecordingStopResult.RecorderUnresponsive
        } else {
            tickerJob = null
            RecordingStopResult.StoppedTiming(timingResult)
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
                val forCompletion = IssueWithTime(issue, TimeSpend.fromPeriod(recordingTime))
                durationRequest.complete(forCompletion)
                break
            }
        }
    }
}