package com.pras.slugmenu

import android.util.Log
import androidx.compose.ui.platform.debugInspectorInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup

private const val TAG = "Hours Scraper"
/*
Dining menus fill in both dayslist and hourslist

Non dining menus fill in only dayslist and leave hourslist empty,
since they don't have hourly submenus on the site
*/

// this is a dumb solution, but it works and it's all i can think of atm
// todo come back to this later, see if it can be done differently

@Serializable
data class HoursList(val daysList: List<String>, val hoursList: List<List<String>> = listOf())

@Serializable
data class AllHoursList(
    val ninelewis: HoursList = HoursList(listOf(), listOf()),
    val cowellstev: HoursList = HoursList(listOf(), listOf()),
    val crownmerrill: HoursList = HoursList(listOf(), listOf()),
    val porterkresge: HoursList = HoursList(listOf(), listOf()),
    val carsonoakes: HoursList = HoursList(listOf(), listOf()),
    val globalvillage: HoursList = HoursList(listOf(), listOf()),
    val perkbe: HoursList = HoursList(listOf(), listOf()),
    val perkpsb: HoursList = HoursList(listOf(), listOf()),
    val perkems: HoursList = HoursList(listOf(), listOf()),
    val terrafresca: HoursList = HoursList(listOf(), listOf()),
    val portermarket: HoursList = HoursList(listOf(), listOf()),
    val stevcoffee: HoursList = HoursList(listOf(), listOf()),
    val oakescafe: HoursList = HoursList(listOf(), listOf()),
)

suspend fun scrapeHoursData(): String {
    val url = "https://dining.ucsc.edu/eat/"
    val client = HttpClient(CIO)
    val pageData = client.get(url)
    return pageData.body<String>()
}

suspend fun getHoursData(): AllHoursList {
    val pageBody = scrapeHoursData()

    val locationList = listOf(
    "ninelewis",
    "csdh",
    "cmdh",
    "porterdh",
    "rodh",
    "bjqm",
    "slugstop",
    "global",
    "merrillmarket",
    "oakes",
    "perkem",
    "perkbe",
    "perkpsb",
    "ucentercafe",
    "portermarket",
    "stevenson",
    "terra"
    )
    val altLocationList = listOf(
        "altnine",
        "altcsdh",
        "altcmdh",
        "altpdh",
        "altglobal",
        "altrodh",
        "altperkbe",
        "altperkpsb",
        "altperkem",
        "altucentercafe",
        "altportermarket",
        "altstevenson",
        "altoakes"
    )

    val tempDiningList = mutableListOf<HoursList>()
    val tempNonDiningList = mutableListOf<List<String>>()
    locationList.forEachIndexed { index, location ->
        if (index < 5) {
            val diningHours = getDiningHours(location,pageBody)
            if (diningHours.daysList.isEmpty() && diningHours.hoursList.isEmpty()) {
                // fall back to alternate list, sometimes this might work
                tempDiningList.add(getDiningHours(altLocationList[index],pageBody))
            } else {
                tempDiningList.add(diningHours)
            }
        } else {
            val nonDiningHours = getNonDiningHours(location, pageBody)
            if (nonDiningHours.isEmpty()) {
                // fall back to alternate list, sometimes this might work
                tempNonDiningList.add(getNonDiningHours(altLocationList[index], pageBody))
            }
            tempNonDiningList.add(nonDiningHours)
        }
    }

    Log.d(TAG, "successfully returning!")
    return AllHoursList(
        tempDiningList[0],
        tempDiningList[1],
        tempDiningList[2],
        tempDiningList[3],
        tempDiningList[4],
        HoursList(tempNonDiningList[0]),
        HoursList(tempNonDiningList[1]),
        HoursList(tempNonDiningList[2]),
        HoursList(tempNonDiningList[3]),
        HoursList(tempNonDiningList[4]),
        HoursList(tempNonDiningList[5]),
        HoursList(tempNonDiningList[6]),
        HoursList(tempNonDiningList[7])
    )
}

fun getDiningHours(location: String, pageBody: String): HoursList {
    val page = Jsoup.parse(pageBody)

    val days = page.select("div#${location} > p:has(strong)")

    val daysRemovedPatterns = Regex("<p><strong>|</strong></p>")
    val daysList = days.map { it.toString().replace(daysRemovedPatterns, "") }

    val hours = page.select("div#${location} > ul")
    val hoursRemovedPattern = Regex("<li>|</li>| \\(limited entree options\\)\\*")
    val hoursList = hours.map { i ->
        val items = i.select("li")
        items.map { it.toString().replace(hoursRemovedPattern, "") }
    }

    return if (daysList.size == hoursList.size) {
        for (i in daysList.indices) {
            Log.d(TAG, daysList[i])
            Log.d(TAG, hoursList[i].toString())
        }
        HoursList(daysList,hoursList)
    } else {
        Log.d(TAG, "site syntax has changed, returning an empty list")
        HoursList(listOf(), listOf())
    }
}

fun getNonDiningHours(location: String, pageBody: String): List<String> {
    val page = Jsoup.parse(pageBody)
    val hours = page.select("div#${location} > table > tbody > tr > td")

    val hoursList = mutableListOf<String>()
    val hoursRemovedPatterns = Regex("<td>|</td>")
    return if (hours.size % 2 == 0) {
        for (i in 0..<hours.size step 2) {
            val day = hours[i].toString().replace(hoursRemovedPatterns, "")
            val openHours = hours[i + 1].toString().replace(hoursRemovedPatterns, "")
            hoursList.add("$day: $openHours")
        }
        Log.d(TAG, hoursList.toString())
        hoursList
    } else {
        Log.d(TAG,"site syntax has changed, returning an empty list")
        listOf()
    }
}