package com.pras.slugmenu

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@Serializable
data class LocationData(
    val data: List<Location>
)
@Serializable
data class Location(
    val name: String,
    val id: Int,
    val busyness: Int,
    val people: Int,
    val isAvailable: Boolean,
    val capacity: Int,
)

@Serializable
data class CompareData(
    val data: List<CompareList>
)

@Serializable
data class CompareList(
    val comparison: List<Compare>?
)

@Serializable
data class Compare(
    val string: String = "only one location"
)

suspend fun ScrapeWaitzData(): Array<String> {
    val client = HttpClient(CIO)
    val liveResponse: HttpResponse = client.get("https://waitz.io/live/ucsc")
    val liveBody: String = liveResponse.body()

    val compareResponse: HttpResponse = client.get("https://waitz.io/compare/ucsc")
    val compareBody: String = compareResponse.body()

    client.close()
    return arrayOf(liveBody,compareBody)
}

suspend fun GetWaitzData(): Array<MutableList<out MutableList<out Any>>> {
    val jsonResponse = ScrapeWaitzData()
    val liveBody = jsonResponse[0]
    val compareBody = jsonResponse[1]

    val json = Json { ignoreUnknownKeys = true }

    val locationData: LocationData = json.decodeFromString(liveBody)
    val compareData: CompareData = json.decodeFromString(compareBody.replace("<strong>","").replace("</strong>",""))

    val allLocations = mutableListOf<MutableList<Int>>(mutableListOf<Int>(),mutableListOf<Int>(),mutableListOf<Int>(),mutableListOf<Int>(),mutableListOf<Int>())
    var index = 0
    locationData.data.forEach { location ->
        allLocations[index].add(location.busyness)
        allLocations[index].add(location.people)
        allLocations[index].add(location.capacity)
        index += 1
    }

    val allCompares = mutableListOf<MutableList<String>>(mutableListOf<String>(),mutableListOf<String>(),mutableListOf<String>(),mutableListOf<String>(),mutableListOf<String>())
    index = 0
    compareData.data.forEach { comparison ->
        if (comparison.comparison != null) {
            comparison.comparison.forEach { item ->
                allCompares[index].add(item.string)
            }
        }
        index += 1
    }

    return arrayOf(allLocations,allCompares)
}

suspend fun GetWaitzDataAsync(): Array<MutableList<out MutableList<out Any>>> = withContext(Dispatchers.IO) {
    GetWaitzData()
}