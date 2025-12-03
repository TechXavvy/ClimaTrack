package ph.edu.auf.xavier.ardillo.climatrack.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

object LocationUtils {

    // --- Permission helpers ---
    fun hasFine(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    fun hasCoarse(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    fun hasAnyLocation(context: Context): Boolean = hasFine(context) || hasCoarse(context)

    // --- Freshness / accuracy thresholds (tweak if you want) ---
    private const val DEFAULT_MAX_AGE_MS: Long = 2 * 60 * 1000L  // 2 minutes
    private const val DEFAULT_MIN_ACCURACY_M: Float = 200f       // 200 meters

    /**
     * Returns a "fresh enough" (lat, lon), or null if permission missing or no fix.
     * Strategy:
     *  1) Try lastLocation, but only accept if recent + reasonably accurate.
     *  2) Otherwise, request one-shot current location.
     */
    suspend fun getFreshLatLon(
        context: Context,
        maxAgeMs: Long = DEFAULT_MAX_AGE_MS,
        minAccuracyMeters: Float = DEFAULT_MIN_ACCURACY_M
    ): Pair<Double, Double>? {
        if (!hasAnyLocation(context)) return null

        val client = LocationServices.getFusedLocationProviderClient(context)

        // 1) Try last known — accept only if "fresh"
        val last = try { client.suspendLastLocation() } catch (_: SecurityException) { null }
        if (last != null && isFreshEnough(last, maxAgeMs, minAccuracyMeters)) {
            return last.latitude to last.longitude
        }

        // 2) Force a one-shot current location
        val priority = if (hasFine(context))
            Priority.PRIORITY_HIGH_ACCURACY
        else
            Priority.PRIORITY_BALANCED_POWER_ACCURACY

        val cur = try { client.suspendCurrentLocation(priority) } catch (_: SecurityException) { null }
        return cur?.let { it.latitude to it.longitude }
    }

    /** Basic freshness check: age <= maxAgeMs and accuracy <= minAccuracyMeters (if accuracy present) */
    private fun isFreshEnough(loc: Location, maxAgeMs: Long, minAccuracyMeters: Float): Boolean {
        val now = System.currentTimeMillis()
        val ageOk = abs(now - loc.time) <= maxAgeMs
        val accOk = if (loc.hasAccuracy()) loc.accuracy <= minAccuracyMeters else true
        return ageOk && accOk
    }
}

/* ------------------ Private suspend bridges ------------------ */
@SuppressLint("MissingPermission")
private suspend fun FusedLocationProviderClient.suspendLastLocation(): Location? =
    suspendCancellableCoroutine { cont ->
        lastLocation
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { _ -> cont.resume(null) }
    }

@SuppressLint("MissingPermission")
private suspend fun FusedLocationProviderClient.suspendCurrentLocation(
    priority: Int
): Location? = suspendCancellableCoroutine { cont ->
    val cts = CancellationTokenSource()
    getCurrentLocation(priority, cts.token)
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { e -> cont.resumeWithException(e) }
    cont.invokeOnCancellation { cts.cancel() }
}
