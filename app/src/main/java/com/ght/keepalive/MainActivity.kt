package com.ght.keepalive

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ght.keepalive.adapt.KeepAliveAdapter
import com.ght.keepalive.enumid.SettingId
import com.ght.keepalive.model.AllConfigs
import com.ght.keepalive.model.SettingItem
import com.ght.keepalive.permission.PermissionConverter
import com.ght.keepalive.permission.PermissionInterceptor
import com.ght.keepalive.util.StatusCheckerRegistry
import com.ght.keepalive.util.SystemUtils
import com.ght.keepalive.util.ToastUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.BufferedReader

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val barColor = getColor(R.color.white)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        // onResume 中加载，以便从设置页返回时刷新状态
        loadAndDisplaySettings()
    }

    private fun loadAndDisplaySettings() {
        val allConfigs = loadConfigsFromAssets()
        if (allConfigs == null) {
            Toast.makeText(this, "配置文件加载失败", Toast.LENGTH_SHORT).show()
            return
        }

        val manufacturer = Build.MANUFACTURER.lowercase()
        val relevantSettings = getRelevantSettings(allConfigs, manufacturer)

        if (relevantSettings.isEmpty()) {
            Toast.makeText(this, "没有找到适用于您设备的优化项", Toast.LENGTH_SHORT).show()
            recyclerView.adapter = null
        } else {
            recyclerView.adapter = KeepAliveAdapter(
                relevantSettings,
                this::checkSettingStatus
            ) { settingItem ->
                executeNavigation(settingItem)
            }
        }
    }

    private fun loadConfigsFromAssets(): AllConfigs? {
        return try {
            assets.open("keep_alive_config.json").bufferedReader().use(BufferedReader::readText)
                .let { json.decodeFromString(AllConfigs.serializer(), it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getRelevantSettings(
        allConfigs: AllConfigs,
        manufacturer: String
    ): List<SettingItem> {
        val manufacturerConfig = allConfigs.configs[manufacturer] ?: allConfigs.configs["default"]
        if (manufacturerConfig == null) return emptyList()

        val osVersionProperty = manufacturerConfig.os_version_property
        if (osVersionProperty.isNullOrEmpty()) {
            return manufacturerConfig.default_settings
        }

        val actualVersion = SystemUtils.getSystemProperty(osVersionProperty)
        Timber.d("finding setting item by:$manufacturer,$actualVersion")
        if (actualVersion.isNullOrEmpty()) {
            return manufacturerConfig.default_settings
        }

        val versionSpecificSetting = manufacturerConfig.version_specifics?.find {
            val regex = Regex(it.version_matches_regex)
            regex.matches(actualVersion)
        }


        val specificItems = versionSpecificSetting?.settings ?: manufacturerConfig.default_settings
        // 合并并去重，确保每个 id 只出现一次
        return (specificItems).distinctBy { it.id }
    }

    private fun checkSettingStatus(settingId: SettingId): Boolean {
        return StatusCheckerRegistry.check(settingId, this)
    }

    private fun executeNavigation(setting: SettingItem) {
        try {
            setting.intent.permission?.let {

                XXPermissions.with(this)
                    .permission(it)
                    .interceptor(PermissionInterceptor())
                    .request(OnPermissionCallback { permissions, allGranted ->
                        if (!allGranted) {
                            return@OnPermissionCallback
                        }
                        ToastUtil.showShort(
                            java.lang.String.format(
                                getString(R.string.demo_obtain_permission_success_hint),
                                PermissionConverter.getNamesByPermissions(this, permissions)
                            )
                        )
                    })
                return
            }
            val intent = Intent()
            val intentInfo = setting.intent
            if (intentInfo.action != null) {
                intent.action = intentInfo.action
            }
            if (intentInfo.component != null) {
                val componentInfo = intentInfo.component.split("/")
                intent.component = ComponentName(componentInfo[0], componentInfo[1])
            }
            if (intentInfo.isAllNullOrEmpty) {
                Timber.e("intent info data is null!")
                Toast.makeText(this, "跳转指令配置错误", Toast.LENGTH_SHORT).show()
                return
            }
            intent.putExtra("package_name", packageName)
            intent.putExtra("extra_pkgname", packageName)
            intent.putExtra("package_label", getAppName(this))
            intent.setData(Uri.fromParts("package", packageName, null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d( "无法打开该设置界面，您的系统可能不支持", Toast.LENGTH_LONG)
            XXPermissions.startPermissionActivity(this)
        }
    }

    private fun getAppName(context: Context): String {
        try {
            return context.applicationInfo.loadLabel(context.packageManager).toString()
        } catch (e: java.lang.Exception) {
            Timber.e("Failed to get app name.")
            return "" // 返回空字符串作为后备
        }
    }
}