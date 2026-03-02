package com.adservrs.adplayer.lite.example.examples

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.adservrs.adplayer.lite.AdPlayer
import com.adservrs.adplayer.lite.AdPlayerContentOverride
import com.adservrs.adplayer.lite.AdPlayerInReadController
import com.adservrs.adplayer.lite.AdPlayerView
import com.adservrs.adplayer.lite.AdPlayerState
import com.adservrs.adplayer.lite.AdPlayerStateListener
import kotlinx.coroutines.delay
import org.json.JSONObject
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import com.adservrs.adplayer.lite.example.PUB_ID
import com.adservrs.adplayer.lite.example.TAG_ID

private const val TAG = "SimpleExample"

@Composable
fun SimpleExample(modifier: Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    var controller by remember { mutableStateOf<AdPlayerInReadController?>(null) }
    var shouldCreatePlayer by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "Notification permission result: granted=$isGranted")
    }

    // Add delay before creating the player
    LaunchedEffect(Unit) {
        Log.d(TAG, "LaunchedEffect: Composable recomposed, shouldCreatePlayer=$shouldCreatePlayer")
        delay(2000) // 2 second delay
        shouldCreatePlayer = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(modifier = modifier.fillMaxSize()) {
            // Play/Pause Toggle Button and Show/Hide Bottom Sheet Button
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        controller?.let { controller ->
                            if (isPlaying) {
                                Log.d(TAG, "Pausing player, controller: $controller")
                                controller.pause()
                                isPlaying = false
                            } else {
                                Log.d(TAG, "Resuming player")
                                controller.resume()
                                //controller.setVolumeLevel(1.0f)
                                isPlaying = true
                            }
                        }
                    }
                ) {
                    Text(if (isPlaying) "⏸️ Pause" else "▶️ Play")
                }
                Button(
                    onClick = {
                        Log.d(TAG, "Request Notification Permission button pressed")
                        controller?.pause()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            Log.d(TAG, "Notification permission not required below API 33")
                        }
                    }
                ) {
                    Text("Request Notification Permission")
                }
            }

            // Ad Player View
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (shouldCreatePlayer) {
                    AndroidView(
                        factory = { context ->
                            // ...existing factory code...
                            val view = AdPlayerView(context)
                            AdPlayer.getTag(
                                context,
                                PUB_ID,
                                TAG_ID
                            ).apply {
                                val str = ""
                                    val jsonObject = JSONObject(str)
                                    val searR = jsonObject.optJSONArray("data")
                                        ?: jsonObject.optJSONArray("playlist")
                                    controller = newInReadController {
//                                        it.contentOverride = null
                                        it.contentOverride = AdPlayerContentOverride.SearchContent(searR)
                                    }
                                    view.attachController(controller)
                                    controller?.let { ctr ->
                                        with(ctr) {
                                            addStateListener(object : AdPlayerStateListener {
                                                override fun onAdPlayerStateChanged(newState: AdPlayerState) {
                                                    Log.d(TAG, "State changed: $newState")
                                                    isPlaying =
                                                        newState == AdPlayerState.Playing.Content || newState == AdPlayerState.Playing.AdVideo
                                                }
                                            })

                                            // Add events listener
                                            addEventsListener { event ->
                                                controller?.setVolumeLevel(1.0f)
                                                Log.d(TAG, "Event received: $event")
                                            }
                                        }
                                    }
                                }
                                view
                            },
                            onRelease = {
                                it.release()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f),
                        )
                }
            }
        }
    }
}
