package com.example.natraj.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

object LocationHelper {
    private const val TAG = "LocationHelper"
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    
    data class AddressInfo(
        val fullAddress: String = "",
        val addressLine: String = "",
        val city: String = "",
        val state: String = "",
        val pincode: String = "",
        val country: String = ""
    )
    
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            return null
        }
        
        if (!isLocationEnabled(context)) {
            Log.w(TAG, "Location services disabled")
            return null
        }
        
        val fusedLocationClient: FusedLocationProviderClient = 
            LocationServices.getFusedLocationProviderClient(context)
        
        return suspendCancellableCoroutine { continuation ->
            try {
                val cancellationTokenSource = CancellationTokenSource()
                
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
                
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        Log.w(TAG, "Location is null, trying last known location")
                        // Fallback to last known location
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                            continuation.resume(lastLocation)
                        }.addOnFailureListener {
                            Log.e(TAG, "Failed to get last known location", it)
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get current location", e)
                    continuation.resume(null)
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception getting location", e)
                continuation.resume(null)
            } catch (e: Exception) {
                Log.e(TAG, "Exception getting location", e)
                continuation.resume(null)
            }
        }
    }
    
    suspend fun getAddressFromLocation(context: Context, location: Location): AddressInfo {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ - use async API
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            continuation.resume(parseAddress(address))
                        } else {
                            Log.w(TAG, "No address found for location")
                            continuation.resume(AddressInfo())
                        }
                    }
                }
            } else {
                // Older Android versions - use synchronous API
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                
                if (!addresses.isNullOrEmpty()) {
                    parseAddress(addresses[0])
                } else {
                    Log.w(TAG, "No address found for location")
                    AddressInfo()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address from location", e)
            AddressInfo()
        }
    }
    
    private fun parseAddress(address: Address): AddressInfo {
        val addressLine = buildString {
            if (address.subThoroughfare != null) append("${address.subThoroughfare}, ")
            if (address.thoroughfare != null) append("${address.thoroughfare}, ")
            if (address.subLocality != null) append("${address.subLocality}, ")
            if (address.locality != null) append(address.locality)
        }.trim().removeSuffix(",")
        
        return AddressInfo(
            fullAddress = address.getAddressLine(0) ?: "",
            addressLine = addressLine,
            city = address.locality ?: address.subAdminArea ?: "",
            state = address.adminArea ?: "",
            pincode = address.postalCode ?: "",
            country = address.countryName ?: "India"
        )
    }
    
    suspend fun detectAndFillAddress(context: Context): AddressInfo? {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Cannot detect location - permission not granted")
            return null
        }
        
        if (!isLocationEnabled(context)) {
            Log.w(TAG, "Cannot detect location - location services disabled")
            return null
        }
        
        val location = getCurrentLocation(context) ?: return null
        return getAddressFromLocation(context, location)
    }
}
