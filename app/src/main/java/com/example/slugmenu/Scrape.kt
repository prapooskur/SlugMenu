package com.example.slugmenu

import android.content.ContentValues.TAG
import android.util.Log
import android.view.MenuItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException


/*
enum class Time {
    BREAKFAST,
    LUNCH,
    DINNER,
    LATENIGHT
}
 */

/*
@Composable
fun WebScraper(inputUrl: String, time: Time) {
    // Scrape data using JSoup
    val scrapedData = getWebData(inputUrl)

    // Display scraped data in a list
    LazyColumn {
        items(scrapedData.size) { item ->
            Text(text = scrapedData[item])
        }
    }
}
 */

fun getWebData (inputUrl: String): MutableList<MutableList<String>> {
    val baseurl: String = "https://nutrition.sa.ucsc.edu/shortmenu.aspx?sName=UC+Santa+Cruz+Dining&locationNum="
    val url: String = baseurl+inputUrl
    val locationCookie: String = inputUrl.substring(0,2)

    val cookies = HashMap<String, String>()
    cookies["WebInaCartDates"] = ""
    cookies["WebInaCartLocation"] = locationCookie
    cookies["WebInaCartMeals"] = ""
    cookies["WebInaCartQtys"] = ""
    cookies["WebInaCartRecipes"] = ""


    val allListItems = mutableListOf<MutableList<String>>()


    val doc: Document = Jsoup.connect(url).cookies(cookies).get()
//    println(doc)
    val table: Elements = doc.select("table[width=100%][cellspacing=1][cellpadding=0][border=0]")

//    val timeChoice = time.ordinal
    for (i in 0 until 4) {
        val listItems = mutableListOf<String>()

        Log.d("TAG","iterator val: $i")
        val rows: Elements = table[i].select("tr")
        val trs: Elements = rows.select("tr")
//    println("start")
        for (j in trs) {
//          println(j)
//          println("start")

            var separators: String = j.select("span[style=\"color: #000000\"]").toString()
            var items: String = j.select("span[style=\"color: #585858\"]").toString()
//          print(items)
            if (separators.length > 29 && !separators.contains("&nbsp;")) {
                var cleanSeparator = separators.substring(29, separators.length - 7)
                listItems.add(cleanSeparator)
//              println(cleanSeparator)
            }
            if (items.length > 42 && items !in listItems) {
                var cleanItem = items.substring(29, items.length - 13)
                if (!listItems.contains(cleanItem)) {
//                  println("add")
//                  println(cleanItem)
                    listItems.add(cleanItem)
                }
            }

        }
        allListItems.add(listItems)
        //END LOOP
    }
    return allListItems

}

suspend fun GetMenus(inputUrl: String): Array<MutableList<String>> {
    return withContext(Dispatchers.IO) {
        val menus = getWebData(inputUrl)
//        Log.d("TAG", "array: $menus")
        arrayOf(menus[0], menus[1], menus[2], menus[3])
    }
}
suspend fun getMenuAsync(locationId: String): Array<MutableList<String>> = withContext(Dispatchers.IO) {
    GetMenus(locationId)
}

