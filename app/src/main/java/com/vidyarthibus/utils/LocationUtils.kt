package com.vidyarthibus.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import com.vidyarthibus.data.model.BusRoute
import com.vidyarthibus.data.model.BusStop
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocationUtils {

    const val GEOFENCE_RADIUS_M = 600.0f

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location =
        suspendCancellableCoroutine { cont ->
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(
                    CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                        .build(), null
                )
                .addOnSuccessListener { loc ->
                    if (loc != null) cont.resume(loc)
                    else cont.resumeWithException(Exception("GPS unavailable"))
                }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    /** Returns nearest stop within geofence, or null if user is too far (report blocked). */
    fun nearestAllowedStop(userLoc: Location, route: BusRoute): BusStop? =
        route.stops
            .associateWith { distanceTo(userLoc, it.latitude, it.longitude) }
            .filter { (_, d) -> d <= GEOFENCE_RADIUS_M }
            .minByOrNull { (_, d) -> d }
            ?.key

    fun distanceTo(from: Location, lat: Double, lng: Double): Float {
        val r = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, lat, lng, r)
        return r[0]
    }

    fun formatDistance(metres: Float) =
        if (metres < 1000) "${metres.toInt()} m" else "%.1f km".format(metres / 1000)
}
