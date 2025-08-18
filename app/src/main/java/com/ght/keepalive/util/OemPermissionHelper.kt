package com.ght.keepalive.util

import android.app.AppOpsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Process
import timber.log.Timber

/**
 * 针对国内特定厂商（OEM, 如小米、VIVO）的非标准权限检测工具类。
 * 主要用于检查“后台弹出界面”和“锁屏显示”等特殊权限。
 * 日志记录已适配 Timber。
 */
object OemPermissionHelper {
    // 使用 Timber 后，不再需要手动定义 TAG

    // 小米权限对应的 op code
    private const val OP_XIAOMI_BACKGROUND_POPUP = 10021 // 后台弹出界面
    private const val OP_XIAOMI_SHOW_WHEN_LOCKED = 10020 // 锁屏显示

    // VIVO 权限查询相关的 Uri
    private val VIVO_URI_BG_START_ACTIVITY = Uri.parse("content://com.vivo.permissionmanager.provider.permission/start_bg_activity")
    private val VIVO_URI_LOCK_SCREEN_ACTION = Uri.parse("content://com.vivo.permissionmanager.provider.permission/control_locked_screen_action")

    // VIVO 权限返回值
    const val VIVO_PERMISSION_ALLOWED = 0 // 允许
    const val VIVO_PERMISSION_DENIED = 1  // 拒绝



    /**
     * 检测小米的“后台弹出界面”权限是否开启。
     * 为保证Java代码兼容性，添加了 @JvmStatic。如果项目为纯Kotlin，可以移除。
     *
     * @param context 上下文
     * @return `true` 表示权限已开启, `false` 表示权限未开启或检测失败
     */
    @JvmStatic
    fun isXiaomiBackgroundPopupAllowed(context: Context): Boolean {
        return checkXiaomiOp(context, OP_XIAOMI_BACKGROUND_POPUP)
    }

    /**
     * 检测小米的“锁屏显示”权限是否开启。
     * 为保证Java代码兼容性，添加了 @JvmStatic。如果项目为纯Kotlin，可以移除。
     *
     * @param context 上下文
     * @return `true` 表示权限已开启, `false` 表示权限未开启或检测失败
     */
    @JvmStatic
    fun canXiaomiShowWhenLocked(context: Context): Boolean {
        return checkXiaomiOp(context, OP_XIAOMI_SHOW_WHEN_LOCKED)
    }

    /**
     * 检测 VIVO 的“后台弹出界面”权限状态。
     * 为保证Java代码兼容性，添加了 @JvmStatic。如果项目为纯Kotlin，可以移除。
     *
     * @param context 上下文
     * @return [VIVO_PERMISSION_ALLOWED] (0) 表示开启, [VIVO_PERMISSION_DENIED] (1) 表示关闭或查询失败
     */
    @JvmStatic
    fun getVivoBgStartPermissionStatus(context: Context): Int {
        return queryVivoPermission(context, VIVO_URI_BG_START_ACTIVITY)
    }

    /**
     * 检测 VIVO 的“锁屏显示”权限状态。
     * 为保证Java代码兼容性，添加了 @JvmStatic。如果项目为纯Kotlin，可以移除。
     *
     * @param context 上下文
     * @return [VIVO_PERMISSION_ALLOWED] (0) 表示开启, [VIVO_PERMISSION_DENIED] (1) 表示关闭或查询失败
     */
    @JvmStatic
    fun getVivoLockStatus(context: Context): Int {
        return queryVivoPermission(context, VIVO_URI_LOCK_SCREEN_ACTION)
    }

    /**
     * 统一查询小米 AppOps 权限的私有方法
     */
    private fun checkXiaomiOp(context: Context, op: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false
        }
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        if (ops == null) {
            // 改动点 1: 使用 Timber 记录错误
            Timber.e("AppOpsManager is null, cannot check permission.")
            return false
        }

        try {
            val method = ops.javaClass.getMethod(
                "checkOpNoThrow",
                Int::class.java,
                Int::class.java,
                String::class.java
            )
            val result = method.invoke(
                ops,
                op,
                Process.myUid(),
                context.packageName
            ) as? Int

            return result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            // 改动点 2: 使用 Timber 记录异常和上下文信息
            Timber.e(e, "checkOpNoThrow for op %d not supported", op)
        }
        return false
    }

    /**
     * 统一查询 VIVO 权限的私有方法
     */
    private fun queryVivoPermission(context: Context, uri: Uri): Int {
        val packageName = context.packageName
        val selection = "pkgname = ?"
        val selectionArgs = arrayOf(packageName)

        try {
            context.contentResolver.query(uri, null, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex("currentstate")
                    if (index != -1) {
                        return cursor.getInt(index)
                    }
                }
            }
        } catch (e: Throwable) {
            // 改动点 3: 使用 Timber 记录异常
            Timber.e(e, "Error querying vivo permission for uri: %s", uri)
        }
        return VIVO_PERMISSION_DENIED
    }

    /**
     * 检查本应用是否可以在后台使用数据（对应安卓系统的“流量节省程序”功能）。
     *
     * @param context a Context
     * @return true 如果后台数据未受限制, false 如果被限制。
     */
    @JvmStatic
    fun isBackgroundDataAllowed(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connMgr == null) {
            Timber.w("ConnectivityManager is null. Assuming background data is allowed.")
            return true // 获取服务失败，返回一个安全的默认值
        }

        return when (connMgr.restrictBackgroundStatus) {
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED -> false // 被限制
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED, // 在白名单中
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED -> true // 功能未开启
            else -> true
        }
    }
}