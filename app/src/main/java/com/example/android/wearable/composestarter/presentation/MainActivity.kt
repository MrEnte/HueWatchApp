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

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
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
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.example.android.wearable.composestarter.presentation.communication.changeBrightnessLevel
import com.example.android.wearable.composestarter.presentation.communication.getRoomStatus
import com.example.android.wearable.composestarter.presentation.communication.getRooms
import com.example.android.wearable.composestarter.presentation.communication.toggleLight
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
const val BRIDGE_API = "192.168.0.144"
const val HUE_APPLICATION_KEY = "JDbjnj9gCWzwF83ECLt5hHltW-2pj8C1In6tUZCC"

const val BASE_URL = "https://$BRIDGE_API/clip/v2"

class MainActivity : ComponentActivity() {
    lateinit var connectivityManager: ConnectivityManager
    lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO change this!
        NukeSSLCerts.nuke()

        if (Settings.System.canWrite(this)) {

            connectivityManager =
                this.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                }
            }

            connectivityManager.requestNetwork(
                NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build(),
                networkCallback
            )

            setContent {
                Navigation()
            }
        } else {
            val intent = Intent()
            intent.setAction(ACTION_MANAGE_WRITE_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.bindProcessToNetwork(null)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

@Composable
fun WearApp(navController: NavController) {
    val ctx = LocalContext.current
    var rooms by remember { mutableStateOf(RoomResponse()) }

    getRooms(ctx = ctx, listener = { response -> rooms = response })

    WearAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (rooms.data[0].getName() == "") {
                Text(text = "Loading")
            } else {
                RoomNavigationButtons(rooms = rooms, navController = navController)
            }
        }
    }
}

@Composable
fun RoomNavigationButtons(rooms: RoomResponse, navController: NavController) {
    for (room in rooms.data) {
        Button(
            modifier = Modifier.padding(Dp(16f)),
            onClick = { navController.navigate(Screen.DetailScreen.withArgs(room.services[0].rid)) }) {
            Text(text = room.getName())
        }
    }
}

@Composable
fun RoomSettings(roomId: String) {
    WearAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            println(roomId)
            LightToggle(roomId = roomId)
            BrightnessSlider(roomId = roomId)
        }
    }
}

@Composable
fun BrightnessSlider(roomId: String) {
    val ctx = LocalContext.current
    var brightnessLevel by remember { mutableStateOf(50f) }

    InlineSlider(
        value = brightnessLevel,
        onValueChange = { value ->
            run {
                brightnessLevel = value
                changeBrightnessLevel(ctx, roomId, brightnessLevel)
            }
        },
        increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
        decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
        valueRange = 0f..100f,
        steps = 9,
        segmented = false,
        modifier = Modifier.padding(Dp(16f))
    )
}

@Composable
fun LightToggle(roomId: String) {
    val ctx = LocalContext.current

    var lightIsOn by remember { mutableStateOf(false) }
    getRoomStatus(ctx, roomId, listener = { response -> lightIsOn = response.data[0].on.on })

    var label = "Off"
    if (lightIsOn) {
        label = "On"
    }

    ToggleChip(
        label = { Text(text = label) },
        checked = lightIsOn,
        onCheckedChange = { checked ->
            run {
                lightIsOn = checked
                toggleLight(ctx, checked, roomId)
            }
        },
        modifier = Modifier.padding(Dp(16f)),
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked = lightIsOn),
                contentDescription = if (lightIsOn) "On" else "Off",
            )
        },
    )
}

class MetaData {
    var name: String = "";
}

class Services {
    var rid: String = "";
}

class Room {
    var id: String = "";
    var metadata: MetaData = MetaData();
    var services: List<Services> = List(1) { Services() };

    fun getName(): String {
        return metadata.name
    }
}

class RoomResponse {
    var data: List<Room> = List(1) { Room() }
}

class OnObject {
    var on: Boolean = false
}

class RoomStatus {
    var on: OnObject = OnObject()
}

class RoomStatusResponse {
    var data: List<RoomStatus> = List(1) { RoomStatus() }
}

