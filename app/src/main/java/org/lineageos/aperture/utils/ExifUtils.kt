/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.utils

import androidx.exifinterface.media.ExifInterface
import org.lineageos.aperture.models.Rotation
import org.lineageos.aperture.models.Transform
import java.io.InputStream

class ExifUtils {
    companion object {
        private val orientationMap = mapOf(
            ExifInterface.ORIENTATION_UNDEFINED to Transform.DEFAULT,
            ExifInterface.ORIENTATION_NORMAL to Transform.DEFAULT,
            ExifInterface.ORIENTATION_ROTATE_90 to Transform(Rotation.ROTATION_90, false),
            ExifInterface.ORIENTATION_ROTATE_180 to Transform(Rotation.ROTATION_180, false),
            ExifInterface.ORIENTATION_ROTATE_270 to Transform(Rotation.ROTATION_270, false),
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to Transform(Rotation.ROTATION_0, true),
            ExifInterface.ORIENTATION_FLIP_VERTICAL to Transform(Rotation.ROTATION_180, true),
            ExifInterface.ORIENTATION_TRANSPOSE to Transform(Rotation.ROTATION_270, true),
            ExifInterface.ORIENTATION_TRANSVERSE to Transform(Rotation.ROTATION_90, true),
        )

        fun orientationToTransform(exifOrientation: Int): Transform {
            return orientationMap.getOrDefault(exifOrientation, Transform.DEFAULT)
        }

        fun getTransform(fileDescriptor: java.io.FileDescriptor): Transform {
            val orientation = ExifInterface(fileDescriptor).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            return orientationToTransform(orientation)
        }

        fun getTransform(inputStream: InputStream): Transform {
            val stream = if (inputStream.markSupported()) inputStream else java.io.BufferedInputStream(inputStream)
            stream.mark(1024 * 1024)
            val orientation = ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            try {
                stream.reset()
            } catch (_: Exception) {}
            return orientationToTransform(orientation)
        }
    }
}
