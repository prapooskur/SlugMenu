package com.pras.slugmenu

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import java.time.LocalDate
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

private const val TAG = "BackgroundDownloadWorker"

@Serializable
data class LocationListItem(val name: String, val url: String, val type: LocationType, var enabled: Boolean)

enum class LocationType {
    Dining,
    NonDining,
    Oakes
}

class BackgroundDownloadWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // TODO: Come back to this and finish

        // takes a serialized List<LocationList> comprised of four-element LocationListItems

        val menuDatabase = MenuDatabase.getInstance(applicationContext)
        val menuDao = menuDatabase.menuDao()
        
        val locationList: List<LocationListItem> = inputData.getString("locationList")
            ?.let { Json.decodeFromString(it) } ?: emptyList()

        return try {
            if (locationList.isNotEmpty()) {
                // Schema: four-element class of string/string/enum/bool
                // String 1: location name
                // String 2: location URL
                // Enum: type of menu (dining menu, non dining menu, oakes)
                // Boolean: Whether to download the menu or not

                coroutineScope {
                    // Uses coroutines to download and insert the menus asynchronously
                    val deferredResults = locationList.map { location ->
                        async(Dispatchers.IO) {
                            if (location.enabled) {
                                val locationName = location.name
                                val locationUrl = location.url
                                val menuType = location.type

                                try {
                                    val menuList: Array<MutableList<String>> = when (menuType) {
                                        LocationType.Dining     -> getDiningMenuAsync(locationUrl)
                                        LocationType.NonDining  -> getSingleMenuAsync(locationUrl)
                                        LocationType.Oakes      -> getOakesMenuAsync(locationUrl)
                                    }
                                    if (menuList.isNotEmpty()) {
                                        Log.d(TAG, "Downloading menu in background for $locationName")
                                        menuDao.insertMenu(
                                            Menu(
                                                locationName,
                                                MenuTypeConverters().fromList(menuList),
                                                LocalDate.now().toString()
                                            )
                                        )
                                    } else {
                                        Log.d(TAG, "Error downloading menu for $locationName: menu list is null or empty")
                                    }
                                } catch (e: Exception) {
                                    Log.d(TAG, "Error downloading menu for $locationName: $e")
                                }
                            } else {
                                Log.d(TAG, "Error downloading menu: invalid List schema")
                            }
                        }
                    }
                    deferredResults.awaitAll()
                    Log.d(TAG, "All menu downloads completed.")
                }
            }
            Result.success()
        } catch (throwable: Throwable) {
            Log.d(TAG,"Error downloading menu", throwable)
            Result.failure()
        }
    }

}

// Object that automatically schedules background downloads
object BackgroundDownloadScheduler {

    fun refreshPeriodicWork(context: Context) {
        // hardcoded to PST, since that's where UCSC is
        val timeZone = ZoneId.of("America/Los_Angeles")

        val currentDateTime = LocalDateTime.now()


        var executionDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0))
            .atZone(timeZone)
            .toLocalDateTime()

        if (executionDateTime.isBefore(currentDateTime)) {
            Log.d(TAG,"current time is after 2:00 AM, scheduling for tomorrow")
            executionDateTime = executionDateTime.plusDays(1)
        }

        Log.d(TAG, "Scheduled for $executionDateTime")


        val duration = Duration.between(currentDateTime, executionDateTime)
        val minutes = duration.toMinutes()

        Log.d(TAG, "time difference is $minutes minutes")

        //define constraints
        val workerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val locationNames = listOf<String>("Nine/Lewis","Cowell/Stevenson","Crown/Merrill","Porter/Kresge","Perks","Terra Fresca","Porter Market", "Stevenson Coffee House", "Global Village Cafe", "Oakes Cafe")
        val locationUrls =  listOf<String>("40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1","05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1","20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1","25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1","22&locationName=Perk+Coffee+Bars&naFlag=1","45&locationName=UCen+Coffee+Bar&naFlag=1","50&locationName=Porter+Market&naFlag=1","26&locationName=Stevenson+Coffee+House&naFlag=1","46&locationName=Global+Village+Cafe&naFlag=1","23&locationName=Oakes+Cafe&naFlag=1")
        val locationTypes = listOf<LocationType>(LocationType.Dining, LocationType.Dining, LocationType.Dining, LocationType.Dining, LocationType.NonDining, LocationType.NonDining, LocationType.NonDining, LocationType.NonDining, LocationType.NonDining, LocationType.Oakes)
        val locationEnabled = listOf<Boolean>(true,true,true,true,true,true,true,true,true,true)

        val mutableLocationList = mutableListOf<LocationListItem>()
        for (i in locationNames.indices) {
            mutableLocationList.add(LocationListItem(locationNames[i], locationUrls[i], locationTypes[i], locationEnabled[i]))
        }

        val locationList = mutableLocationList.toList()
        Log.d(TAG,"location List: $locationList")
        val serializedLocationList = Json.encodeToString(locationList)
        Log.d(TAG,"serialized Location List: $serializedLocationList")

        val inputLocationList = Data.Builder()
            .putString("locationList", serializedLocationList)
            .build()

        val refreshCpnWork = PeriodicWorkRequest.Builder(BackgroundDownloadWorker::class.java, 24, TimeUnit.HOURS)
            .setInputData(inputLocationList)
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .setConstraints(workerConstraints)
            .addTag("backgroundMenuDownload")
            .build()


        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("backgroundMenuDownload", ExistingPeriodicWorkPolicy.UPDATE, refreshCpnWork)
    }

    fun runSingleDownload(context: Context) {
        // define constraints
        val workerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val locationNames = listOf<String>("Nine/Lewis","Cowell/Stevenson","Crown/Merrill","Porter/Kresge","Perk Coffee Bars","Terra Fresca","Porter Market", "Stevenson Coffee House", "Global Village Cafe", "Oakes Cafe")
        val locationUrls = listOf<String>("40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1","05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1","20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1","25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1","22&locationName=Perk+Coffee+Bars&naFlag=1","45&locationName=UCen+Coffee+Bar&naFlag=1","50&locationName=Porter+Market&naFlag=1","26&locationName=Stevenson+Coffee+House&naFlag=1","46&locationName=Global+Village+Cafe&naFlag=1","23&locationName=Oakes+Cafe&naFlag=1")
        val locationTypes = listOf<LocationType>(LocationType.Dining, LocationType.Dining, LocationType.Dining, LocationType.Dining, LocationType.NonDining, LocationType.NonDining, LocationType.NonDining, LocationType.NonDining, LocationType.NonDining, LocationType.Oakes)
        val locationEnabled = listOf<Boolean>(true,true,true,true,true,true,true,true,true,true)

        val mutableLocationList = mutableListOf<LocationListItem>()
        for (i in locationNames.indices) {
            mutableLocationList.add(LocationListItem(locationNames[i], locationUrls[i], locationTypes[i], locationEnabled[i]))
        }

        val locationList = mutableLocationList.toList()
        Log.d(TAG,"location List: $locationList")
        val serializedLocationList = Json.encodeToString(locationList)
        Log.d(TAG,"serialized Location List: $serializedLocationList")

        val inputLocationList = Data.Builder()
            .putString("locationList", serializedLocationList)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<BackgroundDownloadWorker>()
            .setInputData(inputLocationList)
            .setConstraints(workerConstraints)
            .addTag("backgroundMenuDownload")
            .build()


        WorkManager
            .getInstance(context)
            .enqueue(oneTimeWorkRequest)
    }

    fun cancelDownloadByTag(context: Context, tag: String) {
        WorkManager
            .getInstance(context)
            .cancelAllWorkByTag(tag)
    }
}