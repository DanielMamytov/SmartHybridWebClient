// WebAppInterface.kt
package com.example.smarthybridwebclient.utils

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.example.smarthybridwebclient.presentation.main.MainActivity


class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun tryAgain() {
        if (context is MainActivity) {
            context.runOnUiThread {
                context.tryReload()
            }
        }
}


    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun log(message: String) {
        Log.d("SmartBridge", message)
    }

    @JavascriptInterface
    fun notifyUploadSuccess(fileName: String) {
        Toast.makeText(context, "Файл $fileName загружен", Toast.LENGTH_LONG).show()
    }
    @JavascriptInterface
    fun startGitHubLogin() {
        (context as MainActivity).startGitHubOAuth()
    }

}

// FileUtils.kt — Для получения имени файла из Uri



object FileUtils {
    fun getFileName(context: Context, uri: Uri): String {
        var result = "unknown"
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return result
    }
}
