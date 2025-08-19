package com.ght.repository

import android.content.Context
import com.ght.keepalive.model.ManufacturerConfig
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.FileNotFoundException

class SettingsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun loadManufacturerConfig(manufacturer: String): ManufacturerConfig? {
        return try {
            // 尝试加载厂商专属配置文件
            val fileName = "$manufacturer.json"
            context.assets.open(fileName).bufferedReader().use(BufferedReader::readText)
                .let { json.decodeFromString(ManufacturerConfig.serializer(), it) }
        } catch (e: FileNotFoundException) {
            // 如果找不到，则加载默认配置文件
            try {
                context.assets.open("default.json").bufferedReader().use(BufferedReader::readText)
                    .let { json.decodeFromString(ManufacturerConfig.serializer(), it) }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}