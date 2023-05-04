package com.pras.slugmenu

import android.net.http.HttpResponseCache.install
import android.util.Log
import com.pras.slugmenu.ui.theme.md_theme_light_surfaceTint
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
- scrapeDiningHall: 9/10, C/S, Cr/M, P/K
- scrapeCoffeeBar: Perks, Terra Fresca, Stevenson
- scrapeCafe: McHenry, Oakes
- scrapeMarket: Porter
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
    val stringBody: String = httpResponse.body()
    return(stringBody)
    client.close()
}

suspend fun getWebData (inputUrl: String): MutableList<MutableList<String>> {
    /*
    val baseurl: String = "https://nutrition.sa.ucsc.edu/shortmenu.aspx?sName=UC+Santa+Cruz+Dining&locationNum="
    val url: String = baseurl+inputUrl
    val locationCookie: String = inputUrl.substring(0,2)

    val cookies = HashMap<String, String>()
    cookies["WebInaCartDates"] = ""
    cookies["WebInaCartLocation"] = locationCookie
    cookies["WebInaCartMeals"] = ""
    cookies["WebInaCartQtys"] = ""
    cookies["WebInaCartRecipes"] = ""
     */


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
                var separators: String = j.select("span[style=\"color: #000000\"]").toString()
                var items: String = j.select("span[style=\"color: #585858\"]").toString()
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
                        Log.d("TAG", "clean item: "+cleanItem)
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

suspend fun getCoffeeMenu(inputUrl: String): Array<MutableList<String>> {
    return withContext(Dispatchers.IO) {
        val menu = getWebData(inputUrl)
//        Log.d("TAG", "array: $menus")
        arrayOf(menu[0])
    }
}
suspend fun getCoffeeMenuAsync(locationId: String): Array<MutableList<String>> = withContext(Dispatchers.IO) {
    getCoffeeMenu(locationId)
}

suspend fun getOakesMenu(inputUrl: String): Array<MutableList<String>> {
    return withContext(Dispatchers.IO) {
        val menus = getWebData(inputUrl)
//        Log.d("TAG", "array: $menus")
        arrayOf(menus[0],menus[1])
    }
}
suspend fun getOakesMenuAsync(locationId: String): Array<MutableList<String>> = withContext(Dispatchers.IO) {
    getOakesMenu(locationId)
}