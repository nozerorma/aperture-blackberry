/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.models

enum class FrameRate(val value: Int) {
    FPS_24(24),
    FPS_30(30),
    FPS_60(60),
    FPS_120(120);

    val range: ClosedRange<Int>
        get() = when (this) {
            FPS_30 -> 5..30
            FPS_24 -> 5..24
            FPS_60 -> 15..60
            FPS_120 -> 30..120
        }

    companion object {
        fun fromValue(value: Int) = entries.firstOrNull { it.value == value }
        fun fromRange(range: ClosedRange<Int>) = fromValue(range.endInclusive)
    }
}
