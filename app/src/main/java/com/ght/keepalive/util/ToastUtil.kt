package com.ght.keepalive.util

import android.widget.Toast
import com.ght.keepalive.MyApplication
import timber.log.Timber

class ToastUtil {
    companion object {
        fun showShort(msg: String) {
            Timber.i(msg)
            Toast.makeText(MyApplication.instance, msg, Toast.LENGTH_SHORT).show()
        }
        fun showShortError(msg: String) {
            Timber.e(msg)
            Toast.makeText(MyApplication.instance, msg, Toast.LENGTH_SHORT).show()
        }

        fun showLong(msg: String) {
            Timber.i(msg)
            Toast.makeText(MyApplication.instance, msg, Toast.LENGTH_LONG).show()
        }
    }
}