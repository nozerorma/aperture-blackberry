/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.lineageos.aperture.R
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    private const val GITHUB_API_URL =
        "https://api.github.com/repos/nozerorma/aperture-blackberry/releases/latest"

    data class UpdateInfo(
        val tagName: String,
        val releaseName: String,
        val htmlUrl: String,
        val apkDownloadUrl: String?
    )

    suspend fun checkUpdate(currentVersionName: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Aperture-BlackBerry-Updater")
                connectTimeout = 6000
                readTimeout = 6000
            }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val tagName = json.optString("tag_name", "")
                val releaseName = json.optString("name", tagName)
                val htmlUrl = json.optString("html_url", "")

                var apkUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url").takeIf { it.isNotEmpty() }
                            break
                        }
                    }
                }

                if (isNewerVersion(tagName, currentVersionName)) {
                    UpdateInfo(tagName, releaseName, htmlUrl, apkUrl)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
        val remoteClean = remoteTag.removePrefix("v").trim()
        val currentClean = currentVersion.removePrefix("v").trim()
        if (remoteClean.isEmpty() || currentClean.isEmpty()) return false

        val remoteParts = remoteClean.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    fun showUpdateDialog(context: Context, updateInfo: UpdateInfo) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.bb_update_available_title))
            .setMessage(context.getString(R.string.bb_update_available_message, updateInfo.tagName))
            .setPositiveButton(context.getString(R.string.bb_update_download)) { _, _ ->
                val targetUrl = updateInfo.apkDownloadUrl ?: updateInfo.htmlUrl
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            .setNegativeButton(context.getString(R.string.bb_update_later), null)
            .show()
    }
}
