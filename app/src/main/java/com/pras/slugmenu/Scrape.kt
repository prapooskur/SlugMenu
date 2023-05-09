package com.pras.slugmenu

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.system.measureTimeMillis
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

/*
CATEGORIES:
- getDiningMenu: 9/10, C/S, Cr/M, P/K
- getSingleMenu: Perks, Terra Fresca, Stevenson, Porter, Global Village
- getOakesMenu: Oakes
 */

suspend fun scrapeWebData (inputUrl: String): String {
    val baseurl = "https://nutrition.sa.ucsc.edu/shortmenu.aspx?sName=UC+Santa+Cruz+Dining&locationNum="
    val url: String = baseurl+inputUrl
    val locationCookie: String = inputUrl.substring(0,2)

    val client = HttpClient(CIO) {
        install(HttpCookies) {}
    }
    val httpResponse: HttpResponse = client.get(url) {
        cookie(name = "WebInaCartDates", value = "")
        cookie(name = "WebInaCartLocation", value = locationCookie)
        cookie(name = "WebInaCartMeals", value = "")
        cookie(name = "WebInaCartQtys",value = "")
        cookie(name = "WebInaCartRecipes",value = "")
    }
    client.close()

    val stringBody: String = httpResponse.body()
    return(stringBody)
}

suspend fun getWebData (inputUrl: String): MutableList<MutableList<String>> {
    val allListItems = mutableListOf<MutableList<String>>()

    val webScrapeData = scrapeWebData(inputUrl)

    val doc: Document = Jsoup.parse(webScrapeData)
    val parseTime = measureTimeMillis {
        val table: Elements =
            doc.select("table[width=100%][cellspacing=1][cellpadding=0][border=0]")

        for (i in 0 until table.size) {
            val listItems = mutableListOf<String>()

            Log.d("TAG", "iterator val: $i")
            val rows: Elements = table[i].select("tr")
            val trs: Elements = rows.select("tr")

            for (j in trs) {
                val separators: String = j.select("span[style=\"color: #000000\"]").toString()
                val items: String = j.select("span[style=\"color: #585858\"]").toString()
                if (separators.length > 29 && !separators.contains("&nbsp;")) {
                    var cleanSeparator = separators.substring(29, separators.length - 7)
                    cleanSeparator = cleanSeparator.replace("&amp;", "&")
                    if (cleanSeparator.contains("New City of Santa Cruz Cup Fee of \$.025 BYO and save up to \$0.50 when ordering a togo drink --")) {
                        cleanSeparator = cleanSeparator.substring(0, cleanSeparator.length - 95)
                    } else if (cleanSeparator.contains("Now City of Santa Cruz Cup Fee of \$.025 BYO and save up to \$0.50 when ordering a togo drink --")) {
                        cleanSeparator = cleanSeparator.substring(0, cleanSeparator.length - 95)
                        cleanSeparator += " --"
                    }
                    listItems.add(cleanSeparator)
                }

                if (items.length > 42 && items !in listItems) {
                    var cleanItem = items.substring(29, items.length - 13)
                    cleanItem = cleanItem.replace("&amp;", "&")
                    if (!listItems.contains(cleanItem)) {
                        Log.d("TAG", "clean item: $cleanItem")
                        listItems.add(cleanItem)
                    }
                }

            }
            allListItems.add(listItems)
        }
    }
    Log.d("TAG", "Parse time: "+parseTime+"ms.")
    return allListItems

}

suspend fun getDiningMenu(inputUrl: String): Array<MutableList<String>> {
    return withContext(Dispatchers.IO) {
        val menus = getWebData(inputUrl)
//        Log.d("TAG", "array: $menus")
        arrayOf(menus[0], menus[1], menus[2], menus[3])
    }
}
suspend fun getDiningMenuAsync(locationId: String): Array<MutableList<String>> = withContext(Dispatchers.IO) {
    getDiningMenu(locationId)
}

suspend fun getSingleMenu(inputUrl: String): Array<MutableList<String>> {
    return withContext(Dispatchers.IO) {
        val menu = getWebData(inputUrl)
//        Log.d("TAG", "array: $menus")
        Log.d("TAG", "array: "+menu.size)
        if (menu.size > 0) {
            arrayOf(menu[0])
        } else {
            arrayOf()
        }

    }
}
suspend fun getSingleMenuAsync(locationId: String): Array<MutableList<String>> = withContext(Dispatchers.IO) {
    getSingleMenu(locationId)
}

suspend fun getOakesMenu(inputUrl: String): Array<MutableList<String>> {
    return withContext(Dispatchers.IO) {
        val menus = getWebData(inputUrl)
//        Log.d("TAG", "array: $menus")
        if (menus.size > 0) {
            arrayOf(menus[0],menus[1])
        } else {
            arrayOf()
        }
    }
}
suspend fun getOakesMenuAsync(locationId: String): Array<MutableList<String>> = withContext(Dispatchers.IO) {
    getOakesMenu(locationId)
}
