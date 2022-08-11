package com.example.android.wearable.composestarter.presentation.communication

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.android.wearable.composestarter.presentation.BASE_URL
import com.example.android.wearable.composestarter.presentation.HUE_APPLICATION_KEY
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

fun toggleLight(ctx: Context, toggleState: Boolean, id: String) {
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