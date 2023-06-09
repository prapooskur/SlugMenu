package com.pras.slugmenu

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import java.time.LocalDate
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

private const val TAG = "BackgroundDownloadWorker"

class BackgroundDownloadWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // TODO: Come back to this and finish

        // takes a serialized List<List<Any>> comprised of four-element lists: String, String, Int, Boolean

        val menuDatabase = MenuDatabase.getInstance(applicationContext)
        val menuDao = menuDatabase.menuDao()
        
        val locationUrls: List<List<Any>> = inputData.getString("locationUrls")
            ?.let { Json.decodeFromString(it) } ?: emptyList()

        return try {
            if (!locationUrls.isNullOrEmpty()) {
                // Schema: three-element list of strings
                // String 1: location name
                // String 2: location URL
                // String 3: type of menu (dining menu, non dining menu, oakes)
                // String 4: Whether to download the menu or not

                coroutineScope {
                    // Uses coroutines to download and insert the menus asynchronously (untested)
                    val deferredResults = locationUrls.map { url ->
                        async(Dispatchers.IO) {
                            if (url.size == 3 && url[0] is String && url[1] is String && url[2] is Int && url[3] is Boolean && url[3] == true) {
                                val locationName = url[0] as String
                                val locationUrl = url[1] as String
                                val menuType = url[2] as Int

                                try {
                                    val menuList: Array<MutableList<String>> = when (menuType) {
                                        0 -> getDiningMenuAsync(locationUrl)
                                        1 -> getSingleMenuAsync(locationUrl)
                                        2 -> getOakesMenuAsync(locationUrl)
                                        else -> emptyArray()
                                    }
                                    if (!menuList.isNullOrEmpty()) {
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
                }

                // old synchronous implementation
                /*
                for (url in locationUrls) {
                    if (url.size == 3 && url[0] is String && url[1] is String && url[2] is Int && url[3] is Boolean && url[3] == true) {
                        val locationName = url[0] as String
                        val locationUrl = url[1] as String
                        val menuType = url[2] as Int

                        try {
                            val menuList: Array<MutableList<String>> = when (menuType) {
                                0 -> getDiningMenuAsync(locationUrl)
                                1 -> getSingleMenuAsync(locationUrl)
                                2 -> getOakesMenuAsync(locationUrl)
                                else -> emptyArray()
                            }
                            if (!menuList.isNullOrEmpty()) {
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
                */
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

        val currentDate = LocalDate.now()

        // hardcoded to PST, since that's where UCSC is
        val timeZone = ZoneId.of("America/Los_Angeles")
        var executionDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0, 0))
            .atZone(timeZone)
            .toLocalDateTime()

        if (executionDateTime.toLocalDate().isBefore(currentDate) || executionDateTime.toLocalDate().isEqual(currentDate)) {
            executionDateTime.plusDays(1)
        }





        val duration = Duration.between(executionDateTime, currentDate.atStartOfDay())
        val minutes = duration.toMinutes()

        Log.d("MyWorker", "time difference $minutes")

        //define constraints
        val myConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshCpnWork = PeriodicWorkRequest.Builder(BackgroundDownloadWorker::class.java, 24, TimeUnit.HOURS)
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .setConstraints(myConstraints)
            .addTag("backgroundMenuDownload")
            .build()


        WorkManager.getInstance(context).enqueueUniquePeriodicWork("backgroundMenuDownload",
            ExistingPeriodicWorkPolicy.UPDATE, refreshCpnWork)
    }
}