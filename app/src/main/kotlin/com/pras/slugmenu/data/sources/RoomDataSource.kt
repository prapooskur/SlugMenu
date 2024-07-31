package com.pras.slugmenu.data.sources

import com.pras.slugmenu.Favorite
import com.pras.slugmenu.FavoritesDao
import com.pras.slugmenu.Hours
import com.pras.slugmenu.HoursDao
import com.pras.slugmenu.Menu
import com.pras.slugmenu.MenuDao
import com.pras.slugmenu.Waitz
import com.pras.slugmenu.WaitzDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomDataSource(
    private val menuDao: MenuDao,
    private val waitzDao: WaitzDao,
    private val hoursDao: HoursDao,
    private val favoritesDao: FavoritesDao
) {

    // menu dao
    suspend fun fetchMenu(locationName: String): Menu? {
        return withContext(Dispatchers.IO) { menuDao.getMenu(locationName) }
    }

    suspend fun insertMenu(menu: Menu) {
        withContext(Dispatchers.IO) { menuDao.insertMenu(menu) }
    }

    suspend fun deleteMenus() {
        withContext(Dispatchers.IO) { menuDao.deleteMenus() }
    }

    // waitz dao
    suspend fun fetchWaitz(): Waitz? {
        // hardcoded key
        return withContext(Dispatchers.IO) { waitzDao.getData("dh-oakes") }

    }

    suspend fun insertWaitz(waitz: Waitz) {
        withContext(Dispatchers.IO) { waitzDao.insertWaitz(waitz) }
    }

    suspend fun deleteWaitz() {
        withContext(Dispatchers.IO) { waitzDao.deleteWaitz() }
    }

    // hours dao
    suspend fun fetchHours(): Hours? {
        // hardcoded key
        return withContext(Dispatchers.IO) { hoursDao.getHours("dh-nondh-oakes") }
    }

    suspend fun insertHours(hours: Hours) {
        withContext(Dispatchers.IO) { hoursDao.insertHours(hours) }
    }

    suspend fun deleteHours() {
        withContext(Dispatchers.IO) { hoursDao.deleteHours() }
    }

    // favorites dao
    suspend fun fetchFavorites(): List<Favorite> {
        return withContext(Dispatchers.IO) { favoritesDao.getFavorites() }
    }

    suspend fun fetchFavoritesFlow(): Flow<List<Favorite>> {
        return withContext(Dispatchers.IO) { favoritesDao.getFavoritesFlow() }
    }

    suspend fun selectFavorite(item: String): Favorite? {
        return withContext(Dispatchers.IO) { favoritesDao.selectFavorite(item) }
    }

    suspend fun selectFavorites(items: Set<String>): List<Favorite> {
        return withContext(Dispatchers.IO) { favoritesDao.selectFavorites(items) }
    }

    suspend fun insertFavorite(favorite: Favorite) {
        withContext(Dispatchers.IO) { favoritesDao.insertFavorite(favorite) }
    }

    suspend fun deleteFavorite(favorite: Favorite) {
        withContext(Dispatchers.IO) { favoritesDao.deleteFavorite(favorite) }
    }

    suspend fun deleteAllFavorites() {
        withContext(Dispatchers.IO) { favoritesDao.deleteAllFavorites() }
    }

}