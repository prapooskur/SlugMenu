package com.example.slugmenu

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.system.measureTimeMillis


/*
CATEGORIES:
- scrapeDiningHall: 9/10, C/S, Cr/M, P/K
- scrapeCoffeeBar: Perks, Terra Fresca, Stevenson
- scrapeCafe: McHenry, Oakes
- scrapeMarket: Porter
 */

fun scrapeDiningHall (inputUrl: String): MutableList<MutableList<String>> {
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
    val parseTime = measureTimeMillis {
        val table: Elements =
            doc.select("table[width=100%][cellspacing=1][cellpadding=0][border=0]")

//    val timeChoice = time.ordinal

        for (i in 0 until 4) {
            val listItems = mutableListOf<String>()

            Log.d("TAG", "iterator val: $i")
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
                        cleanItem = cleanItem.replace("&amp;", "&")
                        Log.d("TAG", cleanItem)
                        listItems.add(cleanItem)
                    }
                }

            }
            allListItems.add(listItems)
            //END LOOP
        }
    }
    Log.d("TAG", "Parse time: "+parseTime+"ms.")
    return allListItems

}

suspend fun getDiningMenu(inputUrl: String): Array<MutableList<String>> {
    return withContext(Dispatchers.IO) {
        val menus = scrapeDiningHall(inputUrl)
//        Log.d("TAG", "array: $menus")
        arrayOf(menus[0], menus[1], menus[2], menus[3])
    }
}
suspend fun getDiningMenuAsync(locationId: String): Array<MutableList<String>> = withContext(Dispatchers.IO) {
    getDiningMenu(locationId)
}

fun scrapeCoffeeBar (inputUrl: String): MutableList<MutableList<String>> {
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
    val parseTime = measureTimeMillis {
        val table: Elements =
            doc.select("table[width=100%][cellspacing=1][cellpadding=0][border=0]")

//    val timeChoice = time.ordinal

        for (i in 0 until 4) {
            val listItems = mutableListOf<String>()

            Log.d("TAG", "iterator val: $i")
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
                        cleanItem = cleanItem.replace("&amp;", "&")
                        Log.d("TAG", cleanItem)
                        listItems.add(cleanItem)
                    }
                }

            }
            allListItems.add(listItems)
            //END LOOP
        }
    }
    Log.d("TAG", "Parse time for Perks: "+parseTime+"ms.")
    return allListItems

}