package com.ght.keepalive.model

import com.ght.keepalive.enumid.SettingId
import kotlinx.serialization.Serializable

// AllConfigs 已被移除

@Serializable
data class ManufacturerConfig(
    val comment: String,
    val os_version_property: String? = null,
    val default_settings: List<SettingItem>,
    val version_specifics: List<VersionSpecific>? = null
)

@Serializable
data class VersionSpecific(
    val version_matches_regex: String,
    val comment: String,
    val settings: List<SettingItem>
)

@Serializable
data class SettingItem(
    val id: SettingId,
    val title: String,
    val description: String,
    val is_checkable: Boolean = false, // 包含默认值的关键字段
    val intent: IntentInfo
)

@Serializable
data class IntentInfo(
    val action: String? = null,
    val permission: String? = null,
    val component: String? = null
){
    /**
     * 如果你认为 null 和空字符串 "" 都算“空”，可以使用这个版本。
     * 这种方式更健壮。
     */
    val isAllNullOrEmpty: Boolean
        get() = action.isNullOrEmpty() && permission.isNullOrEmpty() && component.isNullOrEmpty()

}