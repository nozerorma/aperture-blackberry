/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.utils

import android.content.res.Resources
import android.media.MediaActionSound
import android.os.Build
import org.lineageos.aperture.repositories.PreferencesRepository

class CameraSoundsUtils(private val preferencesRepository: PreferencesRepository) {
    private var _mediaActionSound: MediaActionSound? = null
    private val mediaActionSound: MediaActionSound
        get() = _mediaActionSound ?: MediaActionSound().apply {
            load(MediaActionSound.SHUTTER_CLICK)
            load(MediaActionSound.START_VIDEO_RECORDING)
            load(MediaActionSound.STOP_VIDEO_RECORDING)
            _mediaActionSound = this
        }

    fun playShutterClick() {
        if (preferencesRepository.shutterSound.value || mustPlaySounds) {
            mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)
        }
    }

    fun playStartVideoRecording(): Boolean {
        if (preferencesRepository.shutterSound.value || mustPlaySounds) {
            mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING)
            return true
        }
        return false
    }

    fun playStopVideoRecording() {
        if (preferencesRepository.shutterSound.value || mustPlaySounds) {
            mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING)
        }
    }

    fun release() {
        _mediaActionSound?.release()
        _mediaActionSound = null
    }

    companion object {
        val mustPlaySounds: Boolean
            @Suppress("DiscouragedApi")
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                MediaActionSound.mustPlayShutterSound()
            } else {
                val resources = Resources.getSystem()
                val id = resources.getIdentifier("config_camera_sound_forced", "bool", "android")
                id > 0 && resources.getBoolean(id)
            }
    }
}
