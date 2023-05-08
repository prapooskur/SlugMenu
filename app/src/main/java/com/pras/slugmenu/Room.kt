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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "menu")
data class Menu(
    @PrimaryKey val location: String,
    val menus: String,
    val cacheDate: String,
)

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu WHERE location = :location")
    fun getMenu(location: String): Menu?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMenu(menu: Menu)
}

class MenuTypeConverters {
    @TypeConverter
    fun fromString(value: String): Array<MutableList<String>> {
        Log.d("TAG","from string value: $value")
        val listType = object : TypeToken<Array<MutableList<String>>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(value: Array<MutableList<String>>): String {
        Log.d("TAG","from list value: $value")
        return Gson().toJson(value)
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
                instance = Room.databaseBuilder(context.applicationContext, MenuDatabase::class.java, "mydatabase.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance as MenuDatabase
        }
    }
}