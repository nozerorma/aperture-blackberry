/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.qr

import android.content.Context
import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.lineageos.aperture.models.QrResult
import zxingcpp.BarcodeReader

class QrImageAnalyzer(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : ImageAnalysis.Analyzer {
    // QR
    private val reader by lazy {
        BarcodeReader().apply {
            options.tryInvert = true
            options.tryRotate = true
            options.tryDownscale = true
        }
    }

    private val qrTextClassifier by lazy { QrTextClassifier(context) }

    private val _qrResult = MutableStateFlow<QrResult?>(null)
    val qrResult = _qrResult.filterNotNull()
    val rawQrResult = _qrResult.asStateFlow()

    private var lastAnalyzedTimestamp = 0L
    private var lastSeenTimestamp = 0L
    private var lastRawText: String? = null
    private var isDismissed = false

    override fun analyze(image: ImageProxy) {
        image.use {
            val now = SystemClock.uptimeMillis()
            // Throttle to ~8 checks/sec: real-time responsive on viewfinder, minimal CPU hit
            if (now - lastAnalyzedTimestamp < 120L) {
                return
            }
            lastAnalyzedTimestamp = now

            val results = try {
                reader.read(image)
            } catch (e: Exception) {
                android.util.Log.e("QrImageAnalyzer", "Error reading barcode image", e)
                emptyList()
            }

            val firstResult = results.firstOrNull()
            if (firstResult != null) {
                lastSeenTimestamp = now
                val rawText = firstResult.text
                if (isDismissed && rawText == lastRawText) {
                    return
                }
                isDismissed = false
                if (rawText != lastRawText || _qrResult.value == null) {
                    lastRawText = rawText
                    android.util.Log.d("QrImageAnalyzer", "Live QR detected: $rawText, classifying...")
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val classified = qrTextClassifier.classify(firstResult)
                            android.util.Log.d("QrImageAnalyzer", "Live QR classified: ${classified.text}, actions=${classified.actions.size}")
                            _qrResult.value = classified
                        } catch (e: Exception) {
                            android.util.Log.e("QrImageAnalyzer", "Error classifying QR result", e)
                        }
                    }
                }
            } else {
                // If QR code left the viewfinder for > 1500ms, auto-dismiss and reset dismissed state
                if (now - lastSeenTimestamp > 1500L) {
                    isDismissed = false
                    lastRawText = null
                    if (_qrResult.value != null) {
                        android.util.Log.d("QrImageAnalyzer", "Live QR cleared (left viewfinder)")
                        _qrResult.value = null
                    }
                }
            }
        }
    }

    fun dismissResult() {
        isDismissed = true
        _qrResult.value = null
    }

    private fun processResults(results: List<BarcodeReader.Result>) {
        results.firstOrNull()?.let { result ->
            coroutineScope.launch(Dispatchers.IO) {
                // Classify message
                val qrResult = qrTextClassifier.classify(result)

                _qrResult.emit(qrResult)
            }
        }
    }
}
