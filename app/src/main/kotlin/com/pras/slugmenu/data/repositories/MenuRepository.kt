package com.pras.slugmenu.data.repositories

import android.util.Log
import com.pras.slugmenu.Favorite
import com.pras.slugmenu.Hours
import com.pras.slugmenu.Menu
import com.pras.slugmenu.Waitz
import com.pras.slugmenu.data.sources.AllHoursList
import com.pras.slugmenu.data.sources.HoursDataSource
import com.pras.slugmenu.data.sources.MenuDataSource
import com.pras.slugmenu.data.sources.MenuSection
import com.pras.slugmenu.data.sources.RoomDataSource
import com.pras.slugmenu.data.sources.WaitzDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

class MenuRepository(
    private val roomDataSource: RoomDataSource,
    private val menuDataSource: MenuDataSource,
    private val waitzDataSource: WaitzDataSource,
    private val hoursDataSource: HoursDataSource,
) {

    private val TAG = "MenuRepository"

    suspend fun fetchMenu(locationName: String, locationUrl: String, checkCache: Boolean = true): List<List<MenuSection>> {
        return withContext(Dispatchers.IO) {
            // if a custom date, ignore cache and just get data
            if (!checkCache) {
                return@withContext menuDataSource.fetchMenu(locationUrl)
            }

            // if getting current date, check cache
            val currentDate = LocalDate.now().toString()
            val menu = roomDataSource.fetchMenu(locationName)
            if (menu != null && menu.cacheDate == currentDate) {
                menu.menus
            } else {
                val menuList = menuDataSource.fetchMenu(locationUrl)
                roomDataSource.insertMenu(
                    Menu(
                        locationName,
                        menuList,
                        currentDate
                    )
                )
                menuList
            }
        }
    }

    suspend fun fetchBusyness(currentTime: LocalDateTime = LocalDateTime.now()): Pair<Map<String, List<String>>, Map<String, List<String>>> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedTime = currentTime.format(dateFormatter)

        val cachedData = roomDataSource.fetchWaitz()
        if (cachedData != null && cachedData.cacheTime == formattedTime) {
            return Pair(cachedData.live, cachedData.compare)
        } else {
            val waitzData = waitzDataSource.fetchData()
            roomDataSource.insertWaitz(
                Waitz(
                    location = "dh-oakes",
                    cacheTime = currentTime.toString(),
                    live = waitzData[0],
                    compare = waitzData[1]
                )
            )
            return Pair(waitzData[0], waitzData[1])
        }
    }

    suspend fun fetchHours(currentDate: LocalDate): AllHoursList {
        val cachedHours = roomDataSource.fetchHours()
        if (cachedHours != null && Period.between(LocalDate.parse(cachedHours.cacheDate), currentDate).days < 7) {
            Log.d(TAG, "returning cached hours")
            return cachedHours.hours
        } else {
            val allHoursList = hoursDataSource.fetchData()
            roomDataSource.insertHours(
                Hours(
                    "dh-nondh-oakes",
                    allHoursList,
                    currentDate.toString()
                )
            )
            Log.d(TAG, "returning fetched hours: $allHoursList")
            return allHoursList
        }
    }

    suspend fun insertFavorite(item: Favorite) {
        roomDataSource.insertFavorite(item)
    }

    suspend fun clearLocalData() {
        roomDataSource.deleteMenus()
        roomDataSource.deleteWaitz()
        roomDataSource.deleteHours()
    }

}