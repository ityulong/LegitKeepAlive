package com.ght.keepalive.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.ght.keepalive.enumid.SettingId
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import timber.log.Timber

object StatusCheckerRegistry {

    private val checkers: Map<SettingId, (Context) -> Boolean> = mapOf(
        // Key 现在是类型安全的枚举，而不是易错的字符串
        SettingId.BATTERY_OPTIMIZATION to { context ->
            val check = XXPermissions.isGrantedPermissions(
                context,
                Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            )
            Timber.d("checkers,BATTERY_OPTIMIZATION：$check")
            check
        },SettingId.ALERT_WINDOW to { context ->
            val check = XXPermissions.isGrantedPermissions(
                context,
                Permission.SYSTEM_ALERT_WINDOW
            )
            Timber.d("checkers,SYSTEM_ALERT_WINDOW：$check")
            check
        }
        , SettingId.BACKGROUND_DATA to { context ->
            isBackgroundDataAllowed(context)
        }
        , SettingId.XIAOMI_BACKGROUND_POPUP to { context ->
            OemPermissionHelper.isXiaomiBackgroundPopupAllowed(context)
        }
        , SettingId.XIAOMI_LOCK_POPUP to { context ->
            OemPermissionHelper.canXiaomiShowWhenLocked(context)
        }
    )

    /**
     * 公开的检查方法，参数类型也变为 SettingId
     */
    fun check(id: SettingId, context: Context): Boolean {
        val checker = checkers[id]
        return checker?.invoke(context) ?: false
    }

    private fun isBackgroundDataAllowed(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 检查安卓版本是否支持 getRestrictBackgroundStatus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val status = connMgr.restrictBackgroundStatus
            when (status) {
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED -> return false

                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED -> return true

                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED -> return true
            }
        }

        // 对于安卓 7.0 以下的旧版本，我们可以检查一个已废弃的设置。
        // 在大多数情况下，我们可以认为它是允许的，因为旧系统缺少精细化控制。
        // 注意: getBackgroundDataSetting() 在 API 24 中已废弃，并且总是返回 true。
        // noinspection deprecation
        return connMgr.backgroundDataSetting
    }
}