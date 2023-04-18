package com.example.slugmenu

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

enum class Time {
    BREAKFAST,
    LUNCH,
    DINNER,
    LATENIGHT
}
fun getWebData (inputUrl: String, time: Time): MutableList<String> {
    val baseurl: String = "https://nutrition.sa.ucsc.edu/shortmenu.aspx?sName=UC+Santa+Cruz+Dining&locationNum="
    val url: String = baseurl+inputUrl
    val locationCookie: String = inputUrl.substring(0,2)

    val cookies = HashMap<String, String>()
    cookies["WebInaCartDates"] = ""
    cookies["WebInaCartLocation"] = locationCookie
    cookies["WebInaCartMeals"] = ""
    cookies["WebInaCartQtys"] = ""
    cookies["WebInaCartRecipes"] = ""

    val doc: Document = Jsoup.connect(url).cookies(cookies).get()
//    println(doc)
    val table: Elements = doc.select("table[width=100%][cellspacing=1][cellpadding=0][border=0]")


    val timeChoice = time.ordinal

    val rows: Elements = table[timeChoice].select("tr")
    val trs: Elements = rows.select("tr")
//    println("start")



    var listItems = mutableListOf<String>()
    for (j in trs) {
//        println(j)
//        println("start")
        var separators: String = j.select("span[style=\"color: #000000\"]").toString()
        var items: String = j.select("span[style=\"color: #585858\"]").toString()
//            print(items)
        if (separators.length > 29 && !separators.contains("&nbsp;")) {
            var cleanSeparator = separators.substring(29, separators.length - 7)
            listItems.add(cleanSeparator)
//            println(cleanSeparator)
        }
        if (items.length > 42 && items !in listItems) {
            var cleanItem = items.substring(29, items.length - 13)
            if (!listItems.contains(cleanItem)) {
//                println("add")
//                println(cleanItem)
                listItems.add(cleanItem)
            }
        }
    }
    //END LOOP

//    print(listItems)
    return listItems

}
