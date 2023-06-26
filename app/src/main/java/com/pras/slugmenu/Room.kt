package com.pras.slugmenu

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
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
    @PrimaryKey val cacheTime: String,
    val live: String,
    val compare: String,
)

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu WHERE location = :location")
    fun getMenu(location: String): Menu?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMenu(menu: Menu)

    @Query("DELETE FROM menu")
    fun dropMenus()
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
    @Query("SELECT * FROM waitz WHERE cacheTime = :cacheTime")
    fun getData(cacheTime: String): Waitz?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWaitz(waitz: Waitz)

    @Query("DELETE FROM waitz")
    fun dropWaitz()
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





@Database(entities = [Menu::class, Waitz::class], version = 2)
@TypeConverters(MenuTypeConverters::class, WaitzTypeConverters::class)
abstract class MenuDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun waitzDao(): WaitzDao

    companion object {
        private var instance: MenuDatabase? = null

        fun getInstance(context: Context): MenuDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, MenuDatabase::class.java, "menus.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance as MenuDatabase
        }


    }
}