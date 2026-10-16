/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.ext

import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.core.ImageCapture
import androidx.camera.core.TorchState
import androidx.camera.view.CameraController
import org.lineageos.aperture.models.FlashMode

var CameraController.flashMode: FlashMode
    get() = when (torchState.value) {
        TorchState.ON -> FlashMode.ON
        else -> when (imageCaptureFlashMode) {
            ImageCapture.FLASH_MODE_AUTO -> FlashMode.AUTO
            ImageCapture.FLASH_MODE_ON -> FlashMode.ON
            ImageCapture.FLASH_MODE_OFF -> FlashMode.OFF
            ImageCapture.FLASH_MODE_SCREEN -> FlashMode.SCREEN
            else -> throw Exception("Invalid flash mode")
        }
    }
    set(value) {
        enableTorch(value == FlashMode.TORCH || value == FlashMode.ON)

        imageCaptureFlashMode = when (value) {
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.ON -> ImageCapture.FLASH_MODE_OFF
            FlashMode.TORCH -> ImageCapture.FLASH_MODE_OFF
            FlashMode.SCREEN -> ImageCapture.FLASH_MODE_SCREEN
        }
    }

val CameraController.camera2CameraControl: Camera2CameraControl?
    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    get() = cameraControl?.let { Camera2CameraControl.from(it) }
