package com.pras.slugmenu.data.sources

import android.annotation.SuppressLint
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.security.cert.X509Certificate
import java.util.Collections
import javax.net.ssl.X509TrustManager

@Serializable
data class MenuSection(val title: String, val items: List<MenuItem>)
@Serializable
data class MenuItem(val name: String, val price: String)

class MenuDataSource {

    val TAG = "MenuDataSource"

    suspend fun fetchMenu(inputUrl: String): List<List<MenuSection>> {
        return withContext(Dispatchers.IO) {
            scrapeMenu(inputUrl)
        }
    }

    private val BASE_URL = "https://nutrition.sa.ucsc.edu/shortmenu.aspx?sName=UC+Santa+Cruz+Dining&locationNum="

    private suspend fun scrapeMenu(inputUrl: String): List<List<MenuSection>> {

        val url = BASE_URL + inputUrl
        val locationCookie = inputUrl.substring(0,2)

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
        val responseBody: String = httpResponse.body()

        val doc = Jsoup.parse(responseBody)
        val table = doc.select("table[width=100%][cellspacing=1][cellpadding=0][border=0]")
        val allListItems = mutableListOf<List<MenuSection>>()

        for (i in 0 until table.size) {
            val listItems = mutableListOf<MenuSection>()

            Log.d(TAG, "loop iteration: $i")
            val rows: Elements = table[i].select("tr")
            val isDining = rows.select("div.shortmenuprices").size == 0

            // don't bother searching for prices if it's dining
            val query = if (isDining) {
                "div.shortmenucats, div.shortmenurecipes"
            } else {
                "div.shortmenucats, div.shortmenurecipes, div.shortmenuprices"
            }

            val menuItems = rows.select(query)
            Log.d(TAG, "menu items: $menuItems")

            // initialize vars for use in loop
            var currentCategory = ""
            val currentList = mutableListOf<MenuItem>()

            val defaultItemValue = "this should never be seen"
            var currentItem = MenuItem(defaultItemValue, defaultItemValue)

            for ((counter, menuSelection) in menuItems.withIndex()) {

                Log.d(TAG, "current selection: $menuSelection")

                if (menuSelection.hasClass("shortmenucats")) {

                    // add current section of items to listItems, then clear for next section
                    if (currentCategory.isNotEmpty()) {
                        Log.d(TAG, "dumping")
                        Log.d(TAG, "current category: $currentCategory")
                        Log.d(TAG, "current list: $currentList")
                        listItems.add(MenuSection(currentCategory, currentList.toList()))

                        currentCategory = ""
                        currentList.clear()
                    }

                    val menuCategory = menuSelection.text()
                    // clean up category

                    currentCategory = if (!menuCategory.contains("&nbsp;")) {
                        menuCategory
                            .replace("&amp;", "&")
                            // banana joe's (crown late night, atm)
                            .replace("Banana Joes", "Banana Joe's")
                            // perks
                            .replace("New City of Santa Cruz Cup Fee of \$.025 BYO and save up to \$0.50 when ordering a togo drink --", "")
                            //oakes
                            .replace("Now City of Santa Cruz Cup Fee of \$.025 BYO and save up to \$0.50 when ordering a togo drink --", " —")
                            .replace("--","—")

                    } else {
                        menuCategory
                    }

                } else if (menuSelection.hasClass("shortmenurecipes")) {
                    val currentSelection = menuSelection.text()

                    // correct for typos in the menu items
                    val cleanText = currentSelection
                        .replace("&amp;", "&")
                        .replace("Iced Match ", "Iced Matcha ")
                        .replace("Mint Condition Condition","Mint Condition Cookie")
                        .replace("Whiped Cream", "Whipped Cream")

                    Log.d(TAG, "current menu selection: $cleanText")

                    currentItem = currentItem.copy(name = cleanText, price = "")

                    if (
                    // if the item is followed by a price, add in the next loop iter
                        (isDining || !menuItems[counter+1].hasClass("shortmenuprices")) &&
                        currentItem.name != defaultItemValue &&
                        currentItem.price.isBlank() &&
                        !currentList.contains(currentItem)
                    ) {
                        Log.d(TAG, "clean item: $currentItem")
                        currentList.add(currentItem)
                        // reset to default values
                        currentItem = MenuItem(defaultItemValue, defaultItemValue)
                    }

                } else if (!isDining && menuSelection.hasClass("shortmenuprices")) {

                    // no post-processing needed, prices are clean by default
                    // this naively assumes that the price for each item directly follows its name
                    // todo see if this causes issues?
                    currentItem = currentItem.copy(price = menuSelection.text())
                    Log.d(TAG, "current price: ${currentItem.price}")

                    if (
                        currentItem.name != defaultItemValue &&
                        currentItem.price != defaultItemValue &&
                        !currentList.contains(currentItem)
                    ) {
                        Log.d(TAG, "clean item: $currentItem")
                        currentList.add(currentItem)

                        // reset to default value after adding
                        currentItem = MenuItem(defaultItemValue, defaultItemValue)
                    }

                }
            }

            // add one last time after loop
            if (currentCategory.isNotEmpty()) {
                Log.d(TAG, "dumping")
                Log.d(TAG, "current category: $currentCategory")
                Log.d(TAG, "current list: $currentList")
                listItems.add(MenuSection(currentCategory, currentList.toList()))

                currentCategory = ""
                currentList.clear()
            }

            //swap double and single to stop them being printed out of order
            // without the equality check, caramel latte was being swapped with cappuccino
            for (currentSection in listItems) {
                for ((itemIndex, item) in currentSection.items.withIndex()) {
                    if (
                        itemIndex < currentSection.items.size-2 &&
                        item.name.contains("Double") &&
                        currentSection.items[itemIndex+1].name.contains("Single") &&
                        item.name.substringBefore(",") == currentSection.items[itemIndex+1].name.substringBefore(",")
                    ) {
                        Log.d(TAG, "swapping ${currentSection.items[itemIndex]} and ${currentSection.items[itemIndex+1]}")
                        Collections.swap(currentSection.items,itemIndex,itemIndex+1)
                        Log.d(TAG, "swapped ${currentSection.items[itemIndex]} and ${currentSection.items[itemIndex+1]}")
                    }
                }
            }

            allListItems.add(listItems)
        }
        Log.d(TAG, "All items: $allListItems")
        return allListItems
    }

}