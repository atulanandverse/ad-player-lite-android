package com.adservrs.adplayer.lite.example.examples

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.adservrs.adplayer.lite.AdPlayer
import com.adservrs.adplayer.lite.AdPlayerContentOverride
import com.adservrs.adplayer.lite.AdPlayerInReadController
import com.adservrs.adplayer.lite.AdPlayerView
import com.adservrs.adplayer.lite.AdPlayerState
import com.adservrs.adplayer.lite.AdPlayerStateListener
import com.adservrs.adplayer.lite.example.PUB_ID
import com.adservrs.adplayer.lite.example.TAG_ID
import kotlinx.coroutines.delay
import org.json.JSONObject

private const val TAG = "SimpleExample"
@Composable
fun SimpleExample(modifier: Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    var controller by remember { mutableStateOf<AdPlayerInReadController?>(null) }
    var shouldCreatePlayer by remember { mutableStateOf(false) }

    // Add delay before creating the player
    LaunchedEffect(Unit) {
        delay(2000) // 2 second delay
        shouldCreatePlayer = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Play/Pause Toggle Button and Hide Overlay Button
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
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
                                controller.setVolumeLevel(1.0f)
                                controller.resume()
                                controller.setVolumeLevel(1.0f)
                                isPlaying = true
                            }
                        }
                    }
                ) {
                    Text(if (isPlaying) "⏸️ Pause" else "▶️ Play")
                }
            }

            // Ad Player View
            Box(modifier = Modifier.weight(1f)) {
                if (shouldCreatePlayer) {
                    AndroidView(
                        factory = { context ->
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
                                    searR?.let{sr ->
                                        it.contentOverride = AdPlayerContentOverride.SearchContent(sr)
                                    }
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
                                            Log.d(TAG, "Event received: $event")
                                        }
                                    }
                                }
                            }
                            controller?.resume()
                            view
                        },
                        onRelease = {
                            it.release()
                        },
                        modifier = Modifier.fillMaxSize(), // Add padding to see green background around edges
                    )
                }
            }
        }
    }
}
