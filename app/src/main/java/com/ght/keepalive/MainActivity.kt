package com.ght.keepalive

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ght.keepalive.adapt.KeepAliveAdapter
import com.ght.keepalive.enumid.SettingId
import com.ght.keepalive.model.SettingItem
import com.ght.keepalive.permission.PermissionConverter
import com.ght.keepalive.permission.PermissionInterceptor
import com.ght.keepalive.util.StatusCheckerRegistry
import com.ght.keepalive.util.ToastUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.settings.observe(this) { settings ->
            if (settings.isNotEmpty()) {
                recyclerView.adapter = KeepAliveAdapter(
                    settings,
                    this::checkSettingStatus
                ) { settingItem ->
                    executeNavigation(settingItem)
                }
            } else {
                recyclerView.adapter = null
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSettings()
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
                            String.format(
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