package com.trackingdeluxe.speedometer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class PermissionUtils {
    private var onPermissionResult: OnPermissionResult? = null
    private var appPermission = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    /**
     *As location permission
     *@param onPermissionResult action when user granted or denied permission
     */
    fun askPermission(
        appCompatActivity: AppCompatActivity,
        onPermissionResult: OnPermissionResult
    ) {
        askPermission(appCompatActivity, onPermissionResult, appPermission)
    }

    /**
     *Check permission result and action when permission is granted or denied
     *@param grantResults result permission asked permission granted or denied
     */
    fun onRequestPermissionsResult(grantResults: IntArray, permissions: Array<out String>) {
        val grantedPermissions = mutableListOf<String>()
        for ((index, grantResult) in grantResults.withIndex()) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[index])
            }
        }
        providePermissionResult(grantedPermissions.toTypedArray())
    }

    /**
     *Provide granted to app
     *@param grantedPermissions - array of granted permissions
     */
    private fun providePermissionResult(grantedPermissions: Array<out String>) {
        if (onPermissionResult != null) {
            onPermissionResult!!.permissionResult(grantedPermissions)
            onPermissionResult = null
        }
    }

    /**
     *Check need ask permission
     *@return value is need ask permission or not
     */
    private fun needRequestPermission(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    /**
     *Ask permission
     *@param onPermissionResult action when permission granted or denied
     *@param permissions array of permissions
     */
    private fun askPermission(
        context: AppCompatActivity,
        onPermissionResult: OnPermissionResult,
        permissions: Array<out String>
    ) {
        if (!needRequestPermission()) {
            onPermissionResult.permissionResult(permissions)
        } else if (hasAllPermissions(context, permissions)) {
            onPermissionResult.permissionResult(permissions)
        } else {
            this.onPermissionResult = onPermissionResult
            ActivityCompat.requestPermissions(
                context,
                permissions,
                AppConstants.PERMISSION_REQUEST_CODE
            )
        }
    }

    //Action when permission is granted or denied
    interface OnPermissionResult {
        fun permissionResult(grantedPermissions: Array<out String>)
    }

    /**
     *Check has permission from permissions array
     *@param permissions array of permissions
     *@return result is permission granted or denied
     */
    private fun hasAllPermissions(context: Context, permissions: Array<out String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED)
                return false
        }
        return true
    }

    /**
     *Check has location permission
     *@return result is permission granted or denied
     */
    fun hasPermission(context: Context) = hasAllPermissions(context, appPermission)

    fun hasLocationPermission(context: Context) =
        hasAllPermissions(context, appPermission.copyOfRange(0, 1))

    fun hasStoragePermission(context: Context) =
        hasAllPermissions(context, appPermission.copyOfRange(1, appPermission.size - 1))
}