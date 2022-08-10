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
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
import android.util.Log
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
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.android.wearable.composestarter.presentation.theme.WearAppTheme
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NukeSSLCerts.nuke()

        if (Settings.System.canWrite(this)) {

            connectToAvailableWiFi(ctx = this)

            setContent {
                WearApp()
            }
        } else {
            val intent = Intent()
            intent.setAction(ACTION_MANAGE_WRITE_SETTINGS)
            startActivity(intent)
        }
    }
}

fun connectToAvailableWiFi(ctx: Context) {
    val connectivityManager =
        ctx.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            // The Wi-Fi network has been acquired, bind it to use this network by default
            connectivityManager.bindProcessToNetwork(network)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            // The Wi-Fi network has been disconnected
        }
    }

    connectivityManager.requestNetwork(
        NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build(),
        callback
    )
}

@Composable
fun WearApp() {
    val ctx = LocalContext.current
    var rooms by remember { mutableStateOf(RoomResponse()) }

    getRooms(ctx = ctx, listener = { response -> rooms = response })

    WearAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (rooms.data[0].getName() == "") {
                Text(text = "Loading")
            } else {
                LightToggleWrapper(ctx = ctx, rooms = rooms)
            }

//            var brightnessLevel by remember { mutableStateOf(50f) }
//            var lightIsOn by remember { mutableStateOf(false) }
//            LightToggle(
//                isOn = lightIsOn,
//                name = "Light",
//                onToggleClick = {
//                    kotlin.run {
//                        lightIsOn = it
//                        toggleLight(ctx, lightIsOn)
//                    }
//                }
//            )
//
//
//            if (lightIsOn) {
//                BrightnessSlider(
//                    brightnessLevel,
//                    onBrightnessLevelChange = {
//                        kotlin.run {
//                            brightnessLevel = it
//                            changeBrightnessLevel(ctx, brightnessLevel)
//                        }
//                    })
//            }
//
//            println(brightnessLevel)
//            println(lightIsOn)
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
fun LightToggleWrapper(rooms: RoomResponse, ctx: Context) {
    for (room in rooms.data) {
        var lightIsOn by remember { mutableStateOf(false) }

        LightToggle(
            isOn = lightIsOn,
            name = room.getName(),
            onToggleClick = {
                kotlin.run {
                    lightIsOn = it
                    toggleLight(ctx, lightIsOn, room.services[0].rid)
                }
            }
        )
    }
}

@Composable
fun LightToggle(isOn: Boolean, name: String, onToggleClick: (Boolean) -> Unit) {
    var label = "Turn $name On "
    if (isOn) {
        label = "Turn $name Off"
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

private fun toggleLight(ctx: Context, toggleState: Boolean, id: String) {
    val url = "$BASE_URL/resource/grouped_light/$id"
    val queue = Volley.newRequestQueue(ctx)

    val lightStateJson = JSONObject()
    try {
        lightStateJson.put("on", toggleState)
    } catch (e: JSONException) {
        println(e)
    }

    val putData = JSONObject()
    try {
        putData.put("on", lightStateJson)
    } catch (e: JSONException) {
        println(e)
    }
    val putRequest: JsonObjectRequest =
        object : JsonObjectRequest(
            Method.PUT, url, putData,
            Response.Listener { response ->
                Log.d("Response", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("Error.Response", error.toString())
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                headers["hue-application-key"] = HUE_APPLICATION_KEY

                return headers
            }

            override fun getBody(): ByteArray? {
                try {
                    Log.i("json", putData.toString())
                    return putData.toString().toByteArray(charset("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                return null
            }
        }
    queue.add(putRequest)
}

private fun changeBrightnessLevel(ctx: Context, brightnessLevel: Float) {
    val url = "$BASE_URL/resource/grouped_light/2b372bb0-cc98-4b0d-bd57-0bd1218de179"
    val queue = Volley.newRequestQueue(ctx)

    val dimmingObject = JSONObject()
    try {
        dimmingObject.put("brightness", brightnessLevel)
    } catch (e: JSONException) {
        println(e)
    }

    val putData = JSONObject()
    try {
        putData.put("dimming", dimmingObject)
    } catch (e: JSONException) {
        println(e)
    }
    val putRequest: JsonObjectRequest =
        object : JsonObjectRequest(
            Method.PUT, url, putData,
            Response.Listener { response ->
                Log.d("Response", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("Error.Response", error.toString())
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                headers["hue-application-key"] = HUE_APPLICATION_KEY

                return headers
            }

            override fun getBody(): ByteArray? {
                try {
                    Log.i("json", putData.toString())
                    return putData.toString().toByteArray(charset("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                return null
            }
        }
    queue.add(putRequest)
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

private fun getRooms(ctx: Context, listener: Response.Listener<RoomResponse>) {
    val queue = Volley.newRequestQueue(ctx)
    val url = "$BASE_URL/resource/room"

    val headers: MutableMap<String, String> = HashMap()
    headers["Content-Type"] = "application/json"
    headers["Accept"] = "application/json"
    headers["hue-application-key"] = HUE_APPLICATION_KEY


    val roomRequest = GsonRequest(
        url = url,
        clazz = RoomResponse::class.java,
        headers = headers,
        listener = listener,
        errorListener = { error -> println(error) })

    queue.add(roomRequest)
}
