package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import dagger.assisted.Assisted
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.pushproviders.api.PushHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.TimeUnit

private val loggerTag = LoggerTag("ScPushHandler", LoggerTag.PushLoggerTag)

class ScPushWorker(
    @Assisted
    private val context: Context,
    @Assisted
    private val params: WorkerParameters,
    private val pushParser: UnifiedPushParser,
    private val pushHandler: PushHandler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val message = params.inputData.getByteArray(PARAM_MESSAGE)
        val instance = params.inputData.getString(PARAM_INSTANCE)
        if (message == null || instance == null) {
            Timber.tag(loggerTag.value).e("Missing input data")
            return Result.failure()
        }
        // Copy of VectorUnifiedPushMessagingReceiver.onMessage()
        val pushData = pushParser.parse(message, instance)
        return if (pushData == null) {
            Timber.tag(loggerTag.value).w("Invalid data received from UnifiedPush")
            pushHandler.handleInvalid(
                providerInfo = "${UnifiedPushConfig.NAME} - $instance",
                data = String(message),
            )
            Result.success()
        } else {
            Timber.tag(loggerTag.value).d("Handling push from worker $id, tags: ${tags.joinToString()}, attempt: ${params.runAttemptCount}")
            if (pushHandler.handle(pushData = pushData, providerInfo = "${UnifiedPushConfig.NAME} - $instance")) {
                Timber.tag(loggerTag.value).d("Success: Handling push from worker $id, tags: ${tags.joinToString()}, attempt: ${params.runAttemptCount}")
                Result.success()
            } else if (runAttemptCount < 2) {
                Timber.tag(loggerTag.value).d("Retry: Handling push from worker $id, tags: ${tags.joinToString()}, attempt: ${params.runAttemptCount}")
                Result.retry()
            } else {
                Timber.tag(loggerTag.value).d("Failure: Handling push from worker $id, tags: ${tags.joinToString()}, attempt: ${params.runAttemptCount}")
                pushHandler.scHandleLookupFailure(
                    providerInfo = "${UnifiedPushConfig.NAME} - $instance",
                    pushData = pushData,
                )
                Result.failure()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, "SC_APP_BG_SERVICE_NOTIFICATION_CHANNEL_ID")
            .setContentTitle(context.getString(chat.schildi.lib.R.string.sc_push_handler_notification_title))
            .setContentText(context.getString(chat.schildi.lib.R.string.sc_push_handler_notification_summary))
            .setSmallIcon(chat.schildi.lib.R.drawable.ic_notification_sc)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(true)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ForegroundInfo(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            )
        } else {
            ForegroundInfo(1, notification)
        }
    }

    companion object {
        fun launch(
            context: Context,
            message: ByteArray,
            instance: String,
            pushHandler: PushHandler,
            pushParser: UnifiedPushParser,
            scPreferencesStore: ScPreferencesStore,
            coroutineScope: CoroutineScope
        ): Boolean {
            return if (runBlocking { scPreferencesStore.settingFlow(ScPrefs.NOTIFICATION_WORKER).first() }) {
                val id = "sc_push_${Pair(message, instance).hashCode()}"
                val ts = System.currentTimeMillis()
                Timber.tag(loggerTag.value).d("Launching push worker $id at $ts")
                val workManager = WorkManager.getInstance(context)
                val request = OneTimeWorkRequestBuilder<ScPushWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(
                        Data.Builder()
                            .putByteArray(PARAM_MESSAGE, message)
                            .putString(PARAM_INSTANCE, instance)
                            .build()
                    )
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .addTag("sc_push")
                    .addTag("schedule_ts=$ts")
                    .build()
                workManager.enqueueUniqueWork(
                    id,
                    ExistingWorkPolicy.KEEP,
                    request,
                )
                coroutineScope.launch {
                    pushHandler.scHandleReceived()
                    val pushData = pushParser.parse(message, instance)
                    pushHandler.scHandleDeferred(instance, pushData)
                }
                true
            } else {
                false
            }
        }
        private const val PARAM_MESSAGE = "message"
        private const val PARAM_INSTANCE = "instance"
    }
}
