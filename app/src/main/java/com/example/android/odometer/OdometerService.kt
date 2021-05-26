package com.example.android.odometer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import kotlin.random.Random

class OdometerService : Service() {
    private val binder: IBinder = OdometerBinder()
    private val random = Random
    private var distanceInMeters = 0.0
    private var lastLocation: Location? = null
    private var locationManager: LocationManager? = null
    private var listener: LocationListener? = null
    val PERMISSION_STRING = android.Manifest.permission.ACCESS_FINE_LOCATION

    inner class OdometerBinder : Binder() {
        val odometer: OdometerService
            get() = this@OdometerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder

    }

    override fun onCreate() {
        super.onCreate()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
        == PackageManager.PERMISSION_GRANTED) {
            val provider = locationManager!!.getBestProvider(Criteria(), true)
            if (provider != null) {
                locationManager!!.requestLocationUpdates(provider, 1000, 1f, listener!!)
            }
        }

        listener = object : LocationListener{
            override fun onLocationChanged(location: Location) {
                if (lastLocation == null) {
                    lastLocation = location
                }
                distanceInMeters += location.distanceTo(lastLocation)
                lastLocation = location
            }

            @Suppress("DEPRECATION")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) =
                super.onStatusChanged(provider, status, extras)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null && listener != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING) == PackageManager.PERMISSION_GRANTED) {
                locationManager!!.removeUpdates(listener!!)
            }
            locationManager = null
            listener = null
        }
    }

    var getDistance = random.nextDouble()

}
