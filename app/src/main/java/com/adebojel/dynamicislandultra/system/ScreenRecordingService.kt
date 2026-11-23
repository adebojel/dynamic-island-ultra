package com.adebojel.dynamicislandultra.system

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

class ScreenRecordingService : Service() {
    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var recorder: MediaRecorder? = null
    private var outputDescriptor: ParcelFileDescriptor? = null
    private var stopping = false
    private var started = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopRecording()
            return START_NOT_STICKY
        }
        if (intent?.action != ACTION_START) return START_NOT_STICKY
        if (started) return START_NOT_STICKY

        startAsForeground()
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        val resultData = intent.readResultData()
        val outputUri = intent.getStringExtra(EXTRA_OUTPUT_URI)?.let(Uri::parse)
        if (resultCode == 0 || resultData == null || outputUri == null) {
            fail("Data izin atau lokasi file tidak lengkap.")
            return START_NOT_STICKY
        }
        runCatching { beginCapture(resultCode, resultData, outputUri) }
            .onFailure { fail(it.message ?: "Gagal menyiapkan perekaman.") }
        return START_NOT_STICKY
    }

    private fun beginCapture(resultCode: Int, resultData: Intent, outputUri: Uri) {
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = manager.getMediaProjection(resultCode, resultData)
            ?: error("Android tidak memberikan sesi MediaProjection.")
        outputDescriptor = contentResolver.openFileDescriptor(outputUri, "rwt")
            ?: error("File tujuan tidak dapat dibuka untuk ditulis.")

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)
        val width = (metrics.widthPixels.coerceAtLeast(2) / 2) * 2
        val height = (metrics.heightPixels.coerceAtLeast(2) / 2) * 2
        val density = metrics.densityDpi

        val mediaRecorder = if (Build.VERSION.SDK_INT >= 31) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder = mediaRecorder
        mediaRecorder.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(5_000_000)
            setVideoFrameRate(30)
            setVideoSize(width, height)
            setOutputFile(outputDescriptor!!.fileDescriptor)
            setOnErrorListener { _, _, _ -> stopRecording() }
            prepare()
        }

        projection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopRecording()
            }
        }, Handler(Looper.getMainLooper()))

        virtualDisplay = projection?.createVirtualDisplay(
            "DynamicIslandUltraScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder.surface,
            null,
            null
        ) ?: error("Virtual display gagal dibuat.")
        mediaRecorder.start()
        started = true
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val stop = Intent(this, ScreenRecordingService::class.java).apply { action = ACTION_STOP }
        val pending = PendingIntent.getService(
            this, 9032, stop,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentTitle("Perekaman layar aktif")
            .setContentText("Ketuk Stop untuk mengakhiri dan menyimpan file MP4.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_pause, "Stop", pending)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Screen Recorder", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Status perekaman layar yang dimulai pengguna."
                }
            )
        }
    }

    private fun fail(message: String) {
        Toast.makeText(this, "Screen Recorder: $message", Toast.LENGTH_LONG).show()
        stopRecording()
    }

    private fun stopRecording() {
        if (stopping) return
        stopping = true
        runCatching { virtualDisplay?.release() }
        virtualDisplay = null
        recorder?.let { media ->
            if (started) runCatching { media.stop() }
            runCatching { media.reset() }
            runCatching { media.release() }
        }
        recorder = null
        val currentProjection = projection
        projection = null
        runCatching { currentProjection?.stop() }
        runCatching { outputDescriptor?.close() }
        outputDescriptor = null
        started = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        if (!stopping) stopRecording()
        super.onDestroy()
    }

    private fun Intent.readResultData(): Intent? =
        if (Build.VERSION.SDK_INT >= 33) getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        else {
            @Suppress("DEPRECATION")
            getParcelableExtra(EXTRA_RESULT_DATA)
        }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.adebojel.dynamicislandultra.screenrecord.START"
        const val ACTION_STOP = "com.adebojel.dynamicislandultra.screenrecord.STOP"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val EXTRA_OUTPUT_URI = "output_uri"
        private const val CHANNEL_ID = "screen_recorder"
        private const val NOTIFICATION_ID = 9031
    }
}
