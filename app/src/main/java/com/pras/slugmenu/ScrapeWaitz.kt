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
    val name: String = "null",
    val comparison: List<Compare>?
)

@Serializable
data class Compare(
    val string: String = "only one location"
)

suspend fun scrapeWaitzData(): List<String> {
    val client = HttpClient(CIO)
    val liveResponse: HttpResponse = client.get("https://waitz.io/live/ucsc")
    val liveBody: String = liveResponse.body()

    val compareResponse: HttpResponse = client.get("https://waitz.io/compare/ucsc")
    val compareBody: String = compareResponse.body()

    client.close()
    return listOf(liveBody,compareBody)
}

suspend fun getWaitzData(): List<Map<String, List<String>>> {
    val jsonResponse = scrapeWaitzData()
    val liveBody = jsonResponse[0]
    val compareBody = jsonResponse[1]

    val json = Json { ignoreUnknownKeys = true }

    val locationData: LocationData = json.decodeFromString(liveBody)
    val compareData: CompareData = json.decodeFromString(compareBody.replace("<strong>","").replace("</strong>",""))

    val allLocationDictionary = mutableMapOf<String, List<String>>()
    locationData.data.forEach { location ->
        val locationName = location.name.replace(" / ","/").replace("College 9","Nine").replace("John R Lewis","Lewis").replace(" Dining Hall","").replace("Cafe Main","Cafe")
        val currentLocation = mutableListOf<String>()
        currentLocation.add(location.busyness.toString())
        currentLocation.add(location.people.toString())
        currentLocation.add(location.capacity.toString())
        currentLocation.add(location.isAvailable.toString())
        allLocationDictionary[locationName] = currentLocation.toList()
    }


    val allCompareDictionary = mutableMapOf<String, List<String>>()
    compareData.data.forEach { comparison ->
        if (comparison.comparison != null && comparison.name != "null") {
            val locationName = comparison.name.replace(" / ","/").replace("College 9","Nine").replace("John R Lewis","Lewis").replace(" Dining Hall","").replace("Cafe Main","Cafe")
            val currentCompare = mutableListOf<String>()
            comparison.comparison.forEach { item ->
                currentCompare.add(item.string)
            }
            allCompareDictionary[locationName] = currentCompare.toList()
        }
    }


    return listOf(allLocationDictionary.toMap(),allCompareDictionary.toMap())
}

suspend fun getWaitzDataAsync(): List<Map<String, List<String>>> = withContext(Dispatchers.IO) {
    getWaitzData()
}