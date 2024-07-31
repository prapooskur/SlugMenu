package com.pras.slugmenu

import android.app.Application
import com.pras.slugmenu.data.repositories.MenuRepository
import com.pras.slugmenu.data.repositories.PreferencesRepository
import com.pras.slugmenu.data.sources.HoursDataSource
import com.pras.slugmenu.data.sources.MenuDataSource
import com.pras.slugmenu.data.sources.RoomDataSource
import com.pras.slugmenu.data.sources.WaitzDataSource

// todo find a better name for this
class MyApplication : Application() {

    val menuRepository: MenuRepository by lazy {
        val roomDatabase = MenuDatabase.getInstance(this)
        val roomDataSource = RoomDataSource(
            roomDatabase.menuDao(),
            roomDatabase.waitzDao(),
            roomDatabase.hoursDao(),
            roomDatabase.favoritesDao()
        )
        val menuDataSource = MenuDataSource()
        val waitzDataSource = WaitzDataSource()
        val hoursDataSource = HoursDataSource()

        MenuRepository(roomDataSource, menuDataSource, waitzDataSource, hoursDataSource)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(dataStore)
    }

}