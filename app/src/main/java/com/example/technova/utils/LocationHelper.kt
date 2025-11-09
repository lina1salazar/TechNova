package com.example.technova.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class LocationHelper(private val activity: Activity) {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(activity) }


    var locationListener: ((Location) -> Unit)? = null

    private var singleCallback: LocationCallback? = null

    /** Llama esto SOLO después de tener permiso concedido (desde la Activity). */
    fun start() {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getLastLocation()
        } else {
            Toast.makeText(activity, "Faltan permisos de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    /** Detener actualizaciones pendientes (buena práctica) */
    fun stop() {
        // si hay callback de single update, removerlo
        singleCallback?.let { fused.removeLocationUpdates(it) }
    }

    /** Ya verificamos permisos antes de entrar aquí. */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fused.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    locationListener?.invoke(loc)
                    Toast.makeText(
                        activity,
                        "Lat: ${loc.latitude}, Lng: ${loc.longitude}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    requestSingleUpdate()
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error al obtener ubicación: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Actualización única si no hay ubicación previa en caché. */
    @SuppressLint("MissingPermission")
    private fun requestSingleUpdate() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L
        ).setMaxUpdates(1).build()

        val singleCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                locationListener?.invoke(loc)
                Toast.makeText(
                    activity,
                    "Lat: ${loc.latitude}, Lng: ${loc.longitude}",
                    Toast.LENGTH_LONG
                ).show()
                fused.removeLocationUpdates(this)
            }
        }
        fused.requestLocationUpdates(request, singleCallback as LocationCallback, activity.mainLooper)
    }
}
