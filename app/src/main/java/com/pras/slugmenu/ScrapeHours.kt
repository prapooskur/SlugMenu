package com.pras.slugmenu

import android.util.Log
import org.jsoup.Jsoup

private const val TAG = "Hours Scraper"

data class DiningHoursList(val daysList: List<String>, val hoursList: List<List<String>>)

fun getDiningHours(location: String): DiningHoursList {
    val page = Jsoup.connect("https://dining.ucsc.edu/eat/").get()

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
        DiningHoursList(daysList,hoursList)
    } else {
        Log.d(TAG, "site syntax has changed, returning an empty list")
        DiningHoursList(listOf(), listOf())
    }
}

fun getNonDiningHours(location: String): List<String> {
    val page = Jsoup.connect("https://dining.ucsc.edu/eat/").get()
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