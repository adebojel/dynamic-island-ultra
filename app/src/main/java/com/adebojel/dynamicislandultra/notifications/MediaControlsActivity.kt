package com.adebojel.dynamicislandultra.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MediaControlsActivity : ComponentActivity() {
    private var currentMedia by mutableStateOf(ActiveMediaSession.snapshot())
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            currentMedia = ActiveMediaSession.snapshot()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                val item = currentMedia
                var seek by remember(item?.positionMs) { mutableStateOf((item?.positionMs ?: 0L).toFloat()) }
                val duration = (item?.durationMs ?: 0L).coerceAtLeast(1L).toFloat()
                Column(
                    Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("MEDIA SESSION", color = Color(0xFF43E8FF), fontSize = 18.sp)
                    if (item?.artwork != null) {
                        Image(item.artwork.asImageBitmap(), "Artwork media aktif", modifier = Modifier.size(190.dp))
                    } else Spacer(Modifier.height(30.dp))
                    Text(item?.title?.ifBlank { "Tidak ada media aktif" } ?: "Tidak ada media aktif",
                        color = Color.White, fontSize = 20.sp)
                    Text(
                        listOfNotNull(item?.artist?.takeIf { it.isNotBlank() }, item?.album?.takeIf { it.isNotBlank() })
                            .joinToString(" • "),
                        color = Color.LightGray, fontSize = 12.sp
                    )
                    Text(item?.packageName.orEmpty(), color = Color(0xFF93A3BE), fontSize = 10.sp)
                    Slider(
                        value = seek.coerceIn(0f, duration),
                        onValueChange = { seek = it },
                        onValueChangeFinished = { ActiveMediaSession.seekTo(seek.toLong()) },
                        valueRange = 0f..duration,
                        enabled = (item?.durationMs ?: 0L) > 0L
                    )
                    Text("${timeLabel(item?.positionMs ?: 0L)} / ${timeLabel(item?.durationMs ?: 0L)}",
                        color = Color.LightGray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { ActiveMediaSession.skipPrevious() }, enabled = item != null) { Text("Sebelumnya") }
                        Button(onClick = { ActiveMediaSession.togglePlayback() }, enabled = item != null) {
                            Text(if (item?.playing == true) "Jeda" else "Putar")
                        }
                        Button(onClick = { ActiveMediaSession.skipNext() }, enabled = item != null) { Text("Berikutnya") }
                    }
                    Text("Kontrol memerlukan MediaSession yang didukung dan Notification Access.",
                        color = Color(0xFF93A3BE), fontSize = 11.sp)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ActiveMediaSession.ACTION_CHANGED)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(receiver, filter)
        currentMedia = ActiveMediaSession.snapshot()
    }

    override fun onStop() {
        runCatching { unregisterReceiver(receiver) }
        super.onStop()
    }

    private fun timeLabel(milliseconds: Long): String {
        val seconds = milliseconds.coerceAtLeast(0L) / 1000L
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }
}
