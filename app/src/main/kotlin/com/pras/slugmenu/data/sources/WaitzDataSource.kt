package com.pras.slugmenu.data.sources

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

// todo possibly improve how data is structured?

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


class WaitzDataSource {

    private val TAG = "WaitzDataSource"

    // no location name parameter is needed, since api contains all locations
    suspend fun fetchData(): List<Map<String, List<String>>> {
        return withContext(Dispatchers.IO) {
            scrapeData()
        }
    }

    private suspend fun scrapeData(): List<Map<String, List<String>>> {
        val client = HttpClient(CIO)
        val liveResponse: HttpResponse = client.get("https://waitz.io/live/ucsc")
        val liveBody: String = liveResponse.body()

        val compareResponse: HttpResponse = client.get("https://waitz.io/compare/ucsc")
        val compareBody: String = compareResponse.body()

        client.close()

        val json = Json { ignoreUnknownKeys = true }
        // who at waitz thought this api structure was a good idea?
        val locationData: LocationData = json.decodeFromString(liveBody.replace("\"subLocs\":false","\"subLocs\":[]"))
        val compareData: CompareData = json.decodeFromString(compareBody.replace("<strong>","").replace("</strong>",""))

        val locationDictionary = mutableMapOf<String, List<String>>()
        locationData.data.forEach { location ->
            // If the dining hall is stored in a sublocation, use the values from that instead.
            val useSublocation =
                (location.subLocs.isNotEmpty()
                        && (location.name.endsWith("College")
                        && location.subLocs[0].name.startsWith(location.name.substring(0,location.name.length-7).replace(" / ","/"))
                        && location.subLocs[0].name.endsWith("Dining Hall")))
            // (sublocationList.contains(location.name)

            val locationName = updateLocationName(location.name)

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
            locationDictionary[locationName] = currentLocation
        }

        val compareDictionary = mutableMapOf<String, List<String>>()
        compareData.data.forEach { comparison ->
            if (comparison.comparison != null && comparison.name != "null") {
                val locationName = updateLocationName(comparison.name)

                val currentCompare = comparison.comparison.map { it.string }
                compareDictionary[locationName] = currentCompare
            }
        }

        Log.d(TAG, listOf(locationDictionary,compareDictionary).toString())
        return listOf(locationDictionary,compareDictionary)

    }

    private fun updateLocationName(locationName: String): String {
        return locationName.replace(" / ", "/")
            .replace("College 9", "Nine")
            .replace("John R Lewis", "Lewis")
            .replace("Rachel Carson Oakes", "Carson/Oakes")
            .replace(" Dining Hall", "")
            .replace("McHenry Library - ", "")
            .replace("Cafe Main", "Cafe")
            .trim()
    }

}
