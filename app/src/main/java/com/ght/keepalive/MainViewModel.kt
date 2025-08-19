package com.ght.keepalive

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ght.keepalive.model.ManufacturerConfig
import com.ght.keepalive.model.SettingItem
import com.ght.keepalive.util.SystemUtils
import com.ght.repository.SettingsRepository
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    private val _settings = MutableLiveData<List<SettingItem>>()
    val settings: LiveData<List<SettingItem>> = _settings

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadSettings() {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val manufacturerConfig = repository.loadManufacturerConfig(manufacturer)

        if (manufacturerConfig == null) {
            _error.value = "配置文件加载失败"
            return
        }

        val relevantSettings = getRelevantSettings(manufacturerConfig, manufacturer)

        if (relevantSettings.isEmpty()) {
            _error.value = "没有找到适用于您设备的优化项"
            _settings.value = emptyList()
        } else {
            _settings.value = relevantSettings
        }
    }

    private fun getRelevantSettings(
        manufacturerConfig: ManufacturerConfig,
        manufacturer: String
    ): List<SettingItem> {
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
}