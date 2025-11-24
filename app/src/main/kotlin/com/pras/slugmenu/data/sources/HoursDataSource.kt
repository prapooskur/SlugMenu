package com.pras.slugmenu.data.sources

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

private const val TAG = "HoursDataSource"

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

class HoursDataSource {

    suspend fun fetchAllData(): AllHoursList {
        return withContext(Dispatchers.IO) {
            scrapeAllData()
        }
    }

    suspend fun fetchSpecificData(locationId: String): HoursList {
        return withContext(Dispatchers.IO) {
            scrapeSpecificData(locationId)
        }
    }

    private suspend fun scrapeAllData(): AllHoursList {
        val diningMap = listOf(
            "Nine/Lewis",
            "Cowell/Stevenson",
            "Crown/Merrill",
            "Porter/Kresge",
            "Carson/Oakes",
        )

        val nonDiningMap = mapOf(
            "Global Village Cafe" to Uri.encode("Global Village Cafe"),
            "perkem" to Uri.encode("Perk Coffee Bar at Earth & Marine Sciences"),
            "perkbe" to Uri.encode("Perk Coffee Bar at Baskin Engineering"),
            "perkpsb" to Uri.encode("Perk Coffee Bar at Physical Sciences Building"),
            "Terra Fresca" to Uri.encode("University Center Cafe"),
            "Porter Market" to Uri.encode("Porter Market"),
            "Stevenson Coffee House" to Uri.encode("Stevenson Coffee House"),
            "Oakes Cafe" to Uri.encode("Oakes Cafe"),
            "Banana Joe's" to Uri.encode("Banana Joe's Late Night")
        ).keys

        val keys = diningMap + nonDiningMap

        val finalList = keys.map { i ->
            scrapeSpecificData(i)
        }

        return AllHoursList(
            finalList[0],
            finalList[1],
            finalList[2],
            finalList[3],
            finalList[4],
            finalList[5],
            finalList[6],
            finalList[7],
            finalList[8],
            finalList[9],
            finalList[10],
            finalList[11],
            finalList[12],
        )

    }


    private suspend fun scrapeSpecificData(location: String): HoursList {

        val client = HttpClient(CIO) {
            // SSL validation is disabled because UCSC's webserver doesn't properly serve intermediate certs sometimes.
            engine {
                https {
                    trustManager = @SuppressLint("CustomX509TrustManager")
                    object: X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) { }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) { }

                        override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    }
                }
            }
        }
        val pageData = client.get(getLocationUrl(location))
        val pageBody = pageData.body<String>()

        val diningSet = setOf(
            "Nine/Lewis",
            "Cowell/Stevenson",
            "Crown/Merrill",
            "Porter/Kresge",
            "Carson/Oakes",
        )

        val hours = if (diningSet.contains(location)) {
            // dh path
            getDiningHours(pageBody)
        } else {
            // cafe path
            getNonDiningHours(pageBody)
        }

        return hours
    }

}

fun getDiningHours(pageBody: String): HoursList {

//    return HoursList(listOf(), listOf())

    val page = Jsoup.parse(pageBody)

    // todo see if this breaks?
    val dayQuery = "p:has(strong:matches(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday))"
    val days = page.select(dayQuery)
    val hours = page.select("$dayQuery + ul.wp-block-list")

    val daysList = days.map { it.text() }

    val hoursRemovedPattern = Regex("\\(limited entree options\\)\\*")
    val hoursList = hours.map { i ->
        val items = i.select("li")
        items.map { it.text().replace(hoursRemovedPattern, "") }
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

fun getNonDiningHours(pageBody: String): HoursList {
    // todo see if this breaks?
    val page = Jsoup.parse(pageBody)

    val hours = page.select(".mabel-bhi-businesshours-inline > span")
    Log.d(TAG, "retrieved $hours")

    val hoursPerDay = hours.toList().map { i ->
        i.text().replaceFirst(" ", ": ")
    }

    // todo refactor to make this more clear
    return HoursList(hoursPerDay, listOf())
}

fun getLocationUrl(locationId: String): String {
    val diningMap = mapOf(
        "Nine/Lewis" to Uri.encode("nine-jrl"),
        "Cowell/Stevenson" to Uri.encode("cowell-stevenson"),
        "Crown/Merrill" to Uri.encode("crown-merrill"),
        "Porter/Kresge" to Uri.encode("porter-kresge"),
        "Carson/Oakes" to Uri.encode("rcc-oakes"),
    )

    val nonDiningMap = mapOf(
        "Global Village Cafe" to Uri.encode("Global Village Cafe"),
        "perkem" to Uri.encode("Perk Coffee Bar at Earth & Marine Sciences"),
        "perkbe" to Uri.encode("Perk Coffee Bar at Baskin Engineering"),
        "perkpsb" to Uri.encode("Perk Coffee Bar at Physical Sciences Building"),
        "Terra Fresca" to Uri.encode("University Center Cafe"),
        "Porter Market" to Uri.encode("Porter Market"),
        "Stevenson Coffee House" to Uri.encode("Stevenson Coffee House"),
        "Oakes Cafe" to Uri.encode("Oakes Cafe"),
        "Banana Joe's" to Uri.encode("Banana Joe's Late Night")
    )

    return if (locationId in diningMap) {
        "https://dining.ucsc.edu/locations-hours/${diningMap[locationId]}"
    } else {
        "https://dining.wordpress.ucsc.edu/wp-admin/admin-ajax.php?action=mb-bhipro-fetch-shortcode&code=mbhi_hours&options=location%3D%22${nonDiningMap[locationId]}%22%20format%3D%2212%22%20display%3D%22normal%22%20output%3D%22div%22%20includeholidays%3D%22false%22%20includevacations%3D%22false%22%20abbreviatedays%3D%22false%22%20consolidationseparator%3D%22%20-%20%22%20hourseparator%3D%22%20-%20%22%20entryseparator%3D%22%2C%20%22%20mhbr%3D%22true%22%20showonlytoday%3D%22%22%20dayentryseparator%3D%22%20%22%20removezeroes%3D%22true%22%20seo%3D%22true%22%20%20hide_hours%3D%22false%22%20startonsunday%3D%22false%22%20extra_classes%3D%22%22%20dates_in_past%3D%22false%22%20replace_with_specials%3D%22false%22%20replace_with_vacations%3D%22false%22%20date_format%3D%22day%20first%22%20replaced_vacations_format%3D%22%7Bday%7D%20(%7Bname%7D)%22%20replaced_specials_format%3D%22%7Bday%7D%20(%7Bname%7D)%22%20included_vacations_format%3D%22%7Bfrom_day_of_month%7D%20%7Bfrom_month_short%7D%20-%20%7Bto_day_of_month%7D%20%7Bto_month_short%7D%22%20included_specials_format%3D%22%7Bday_of_month%7D%20%7Bmonth_short%7D%22%20day_format%3D%22%7Bday%7D%20%22%20view%3D%22normal%22%20rollover%3D%22false%22%20rollover_from%3D%2230%22%20rollover_to%3D%2214%22"
    }
}
