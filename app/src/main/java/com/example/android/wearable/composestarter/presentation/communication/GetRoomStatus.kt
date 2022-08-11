package com.example.android.wearable.composestarter.presentation.communication

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.android.wearable.composestarter.presentation.BASE_URL
import com.example.android.wearable.composestarter.presentation.GsonRequest
import com.example.android.wearable.composestarter.presentation.HUE_APPLICATION_KEY
import com.example.android.wearable.composestarter.presentation.RoomStatusResponse

fun getRoomStatus(
    ctx: Context,
    roomId: String,
    listener: Response.Listener<RoomStatusResponse>
) {
    val queue = Volley.newRequestQueue(ctx)
    val url = "$BASE_URL/resource/grouped_light/$roomId"

    val headers: MutableMap<String, String> = HashMap()
    headers["Content-Type"] = "application/json"
    headers["Accept"] = "application/json"
    headers["hue-application-key"] = HUE_APPLICATION_KEY


    val roomRequest = GsonRequest(
        url = url,
        clazz = RoomStatusResponse::class.java,
        headers = headers,
        listener = listener,
        errorListener = { error -> println(error) })

    queue.add(roomRequest)
}