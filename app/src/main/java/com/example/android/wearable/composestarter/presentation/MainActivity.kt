/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.composestarter.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material.*
import com.android.volley.toolbox.Volley
import com.example.android.wearable.composestarter.presentation.theme.WearAppTheme

/**
 * Simple "Hello, World" app meant as a starting point for a new project using Compose for Wear OS.
 *
 * Displays only a centered [Text] composable, and the actual text varies based on the shape of the
 * device (round vs. square/rectangular).
 *
 * If you plan to have multiple screens, use the Wear version of Compose Navigation. You can carry
 * over your knowledge from mobile and it supports the swipe-to-dismiss gesture (Wear OS's
 * back action). For more information, go here:
 * https://developer.android.com/reference/kotlin/androidx/wear/compose/navigation/package-summary
 */
val BRIDGE_API = "0.0.0.0"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp()
        }
    }
}

class TimeResponse {
    var datetime: String? = ""
}

@Composable
fun WearApp() {
    val ctx = LocalContext.current

    WearAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var brightnessLevel by remember { mutableStateOf(50f) }
            var lightIsOn by remember { mutableStateOf(false) }

            LightToggle(
                lightIsOn,
                onToggleClick = {
                    kotlin.run {
                        lightIsOn = it
                        getDateTime(ctx, lightIsOn)
                    }
                }
            )

            if (lightIsOn) {
                BrightnessSlider(brightnessLevel, onBrightnessLevelChange = { brightnessLevel = it })
            }

            println(brightnessLevel)
            println(lightIsOn)
        }
    }
}

@Composable
fun BrightnessSlider(brightnessLevel: Float, onBrightnessLevelChange: (Float) -> Unit) {
    InlineSlider(
        value = brightnessLevel,
        onValueChange = onBrightnessLevelChange,
        increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
        decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
        valueRange = 0f..100f,
        steps = 9,
        segmented = false,
        modifier = Modifier.padding(Dp(16f))
    )
}

@Composable
fun LightToggle(isOn: Boolean, onToggleClick: (Boolean) -> Unit) {
    var label = "Turn Light on"
    if (isOn) {
        label = "Turn Light Off"
    }

    ToggleChip(
        label = { Text(text = label) },
        checked = isOn,
        onCheckedChange = onToggleClick,
        modifier = Modifier.padding(Dp(16f)),
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked = isOn),
                contentDescription = if (isOn) "On" else "Off",
            )
        },
    )
}

private fun getDateTime(ctx: Context, toggleState: Boolean) {
    // TODO use hue api
    val queue = Volley.newRequestQueue(ctx)
    val url = "http://worldtimeapi.org/api/timezone/Europe/Berlin"
    val timeRequest = GsonRequest(
        url,
        listener = { response -> println(response.datetime) },
        errorListener = { println("Did not work!") },
        clazz = TimeResponse::class.java,
        headers = null
    )
    queue.add(timeRequest)
}
