package com.ght.keepalive.enumid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 使用枚举统一管理所有的设置项 ID，彻底消除魔术字符串。
 * 每个枚举成员都通过 @SerialName 注解与 JSON 文件中的字符串值精确对应。
 */
@Serializable
enum class SettingId {

    // --- 通用设置 ---

    @SerialName("battery_optimization")
    BATTERY_OPTIMIZATION,

    @SerialName("background_data")
    BACKGROUND_DATA,

    @SerialName("alert_window")
    ALERT_WINDOW,

    // --- 小米专属设置 ---

    @SerialName("xiaomi_power_saving_strategy")
    XIAOMI_POWER_SAVING_STRATEGY,

    @SerialName("xiaomi_background_popup")
    XIAOMI_BACKGROUND_POPUP,

    @SerialName("xiaomi_lock_popup")
    XIAOMI_LOCK_POPUP,

    // --- 荣耀/华为专属设置 ---


    @SerialName("background_activity")
    BACKGROUND_ACTIVITY,


    // ✨ 未来如果 JSON 文件中新增任何厂商或新的 id，
    // ✨ 只需在这里添加对应的枚举成员即可。
}