package com.pras.slugmenu

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class WaitzResponse (
    val name: String,
    val busyness: Int,
    val people: Int,
    val capacity: Int,
    val isOpen: Boolean
)

suspend fun ScrapeWaitzData(): String {
    val client = HttpClient(CIO)
    val httpResponse: HttpResponse = client.get("https://waitz.io/live/ucsc")
    val stringBody: String = httpResponse.body()
    client.close()
    return stringBody
}

suspend fun GetWaitzData(): Array<WaitzResponse> {
    val jsonResponse = ScrapeWaitzData()
    val parser = Json { ignoreUnknownKeys = true }
    val locations = parser.decodeFromString<Array<WaitzResponse>>(jsonResponse)
}

suspend fun GetWaitzDataAsync: Array<WaitzResponse> = withContext(Dispatchers.IO) {
    GetWaitzData()
}