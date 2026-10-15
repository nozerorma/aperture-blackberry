/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import org.lineageos.aperture.ext.mapToRange
import org.lineageos.aperture.ext.px

class VerticalSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Slider(context, attrs, defStyleAttr) {

    var showTickers = true
    var topLabel: String? = null
    var bottomLabel: String? = null

    private val tickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        setShadowLayer(2f, 0f, 0f, Color.BLACK)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 12.px.toFloat()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 0f, 0f, Color.BLACK)
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (!isEnabled) {
            return false
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                val track = track()
                val trackHeight = track.height()
                if (trackHeight > 0) {
                    val relativeY = (event.y - track.top).coerceIn(0f, trackHeight)
                    progress = (trackHeight - relativeY) / trackHeight
                    onProgressChangedByUser?.invoke(progress)
                }
            }
        }

        return true
    }

    override fun track(): RectF {
        val trackWidth = width / 5f

        val left = (width - trackWidth) / 2f
        val right = left + trackWidth

        val topPadding = if (topLabel != null) 20.px.toFloat() else (width / 2f)
        val bottomPadding = if (bottomLabel != null) 20.px.toFloat() else (width / 2f)

        val top = topPadding
        val bottom = height - bottomPadding

        return RectF(left, top, right, bottom)
    }

    override fun thumb(): Triple<Float, Float, Float> {
        val track = track()
        val trackHeight = track.height()

        val cx = width / 2f
        val cy = if (steps > 0) {
            val progress = Int.mapToRange(0..steps, progress).toFloat() / steps
            (trackHeight - (trackHeight * progress)) + track.top
        } else {
            (trackHeight - (trackHeight * progress)) + track.top
        }

        return Triple(cx, cy, width / 2.3f)
    }

    override fun onDraw(canvas: Canvas) {
        if (showTickers) {
            val track = track()
            val trackHeight = track.height()
            val trackBottom = track.bottom
            val cx = width / 2f

            topLabel?.let {
                canvas.drawText(it, cx, track.top - 6.px, labelPaint)
            }
            bottomLabel?.let {
                canvas.drawText(it, cx, trackBottom + 16.px, labelPaint)
            }

            val numTicks = 10
            for (i in 0..numTicks) {
                val y = trackBottom - (trackHeight * (i.toFloat() / numTicks))
                val isMajor = (i == 0 || i == 5 || i == numTicks)
                val tickLen = if (isMajor) 6.px.toFloat() else 3.px.toFloat()
                tickerPaint.strokeWidth = if (isMajor) 2.px.toFloat() else 1.px.toFloat()
                tickerPaint.color = if (isMajor) Color.WHITE else Color.parseColor("#99FFFFFF")

                // Right tick
                canvas.drawLine(track.right + 2.px, y, track.right + 2.px + tickLen, y, tickerPaint)
            }
        }

        super.onDraw(canvas)
    }
}
