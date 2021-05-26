package com.example.android.odometer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private var odometer: OdometerService? = null
    private var bound: Boolean = false

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

    override fun onStart() {
        super.onStart()

        val intent = Intent(this, OdometerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    private fun displayDistance() {
        var distanceView = findViewById<TextView>(R.id.distance)
        //Handler allows switching between Main Thread and Worker Thread
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                var distance = 0.0
                if (bound && odometer != null) {
                    distance = odometer!!.getDistance
                }
                val distanceString = String.format(
                    Locale.getDefault(),
                    R.string.distance_format.toString(), distance
                )
                distanceView.text = distanceString
                handler.postDelayed(this, 1000)
            }
        })
    }
}