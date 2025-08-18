package com.ght.keepalive.util


import java.io.BufferedReader
import java.io.InputStreamReader

object SystemUtils {
    fun getSystemProperty(key: String): String? {
        try {
            val clz = Class.forName("android.os.SystemProperties")
            val getMethod = clz.getMethod("get", String::class.java, String::class.java)
            return getMethod.invoke(clz, key, null) as? String
        } catch (e: Exception) {
            return getSystemPropertyByShell(key)
        }
    }

    private fun getSystemPropertyByShell(key: String): String? {
        try {
            val process = Runtime.getRuntime().exec("getprop $key")
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                return reader.readLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}