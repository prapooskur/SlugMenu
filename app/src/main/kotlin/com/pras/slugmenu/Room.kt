package com.pras.slugmenu

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "Room"

@Entity(tableName = "menu")
data class Menu(
    @PrimaryKey val location: String,
    val menus: String,
    val cacheDate: String,
)

@Entity(tableName = "waitz")
data class Waitz(
    @PrimaryKey val location: String,
    val cacheTime: String,
    val live: String,
    val compare: String,
)

@Entity(tableName = "hours")
data class Hours(
    @PrimaryKey val location: String,
    val hours: String,
    val cacheDate: String
)

@Entity(tableName = "favorites")
data class Favorite(
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    @PrimaryKey val name: String
)

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu WHERE location = :location")
    fun getMenu(location: String): Menu?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMenu(menu: Menu)

    @Query("DELETE FROM menu")
    fun deleteMenus()
}

class MenuTypeConverters {
    @TypeConverter
    fun fromString(value: String): List<List<String>> {
        Log.d(TAG,"from string value: $value")
        return(Json.decodeFromString(value))
    }

    @TypeConverter
    fun fromList(value: List<List<String>>): String {
        Log.d(TAG,"from list value: $value")
        return(Json.encodeToString(value))
    }
}

@Dao
interface WaitzDao {
    @Query("SELECT * FROM waitz WHERE location = :location")
    fun getData(location: String): Waitz?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWaitz(waitz: Waitz)

    @Query("DELETE FROM waitz")
    fun deleteWaitz()
}

class WaitzTypeConverters {
    @TypeConverter
    fun fromWaitzString(value: String): Map<String,List<String>> {
        Log.d(TAG,"from string value: $value")
        return(Json.decodeFromString(value))
    }

    @TypeConverter
    fun fromWaitzList(value: Map<String,List<String>>): String {
        Log.d(TAG,"from list value: $value")
        return(Json.encodeToString(value))
    }
}

@Dao
interface HoursDao {
    @Query("SELECT * FROM hours WHERE location = :location")
    fun getHours(location: String): Hours?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHours(hours: Hours)

    @Query("DELETE FROM hours")
    fun deleteHours()
}

class HoursTypeConverters {
    @TypeConverter
    fun fromHoursString(value: String): AllHoursList {
        Log.d(TAG,"from string value: $value")
        return(Json.decodeFromString(value))
    }

    @TypeConverter
    fun fromHoursList(value: AllHoursList): String {
        Log.d(TAG,"from list value: $value")
        return(Json.encodeToString(value))
    }
}

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites")
    suspend fun getFavorites(): List<Favorite>

    @Query("SELECT * FROM favorites")
    fun getFavoritesFlow(): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE name = :item COLLATE NOCASE")
    suspend fun selectFavorite(item: String): Favorite?

    @Query("SELECT * FROM favorites WHERE name IN (:items) COLLATE NOCASE")
    suspend fun selectFavorites(items: Set<String>): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
}


@Database(version = 5, entities = [Menu::class, Waitz::class, Hours::class, Favorite::class])
@TypeConverters(MenuTypeConverters::class, WaitzTypeConverters::class, HoursTypeConverters::class)
abstract class MenuDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun waitzDao(): WaitzDao
    abstract fun hoursDao(): HoursDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {

        @Volatile
        private var INSTANCE: MenuDatabase? = null

        fun getInstance(context: Context): MenuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, MenuDatabase::class.java, "menus.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}