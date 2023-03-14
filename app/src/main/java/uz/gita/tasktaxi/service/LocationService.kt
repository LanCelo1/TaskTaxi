package uz.gita.tasktaxi.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.gita.tasktaxi.service.client.LocationClient
import uz.gita.tasktaxi.MainActivity
import uz.gita.tasktaxi.R
import uz.gita.tasktaxi.data.model.LocationData
import uz.gita.tasktaxi.data.model.ServiceAction
import uz.gita.tasktaxi.data.repository.AppRepository
import uz.gita.tasktaxi.utils.Constant
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    @Inject
    lateinit var repository: AppRepository

    @Inject
    lateinit var locationClient: LocationClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onBind(p0: Intent?): IBinder? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ServiceAction.START.name -> start()
            ServiceAction.STOP.name -> stop()
        }
        return START_NOT_STICKY
    }

    private fun start() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        val notification = NotificationCompat.Builder(this, "location")
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(createRemoteView("Location: not identified"))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val updatedNotification = notification.setCustomContentView(
                    createRemoteView(
                        "Location: (${lat}, ${long})"
                    )
                )
                repository.insertLocation(LocationData(latitude = lat, longitude = long))
                Log.d("TTT", "Location: ($lat, $long)")
                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createRemoteView(textDescription: String): RemoteViews {
        val intent = Intent(this, LocationService::class.java).apply {
            action = ServiceAction.STOP.name
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val view = RemoteViews(this.packageName, R.layout.remote_view)
        view.setTextViewText(R.id.textTitle, "Tracking location...")
        view.setTextViewText(R.id.textDescription, textDescription)
        view.setOnClickPendingIntent(R.id.buttonCancel, pendingIntent)
        return view
    }

    private fun stop() {
//        stopForeground(true)
        stopSelf()
        Constant.isWorkingService = false
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}