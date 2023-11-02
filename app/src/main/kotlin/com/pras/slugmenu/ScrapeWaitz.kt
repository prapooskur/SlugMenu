package com.pras.slugmenu

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "WaitzScraper"

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
    val subLocs: List<Sublocation> = listOf()
)

@Serializable
data class Sublocation(
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

    //Log.d(TAG,liveBody)
    //Log.d(TAG,compareBody)

    val json = Json { ignoreUnknownKeys = true }

    val locationData: LocationData = json.decodeFromString(liveBody.replace("\"subLocs\":false","\"subLocs\":[]"))
    val compareData: CompareData = json.decodeFromString(compareBody.replace("<strong>","").replace("</strong>",""))

    val allLocationDictionary = mutableMapOf<String, List<String>>()
    val sublocationList = setOf("Cowell / Stevenson College","Crown / Merrill College")
    locationData.data.forEach { location ->

        // If the dining hall is stored in a sublocation, use the values from that instead.
        val useSublocation =
            (location.subLocs.isNotEmpty()
            && (sublocationList.contains(location.name)
            || (location.name.endsWith("College") && location.subLocs[0].name.endsWith("Dining Hall"))))

        val locationName = location.name
            .replace(" / ", "/")
            .replace("College 9", "Nine")
            .replace("John R Lewis", "Lewis")
            .replace(" Dining Hall", "")
            .replace("Cafe Main", "Cafe")

        val currentLocation = if (useSublocation) {
            listOf(
                location.subLocs[0].busyness.toString(),
                location.subLocs[0].people.toString(),
                location.subLocs[0].capacity.toString(),
                location.subLocs[0].isAvailable.toString()
            )
        } else {
            listOf(
                location.busyness.toString(),
                location.people.toString(),
                location.capacity.toString(),
                location.isAvailable.toString()
            )
        }
        allLocationDictionary[locationName] = currentLocation
    }

    val allCompareDictionary = mutableMapOf<String, List<String>>()
    compareData.data.forEach { comparison ->
        if (comparison.comparison != null && comparison.name != "null") {
            val locationName = comparison.name
                .replace(" / ","/")
                .replace("College 9","Nine")
                .replace("John R Lewis","Lewis")
                .replace(" Dining Hall","")
                .replace("Cafe Main","Cafe")

            val currentCompare = comparison.comparison.map { it.string }
            allCompareDictionary[locationName] = currentCompare
        }
    }

    Log.d(TAG, listOf(allLocationDictionary.toMap(),allCompareDictionary.toMap()).toString())
    return listOf(allLocationDictionary.toMap(),allCompareDictionary.toMap())
}

suspend fun getWaitzDataAsync(): List<Map<String, List<String>>> = withContext(Dispatchers.IO) {
    getWaitzData()
}