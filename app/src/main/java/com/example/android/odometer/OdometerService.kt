package com.example.android.odometer

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import kotlin.random.Random

class OdometerService : Service() {
    private val binder: IBinder = OdometerBinder()
    private val random = Random

    inner class OdometerBinder : Binder() {
        val odometer: OdometerService
            get() = this@OdometerService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder

    }

    var getDistance = random.nextDouble()

}
