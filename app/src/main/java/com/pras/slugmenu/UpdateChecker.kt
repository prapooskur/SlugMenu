package com.pras.slugmenu

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "UpdateChecker"

@Serializable
data class Response(
    val tagName: String
)

suspend fun getLatestVersion(): String {
    val baseUrl = "https://api.github.com/repos/prapooskur/slugmenu/releases"
    val client = HttpClient(CIO)
    val apiResponse = client.get(baseUrl)
    val responseBody = apiResponse.body<String>()
    client.close()

    val json = Json { ignoreUnknownKeys = true }
    val responseList: List<Response> = json.decodeFromString(responseBody)
    val response: Response = responseList[0]
    val latestVersion = response.tagName
    Log.d(TAG, "Latest version: v$latestVersion")
    return latestVersion
}