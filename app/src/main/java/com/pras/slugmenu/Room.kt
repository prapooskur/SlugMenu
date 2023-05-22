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

@Entity(tableName = "menu")
data class Menu(
    @PrimaryKey val location: String,
    val menus: String,
    val cacheDate: String,
)

@Entity(tableName = "waitz")
data class Waitz(
    @PrimaryKey val location: String,
    val live: String,
    val compare: String,
    val cacheTime: String,
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
    fun fromString(value: String): Array<MutableList<String>> {
        Log.d("TAG","from string value: $value")
        return(Json.decodeFromString<Array<MutableList<String>>>(value))
    }

    @TypeConverter
    fun fromList(value: Array<MutableList<String>>): String {
        Log.d("TAG","from list value: $value")
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
    fun dropWaitz()
}

class WaitzTypeConverters {
    @TypeConverter
    fun fromLiveString(value: String): Array<MutableList<String>> {
        Log.d("TAG","from string value: $value")
        return(Json.decodeFromString<Array<MutableList<String>>>(value))
    }

    @TypeConverter
    fun fromLiveList(value: Array<MutableList<String>>): String {
        Log.d("TAG","from list value: $value")
        return(Json.encodeToString(value))
    }

    @TypeConverter
    fun fromCompareString(value: String): Array<MutableList<String>> {
        Log.d("TAG","from string value: $value")
        return(Json.decodeFromString<Array<MutableList<String>>>(value))
    }

    @TypeConverter
    fun fromCompareList(value: Array<MutableList<String>>): String {
        Log.d("TAG","from list value: $value")
        return(Json.encodeToString(value))
    }
}





@Database(entities = [Menu::class], version = 1)
@TypeConverters(MenuTypeConverters::class)
abstract class MenuDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao

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