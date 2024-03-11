package com.pras.slugmenu

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private const val TAG = "BackgroundDownloadWorker"

@Serializable
data class LocationListItem(val name: String, val url: String, val type: LocationType)

data class FavoriteItem(val name: String, val locations: MutableMap<String, MutableList<String>>)

enum class LocationType { Dining, NonDining, Oakes }
private const val CHANNEL_ID = "FAVORITES"
//todo update to also download location hours?
class BackgroundDownloadWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {

    private val preferencesDatastore = PreferencesDatastore(context.dataStore)

    override suspend fun doWork(): Result {

        // takes a serialized List<LocationListItem> comprised of three-element LocationListItems

        val menuDatabase = MenuDatabase.getInstance(applicationContext)
        val menuDao = menuDatabase.menuDao()
        val favoritesDao = menuDatabase.favoritesDao()


        val locationList: List<LocationListItem> = inputData.getString("locationList")
            ?.let { Json.decodeFromString(it) } ?: emptyList()

        val isPersistent = inputData.getBoolean("isPersistent", false)

        Log.d(TAG,"location list: $locationList")

        return try {
            if (locationList.isNotEmpty()) {
                // Schema: three-element class of string/string/enum/bool
                // String 1: location name
                // String 2: location URL
                // Enum: type of menu (dining menu, non dining menu, oakes)

                coroutineScope {

                    val notifyPreference = withContext(Dispatchers.IO) {
                        preferencesDatastore.getNotificationPreference.first()
                    }

                    val notifyPermissionGranted =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }

                    val notifyFavorites = isPersistent && notifyPreference && notifyPermissionGranted

                    val favoritesMap = mutableMapOf<String, FavoriteItem>()
                    Log.d(TAG, favoritesDao.getFavorites().toString())
                    val favoritesExist = favoritesDao.getFavorites().isNotEmpty()

                    if (notifyFavorites) {
                        Log.d(TAG, "Notifications enabled")
                        createNotificationChannel()
                    } else {
                        Log.d(TAG, "Notifications disabled")
                    }

                    // Uses coroutines to download and insert the menus asynchronously
                    val deferredResults = locationList.map { location ->
                        async(Dispatchers.IO) {
                            val locationName = location.name
                            val locationUrl = location.url
                            val menuType = location.type

                            try {
                                Log.d(TAG, "Downloading menu in background for $locationName")
                                val menuList: List<List<String>> = when (menuType) {
                                    LocationType.Dining     -> getDiningMenuAsync(locationUrl)
                                    LocationType.NonDining  -> getSingleMenuAsync(locationUrl)
                                    LocationType.Oakes      -> getOakesMenuAsync(locationUrl)
                                }
                                if (menuList.isNotEmpty()) {
                                    // send notifications if user has requested it, making sure to check if permissions were granted
                                    // should this only notify for background downloads, or for both background and user-requested ones?
                                    if (notifyFavorites && favoritesExist) {
                                        // queue up favorited item notifications
                                        for (menu in menuList.indices) {
                                            val favoritesList = favoritesDao.selectFavorites(menuList[menu].toSet())
                                            val time = when (menu) {
                                                0 -> "Breakfast"
                                                1 -> "Lunch"
                                                2 -> "Dinner"
                                                3 -> "Late Night"
                                                else -> "Unknown?"
                                            }
                                            for (favorite in favoritesList) {
                                                // get item in the map associated with favorite at location if it exists, otherwise create a new entry
                                                // then, update it with the proper times
                                                val favoriteKey = favorite.name
                                                favoritesMap.getOrPut(favoriteKey) {
                                                    FavoriteItem(favorite.name, mutableMapOf())
                                                }.locations.getOrPut(locationName) { mutableListOf() }
                                                    .add(time)
                                            }
                                        }
                                    }

                                    menuDao.insertMenu(
                                        Menu(
                                            locationName,
                                            MenuTypeConverters().fromList(menuList),
                                            LocalDate.now().toString()
                                        )
                                    )
                                } else {
                                    Log.d(TAG, "Error downloading menu for $locationName: menu list is empty")
                                }
                            } catch (e: Exception) {
                                Log.d(TAG, "Error downloading menu for $locationName: $e")
                            }
                        }
                    }
                    deferredResults.awaitAll()

                    if (notifyFavorites && favoritesExist) {
                        val groupKey = "com.pras.slugmenu.FAVORITE_NOTIFICATION"
                        val childNotificationList = mutableListOf<NotificationCompat.Builder>()
                        Log.d(TAG, favoritesMap.toString())
                        val favorites = favoritesMap.values
                        Log.d(TAG, favorites.toString())
                        if (favorites.isNotEmpty()) {
                            for (favorite in favorites) {
                                val locValues = favorite.locations.toList()
                                val timesList = mutableListOf<String>()
                                for (value in locValues) {
                                    val times = when (value.second.size) {
                                        1 -> value.second[0]
                                        2 -> value.second.joinToString(" and ")
                                        else -> value.second.subList(0, value.second.size - 1)
                                            .joinToString(", ") + ", and " + value.second.last()
                                    }
                                    timesList.add("at ${value.first.replace("Stevenson","Stev")} for $times")
                                }
                                timesList.sortBy {
                                    when (it.substringAfter("at ").substringBefore(" for ")) {
                                        "Nine/Lewis" -> 1
                                        "Cowell/Stevenson" -> 2
                                        "Cowell/Stev" -> 2
                                        "Crown/Merrill" -> 3
                                        "Porter/Kresge" -> 4
                                        "Carson/Oakes" -> 5
                                        else -> 6
                                    }
                                }

                                val sortKey = favorite.name

                                val bigTextStyle = NotificationCompat.BigTextStyle().bigText(timesList.joinToString("\n"))


                                val contentText = if (timesList.size == 1) { timesList[0] } else { "At ${timesList.size} locations today" }

                                val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.slugicon_notification_foreground)
                                    .setContentTitle(favorite.name)
                                    .setContentText(contentText)
                                    .setStyle(bigTextStyle)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setGroup(groupKey)
                                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                                    .setSortKey(sortKey)
                                    .setAutoCancel(true)

                                childNotificationList.add(builder)
                            }

                            //todo improve appearance on older devices
                            val summaryNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                .setContentTitle("${favorites.size} favorited items available")
                                // Set content text to support devices running API level < 24.
                                // minapi is 24, does this matter?
                                .setContentText(favorites.joinToString { "${it.name}\n" })
                                .setSmallIcon(R.drawable.slugicon_notification_foreground)
                                // Build summary info into InboxStyle template.
                                .setStyle(NotificationCompat.InboxStyle())
                                // Specify which group this notification belongs to.
                                .setGroup(groupKey)
                                // Set this notification as the summary for the group.
                                .setGroupSummary(true)
                                .build()


                            with(NotificationManagerCompat.from(applicationContext)) {
                                for (builder in childNotificationList) {
                                    // notificationId is a unique int for each notification that you must define.
                                    val oneTimeID = System.currentTimeMillis()
                                    notify(oneTimeID.toInt(), builder.build())
                                    Log.d(TAG,"sending child notification")
                                    // small delay to avoid notifications not showing
                                    delay(50)
                                }
                                // The summary notification requires a constant ID.
                                notify(0, summaryNotification)
                            }
                        } else {
                            Log.d(TAG, "favorites empty, skipping notifications")
                        }
                    } else {
                        when {
                            !notifyFavorites -> Log.d(TAG, "Notifications disabled, skipping")
                            !favoritesExist -> Log.d(TAG, "No favorites, skipping notifications")
                        }
                    }

                    Log.d(TAG, "All menu downloads completed.")
                }
            }
            Result.success()
        } catch (throwable: Throwable) {
            Log.d(TAG,"WorkManager failure: error thrown when downloading menu", throwable)
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Favorites"
            val descriptionText = "Notifications for favorite items"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}

// Object that automatically schedules background downloads
object BackgroundDownloadScheduler {

    private val locationNames = listOf(
        "Nine/Lewis",
        "Cowell/Stevenson",
        "Crown/Merrill",
        "Porter/Kresge",
        "Carson/Oakes",
        "Perk Coffee Bars",
        "Terra Fresca",
        "Porter Market",
        "Stevenson Coffee House",
//        "Global Village Cafe",
        "Oakes Cafe"
    )

    private val locationUrls = listOf(
        "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1",
        "05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1",
        "20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1",
        "25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1",
        "30&locationName=Rachel+Carson%2fOakes+Dining+Hall&naFlag=1",
        "22&locationName=Perk+Coffee+Bars&naFlag=1",
        "45&locationName=UCen+Coffee+Bar&naFlag=1",
        "50&locationName=Porter+Market&naFlag=1",
        "26&locationName=Stevenson+Coffee+House&naFlag=1",
//        "46&locationName=Global+Village+Cafe&naFlag=1",
        "23&locationName=Oakes+Cafe&naFlag=1"
    )

    private val locationTypes = listOf(
        LocationType.Dining,
        LocationType.Dining,
        LocationType.Dining,
        LocationType.Dining,
        LocationType.Dining,
        LocationType.NonDining,
        LocationType.NonDining,
        LocationType.NonDining,
        LocationType.NonDining,
//        LocationType.NonDining,
        LocationType.Oakes
    )

    private val locationList = locationNames.indices.map { LocationListItem(locationNames[it], locationUrls[it], locationTypes[it]) }

    fun refreshPeriodicWork(context: Context) {
        // hardcoded to PST, since that's where UCSC is
        val timeZone = ZoneId.of("America/Los_Angeles")
        Log.d(TAG, "Timezone is $timeZone")

        val currentDateTime = LocalDateTime.now(timeZone)

        if (currentDateTime.hour !in 0..2) {
            Log.d(TAG, "Work started outside the flex interval.")
            //throw Exception("Work started outside the flex interval.")
        }

        // set to 2AM because workmanager may not download at the exact time
        /*
        var executionDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0))
            .atZone(timeZone)
            .toLocalDateTime()
         */
        var executionDateTime = ZonedDateTime.of(LocalDate.now(), LocalTime.of(2, 0), timeZone)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

        Log.d(TAG, "Current time is $currentDateTime")
        Log.d(TAG, "Execution time is $executionDateTime")
        if (executionDateTime.isBefore(currentDateTime)) {
            Log.d(TAG,"current time is after 2:00 AM, scheduling for tomorrow")
            Log.d(TAG, "Execution time is $executionDateTime")
            executionDateTime = executionDateTime.plusDays(1)
        }

        //Log.d(TAG, "Scheduled for $executionDateTime")


        val duration = Duration.between(currentDateTime, executionDateTime)
        val minutes = duration.toMinutes()

        Log.d(TAG, "time difference is $minutes minutes")


        //define constraints
        val workerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //update location menus daily
        Log.d(TAG,"location List: $locationList")
        val serializedLocationList = Json.encodeToString(locationList)
        Log.d(TAG,"serialized Location List: $serializedLocationList")

        val backgroundWorkerInput = Data.Builder()
            .putString("locationList", serializedLocationList)
            .putBoolean("isPersistent", true)
            .build()

        // the flexinterval here should, in theory, only allow the task to run between 1 and 2 AM
        // hopefully this stops the task from running before a new day begins
        val refreshCpnWork = PeriodicWorkRequest.Builder(BackgroundDownloadWorker::class.java, 24, TimeUnit.HOURS, 1, TimeUnit.HOURS)
            .setInputData(backgroundWorkerInput)
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

        Log.d(TAG,"location List: $locationList")
        val serializedLocationList = Json.encodeToString(locationList)
        Log.d(TAG,"serialized Location List: $serializedLocationList")

        val inputLocationList = Data.Builder()
            .putString("locationList", serializedLocationList)
            //todo set to false once testing done?
            .putBoolean("isPersistent", true)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<BackgroundDownloadWorker>()
            .setInputData(inputLocationList)
            .setConstraints(workerConstraints)
            .addTag("backgroundMenuDownload")
            .build()

        // with existingworkpolicy.keep, work won't be duplicated if the button is rapidly pressed
        WorkManager
            .getInstance(context)
            .enqueueUniqueWork("oneTimeBackgroundMenuDownload", ExistingWorkPolicy.KEEP, oneTimeWorkRequest)

        Log.d(TAG,"Single download queued")
    }

    fun cancelDownloadByTag(context: Context, tag: String) {
        WorkManager
            .getInstance(context)
            .cancelAllWorkByTag(tag)
    }
}