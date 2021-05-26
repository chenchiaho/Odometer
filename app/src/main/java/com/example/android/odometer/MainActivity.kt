package com.example.android.odometer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var odometer = OdometerService()
    private var bound: Boolean = false
    private val PERMISSION_REQUEST_CODE = 666
    private val NOTIFICATION_ID = 111

    private val connection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            val odometerBinder: OdometerService.OdometerBinder = binder as OdometerService.OdometerBinder
            odometer = odometerBinder.odometer
            bound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayDistance()
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, OdometerService::class.java)
                    bindService(intent, connection, BIND_AUTO_CREATE)
                } else {
                    val builder = NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_menu_compass)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.permission_denied))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(longArrayOf(1000, 1000))
                        .setAutoCancel(true)

                    val actionIntent = Intent(this, MainActivity::class.java)
                    val actionPendingIntent = PendingIntent.getActivities(
                        this, 0,
                        arrayOf(actionIntent), PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    builder.setContentIntent(actionPendingIntent)

                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                }
        }
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, odometer.PERMISSION_STRING)
            == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(odometer.PERMISSION_STRING),
                PERMISSION_REQUEST_CODE
            )
        } else {
            val intent = Intent(this, OdometerService::class.java)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    @Suppress("DEPRECATION")
    private fun displayDistance() {
        val distanceView = findViewById<TextView>(R.id.distance)
        //Handler allows switching between Main Thread and Worker Thread
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                var distance = 0.0
                if (bound) {
                    distance = odometer.getDistance
                }
                val distanceString = String.format(
                    Locale.getDefault(),
                    getString(R.string.distance_format), distance
                )
                distanceView.text = distanceString
                handler.postDelayed(this, 1000)
            }
        })
    }
}