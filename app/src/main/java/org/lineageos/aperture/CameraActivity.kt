/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture

import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.Intent
import android.content.pm.ActivityInfo
import org.lineageos.aperture.ext.sendWithBalAllowed
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import java.util.Locale
import android.util.Size
import kotlinx.coroutines.withContext
import org.lineageos.aperture.ext.transform
import org.lineageos.aperture.models.QrResult
import com.google.android.material.card.MaterialCardView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.MirrorMode
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.extensions.ExtensionMode
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.ScreenFlashView
import androidx.camera.viewfinder.core.ZoomGestureDetector
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.size.Scale
import coil3.video.VideoFrameDecoder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lineageos.aperture.ext.camera2CameraControl
import org.lineageos.aperture.ext.flashMode
import org.lineageos.aperture.ext.mapToRange
import org.lineageos.aperture.ext.px
import org.lineageos.aperture.ext.scale
import org.lineageos.aperture.ext.setColorCorrectionAberrationMode
import org.lineageos.aperture.ext.setDistortionCorrectionMode
import org.lineageos.aperture.ext.setEdgeMode
import org.lineageos.aperture.ext.setFrameRate
import org.lineageos.aperture.ext.setHotPixelMode
import org.lineageos.aperture.ext.setNoiseReductionMode
import org.lineageos.aperture.ext.setPadding
import org.lineageos.aperture.ext.setShadingMode
import org.lineageos.aperture.ext.setVideoStabilizationMode
import org.lineageos.aperture.ext.slide
import org.lineageos.aperture.ext.slideDown
import org.lineageos.aperture.ext.smoothRotate
import org.lineageos.aperture.ext.transform
import org.lineageos.aperture.ext.updateBarsVisibility
import org.lineageos.aperture.models.AssistantIntent
import org.lineageos.aperture.models.CameraConfiguration
import org.lineageos.aperture.models.CameraFacing
import org.lineageos.aperture.models.CameraMode
import org.lineageos.aperture.models.CameraState
import org.lineageos.aperture.models.Event
import org.lineageos.aperture.models.FlashMode
import org.lineageos.aperture.models.GestureAction
import org.lineageos.aperture.models.GridMode
import org.lineageos.aperture.models.HardwareKey
import org.lineageos.aperture.models.MediaType
import org.lineageos.aperture.models.Permission
import org.lineageos.aperture.models.PermissionState
import org.lineageos.aperture.models.PhotoOutputFormat
import org.lineageos.aperture.models.Rotation
import org.lineageos.aperture.models.ThermalStatus
import org.lineageos.aperture.models.TimerMode
import org.lineageos.aperture.models.VideoDynamicRange
import org.lineageos.aperture.models.VideoMirrorMode
import org.lineageos.aperture.models.VideoStabilizationMode
import org.lineageos.aperture.ui.dialogs.LocationPermissionsDialog
import org.lineageos.aperture.ui.dialogs.QrBottomSheetDialog
import org.lineageos.aperture.ui.views.CameraModeSelectorLayout
import org.lineageos.aperture.ui.views.CapturePreviewLayout
import org.lineageos.aperture.ui.views.CountDownView
import org.lineageos.aperture.ui.views.GridView
import org.lineageos.aperture.ui.views.HorizontalSlider
import org.lineageos.aperture.ui.views.IslandView
import org.lineageos.aperture.ui.views.LensSelectorLayout
import org.lineageos.aperture.ui.views.LevelerView
import org.lineageos.aperture.ui.views.PreviewBlurView
import org.lineageos.aperture.ui.views.VerticalSlider
import org.lineageos.aperture.utils.ExifUtils
import org.lineageos.aperture.utils.GoogleLensUtils
import org.lineageos.aperture.utils.PermissionsManager
import org.lineageos.aperture.utils.ShortcutsUtils
import org.lineageos.aperture.viewmodels.CameraViewModel
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.math.abs
import kotlin.reflect.safeCast
import androidx.camera.core.CameraState as CameraXCameraState

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class, ExperimentalZeroShutterLag::class)
open class CameraActivity : AppCompatActivity(R.layout.activity_camera) {
    // View models
    private val viewModel by viewModels<CameraViewModel>()

    // Views
    private val aspectRatioButton by lazy { findViewById<Button>(R.id.aspectRatioButton) }
    private val cameraModeSelectorLayout by lazy { findViewById<CameraModeSelectorLayout>(R.id.cameraModeSelectorLayout) }
    private val capturePreviewLayout by lazy { findViewById<CapturePreviewLayout>(R.id.capturePreviewLayout) }
    private val countDownView by lazy { findViewById<CountDownView>(R.id.countDownView) }
    private val effectButton by lazy { findViewById<Button>(R.id.effectButton) }
    private val exposureLevel by lazy { findViewById<VerticalSlider>(R.id.exposureLevel) }
    private val flashButton by lazy { findViewById<ImageButton>(R.id.flashButton) }
    private val flipCameraButton by lazy { findViewById<ImageButton>(R.id.flipCameraButton) }
    private val galleryButtonCardView by lazy { findViewById<CardView>(R.id.galleryButtonCardView) }
    private val galleryButtonIconImageView by lazy { findViewById<ImageView>(R.id.galleryButtonIconImageView) }
    private val galleryButtonPreviewImageView by lazy { findViewById<ImageView>(R.id.galleryButtonPreviewImageView) }
    private val googleLensButton by lazy { findViewById<ImageButton>(R.id.googleLensButton) }
    private val gridButton by lazy { findViewById<Button>(R.id.gridButton) }
    private val gridView by lazy { findViewById<GridView>(R.id.gridView) }
    private val islandView by lazy { findViewById<IslandView>(R.id.islandView) }
    private val lensSelectorLayout by lazy { findViewById<LensSelectorLayout>(R.id.lensSelectorLayout) }
    private val levelerView by lazy { findViewById<LevelerView>(R.id.levelerView) }
    private val mainLayout by lazy { findViewById<ConstraintLayout>(R.id.mainLayout) }
    private val micButton by lazy { findViewById<Button>(R.id.micButton) }
    private val previewBlurView by lazy { findViewById<PreviewBlurView>(R.id.previewBlurView) }
    private val proButton by lazy { findViewById<ImageButton>(R.id.proButton) }
    private val screenFlashView by lazy { findViewById<ScreenFlashView>(R.id.screenFlashView) }
    private val secondaryBottomBarLayout by lazy { findViewById<ConstraintLayout>(R.id.secondaryBottomBarLayout) }
    private val secondaryTopBarLayout by lazy { findViewById<HorizontalScrollView>(R.id.secondaryTopBarLayout) }
    private val settingsButton by lazy { findViewById<Button>(R.id.settingsButton) }
    private val shutterButton by lazy { findViewById<ImageButton>(R.id.shutterButton) }
    private val timerButton by lazy { findViewById<Button>(R.id.timerButton) }
    private val videoFrameRateButton by lazy { findViewById<Button>(R.id.videoFrameRateButton) }
    private val videoQualityButton by lazy { findViewById<Button>(R.id.videoQualityButton) }
    private val videoRecordingStateButton by lazy { findViewById<ImageButton>(R.id.videoRecordingStateButton) }
    private val videoDynamicRangeButton by lazy { findViewById<Button>(R.id.videoDynamicRangeButton) }
    private val viewFinder by lazy { findViewById<PreviewView>(R.id.viewFinder) }
    private val viewFinderFocus by lazy { findViewById<ImageView>(R.id.viewFinderFocus) }
    private val zoomLevel by lazy { findViewById<VerticalSlider>(R.id.zoomLevel) }

    // BlackBerry UI Views
    private val bbTopBarContainer by lazy { findViewById<LinearLayout>(R.id.bbTopBarLayout) }
    private val btnBbFlash by lazy { findViewById<ImageButton>(R.id.btnBbFlash) }
    private val btnBbTimer by lazy { findViewById<ImageButton>(R.id.btnBbTimer) }
    private val btnBbAspectRatio by lazy { findViewById<ImageButton>(R.id.btnBbAspectRatio) }
    private val btnBbHdr by lazy { findViewById<ImageButton>(R.id.btnBbHdr) }
    private val btnBbGrid by lazy { findViewById<ImageButton>(R.id.btnBbGrid) }
    private val btnBbSettings by lazy { findViewById<ImageButton>(R.id.btnBbSettings) }
    private val llBbSubmenu by lazy { findViewById<LinearLayout>(R.id.llBbSubmenu) }

    private val bbModeButton by lazy { findViewById<ImageButton>(R.id.bbModeButton) }
    private val bbModeGridContainer by lazy { findViewById<LinearLayout>(R.id.bbModeGridLayout) }
    private val modeItemPhoto by lazy { findViewById<LinearLayout>(R.id.modeItemPhoto) }
    private val modeItemVideo by lazy { findViewById<LinearLayout>(R.id.modeItemVideo) }
    private val modeItemPro by lazy { findViewById<LinearLayout>(R.id.modeItemPro) }
    private val ivModeIconPhoto by lazy { findViewById<ImageView>(R.id.ivModeIconPhoto) }
    private val ivModeIconVideo by lazy { findViewById<ImageView>(R.id.ivModeIconVideo) }
    private val ivModeIconPro by lazy { findViewById<ImageView>(R.id.ivModeIconPro) }

    private val bbFilterButton by lazy { findViewById<ImageButton>(R.id.bbFilterButton) }
    private val bbFilterRibbonContainer by lazy { findViewById<HorizontalScrollView>(R.id.bbFilterRibbonLayout) }
    private val bbFilterItemList by lazy { findViewById<LinearLayout>(R.id.bbFilterItemList) }
    private val bbBottomDeck by lazy { findViewById<ConstraintLayout>(R.id.bbBottomDeckLayout) }

    private val bbProControlsContainer by lazy { findViewById<LinearLayout>(R.id.bbProControlsLayout) }
    private val bbEvDial by lazy { findViewById<LinearLayout>(R.id.bbEvDial) }
    private val tvEvIndicator by lazy { findViewById<TextView>(R.id.tvEvIndicator) }
    private val btnProEv by lazy { findViewById<Button>(R.id.btnProEv) }
    private val btnProIso by lazy { findViewById<Button>(R.id.btnProIso) }
    private val btnProShutter by lazy { findViewById<Button>(R.id.btnProShutter) }
    private val btnProWb by lazy { findViewById<Button>(R.id.btnProWb) }
    private val btnProMf by lazy { findViewById<Button>(R.id.btnProMf) }
    private val btnProAuto by lazy { findViewById<Button>(R.id.btnProAuto) }
    private val bbProSubmenuScroll by lazy { findViewById<HorizontalScrollView>(R.id.bbProSubmenuScroll) }
    private val bbProSubmenuList by lazy { findViewById<LinearLayout>(R.id.bbProSubmenuList) }

    private var isSquareMode: Boolean = false
    private var isHdrEnabled: Boolean = false
    private var is3_2Mode: Boolean = false
    private var currentSelectedRatio: Int = AspectRatio.RATIO_4_3

    data class ResolutionOption(
        val label: String,
        val size: Size,
        val mp: String,
        val captureSize: Size = size
    )

    private fun getAvailableResolutions(ratio: Int, isSquare: Boolean, is3_2: Boolean): List<ResolutionOption> {
        val currentCamera = viewModel.currentCamera
        val allSizes = currentCamera?.supportedPhotoResolutions.orEmpty()

        // Find all native 4:3 sensor resolutions (full sensor area)
        val sizes4_3 = allSizes.filter {
            val w = maxOf(it.width, it.height).toFloat()
            val h = minOf(it.width, it.height).toFloat()
            abs((w / h) - (4f / 3f)) < 0.05f
        }.ifEmpty {
            listOf(Size(4032, 3024), Size(3264, 2448), Size(2560, 1920), Size(1920, 1440))
        }

        return when {
            isSquare -> {
                sizes4_3.take(4).map { capSize ->
                    val dim = minOf(capSize.width, capSize.height)
                    val sqSize = Size(dim, dim)
                    val mp = (dim.toLong() * dim.toLong()).toFloat() / 1_000_000f
                    val label = if (mp >= 10f) String.format(Locale.US, "%.0f MP", mp) else String.format(Locale.US, "%.1f MP", mp)
                    ResolutionOption(label, sqSize, "${dim}×${dim}", capSize)
                }
            }
            ratio == AspectRatio.RATIO_16_9 && !isSquare && !is3_2 -> {
                sizes4_3.take(4).map { capSize ->
                    val w = maxOf(capSize.width, capSize.height)
                    val h = (w * 9) / 16
                    val rSize = Size(w, h)
                    val mp = (w.toLong() * h.toLong()).toFloat() / 1_000_000f
                    val label = if (mp >= 10f) String.format(Locale.US, "%.0f MP", mp) else String.format(Locale.US, "%.1f MP", mp)
                    ResolutionOption(label, rSize, "${w}×${h}", capSize)
                }
            }
            is3_2 -> {
                sizes4_3.take(4).map { capSize ->
                    val w = maxOf(capSize.width, capSize.height)
                    val h = (w * 2) / 3
                    val rSize = Size(w, h)
                    val mp = (w.toLong() * h.toLong()).toFloat() / 1_000_000f
                    val label = if (mp >= 10f) String.format(Locale.US, "%.0f MP", mp) else String.format(Locale.US, "%.1f MP", mp)
                    ResolutionOption(label, rSize, "${w}×${h}", capSize)
                }
            }
            else -> {
                sizes4_3.take(4).map { capSize ->
                    val w = capSize.width
                    val h = capSize.height
                    val mp = (w.toLong() * h.toLong()).toFloat() / 1_000_000f
                    val label = if (mp >= 10f) String.format(Locale.US, "%.0f MP", mp) else String.format(Locale.US, "%.1f MP", mp)
                    ResolutionOption(label, capSize, "${w}×${h}", capSize)
                }
            }
        }
    }

    private var currentTargetResolution: Size? = null
    private var currentCaptureResolution: Size? = null
    private var currentResolutionLabel: String = "12 MP"

    private var currentIso: Int? = null
    private var currentExposureNanos: Long? = null
    private var currentAwbMode: Int? = null
    private var currentFocusDistance: Float? = null

    enum class BbFilter(
        val titleRes: Int,
        val colorMatrix: ColorMatrix?,
        val drawableRes: Int,
    ) {
        NONE(R.string.bb_filter_none, null, R.drawable.lighthouse2_normal),
        SATURATE(R.string.bb_filter_saturate, ColorMatrix().apply { setSaturation(1.6f) }, R.drawable.lighthouse2_saturate),
        FRESHEN(R.string.bb_filter_freshen, ColorMatrix(floatArrayOf(
            1.15f, 0f, 0f, 0f, 15f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 0.85f, 0f, -20f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_freshen),
        FREEZE(R.string.bb_filter_freeze, ColorMatrix(floatArrayOf(
            0.85f, 0f, 0f, 0f, -10f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 1.30f, 0f, 25f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_freeze),
        SEPIA(R.string.bb_filter_sepia, ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_sepia),
        STARK(R.string.bb_filter_stark, ColorMatrix(floatArrayOf(
            1.5f * 0.299f, 1.5f * 0.587f, 1.5f * 0.114f, 0f, -40f,
            1.5f * 0.299f, 1.5f * 0.587f, 1.5f * 0.114f, 0f, -40f,
            1.5f * 0.299f, 1.5f * 0.587f, 1.5f * 0.114f, 0f, -40f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_stark),
        AGAPANTHA(R.string.bb_filter_agapantha, ColorMatrix(floatArrayOf(
            1.1f, 0f, 0.2f, 0f, 10f,
            0f, 0.9f, 0.1f, 0f, -10f,
            0.2f, 0f, 1.3f, 0f, 25f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_agapantha),
        CLIFTON(R.string.bb_filter_clifton, ColorMatrix(floatArrayOf(
            0.9f, 0f, 0f, 0f, 20f,
            0f, 1.05f, 0f, 0f, 15f,
            0f, 0f, 1.15f, 0f, 30f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_clifton),
        BW093(R.string.bb_filter_bw093, ColorMatrix(floatArrayOf(
            0.33f, 0.33f, 0.33f, 0f, 30f,
            0.33f, 0.33f, 0.33f, 0f, 30f,
            0.33f, 0.33f, 0.33f, 0f, 30f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_bw093),
        ILFORD(R.string.bb_filter_ilford, ColorMatrix().apply { setSaturation(0f) }, R.drawable.lighthouse2_ilford),
        PORTRA160(R.string.bb_filter_portra160, ColorMatrix(floatArrayOf(
            1.2f, 0f, 0f, 0f, 10f,
            0f, 1.1f, 0f, 0f, 5f,
            0f, 0f, 0.95f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_portra160),
        ROUGE(R.string.bb_filter_rouge, ColorMatrix(floatArrayOf(
            1.35f, 0f, 0f, 0f, 20f,
            0f, 0.95f, 0f, 0f, -5f,
            0f, 0f, 0.95f, 0f, -5f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_rouge),
        VIGNETTE(R.string.bb_filter_vignette, ColorMatrix(floatArrayOf(
            0.9f, 0f, 0f, 0f, -15f,
            0f, 0.9f, 0f, 0f, -15f,
            0f, 0f, 0.9f, 0f, -15f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_vignette),
        PX70(R.string.bb_filter_px70, ColorMatrix(floatArrayOf(
            1.05f, 0f, 0.1f, 0f, 15f,
            0f, 1.1f, 0f, 0f, 10f,
            0.1f, 0f, 0.85f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_px70),
        NEGATIVE(R.string.bb_filter_negative, ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_negative),
        DESATURATE(R.string.bb_filter_desaturate, ColorMatrix().apply { setSaturation(0.35f) }, R.drawable.lighthouse2_desaturate),
        GILD(R.string.bb_filter_gild, ColorMatrix(floatArrayOf(
            1.25f, 0f, 0f, 0f, 25f,
            0f, 1.15f, 0f, 0f, 15f,
            0f, 0f, 0.8f, 0f, -30f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_gild),
        INFRARED(R.string.bb_filter_infrared, ColorMatrix(floatArrayOf(
            0.2f, 0.7f, 0.1f, 0f, 20f,
            0.9f, 0.1f, 0f, 0f, -20f,
            0.9f, 0.1f, 0f, 0f, 40f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_infrared),
        PROTEA(R.string.bb_filter_protea, ColorMatrix(floatArrayOf(
            1.25f, 0f, 0.15f, 0f, 15f,
            0f, 0.95f, 0.05f, 0f, 0f,
            0.15f, 0f, 1.15f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        )), R.drawable.lighthouse2_protea)
    }

    private var currentBbFilter: BbFilter = BbFilter.NONE

    // System services
    private val keyguardManager by lazy { getSystemService(KeyguardManager::class.java) }

    /**
     * The currently shown URI.
     */
    private var galleryButtonUri: Uri? = null

    /**
     * Medias captured from secure activity will be stored here
     * NOTE: Order is important, the first element is the newest one.
     */
    private val secureMediaUris = ArrayDeque<Uri>()

    // QR
    private val qrBottomSheetDialog by lazy {
        QrBottomSheetDialog(this).apply {
            setOnDismissListener {
                viewModel.onQrDialogDismissed()
            }
        }
    }
    private val liveQrChipContainer by lazy {
        findViewById<View>(R.id.bbLiveQrChipContainer)
    }
    private val cardQrChip by lazy { findViewById<View>(R.id.cardQrChip) }
    private val tvQrChipTitle by lazy { findViewById<TextView>(R.id.tvQrChipTitle) }
    private val tvQrChipSubtitle by lazy { findViewById<TextView>(R.id.tvQrChipSubtitle) }
    private val ivQrChipIcon by lazy { findViewById<ImageView>(R.id.ivQrChipIcon) }
    private val ivQrChipClose by lazy { findViewById<View>(R.id.ivQrChipClose) }
    private var currentLiveQrResult: QrResult? = null

    private fun updateLiveQrChip(qrResult: QrResult?) {
        val chipView = liveQrChipContainer ?: run {
            Log.w(LOG_TAG, "updateLiveQrChip: liveQrChipContainer is null!")
            return
        }
        currentLiveQrResult = qrResult
        Log.d(LOG_TAG, "updateLiveQrChip: text=${qrResult?.text}, isVisible=${chipView.isVisible}")

        if (qrResult == null) {
            if (chipView.isVisible) {
                chipView.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .setDuration(200)
                    .withEndAction { chipView.isVisible = false }
                    .start()
            }
            return
        }

        // Don't show QR chip if mode grid is currently open or mode is video
        if (bbModeGridContainer.isVisible || viewModel.cameraMode.value == CameraMode.VIDEO) {
            chipView.isVisible = false
            return
        }

        val firstAction = qrResult.actions.firstOrNull()
        val rawText = qrResult.text.orEmpty().trim()

        val isUrl = rawText.startsWith("http://", ignoreCase = true) || rawText.startsWith("https://", ignoreCase = true)
        if (isUrl) {
            val displayUrl = try {
                val uri = Uri.parse(rawText)
                val host = uri.host
                if (!host.isNullOrEmpty()) {
                    val path = uri.path
                    if (!path.isNullOrEmpty() && path != "/") {
                        "$host${path.take(20)}"
                    } else {
                        host
                    }
                } else {
                    rawText
                }
            } catch (_: Exception) {
                rawText
            }
            tvQrChipTitle.text = displayUrl
            tvQrChipSubtitle.text = firstAction?.title ?: getString(R.string.qr_uri_title)
            tvQrChipSubtitle.isVisible = true
        } else {
            tvQrChipTitle.text = firstAction?.title ?: rawText
            if (!firstAction?.title.isNullOrEmpty() && rawText != firstAction?.title) {
                tvQrChipSubtitle.text = rawText
                tvQrChipSubtitle.isVisible = true
            } else {
                tvQrChipSubtitle.isVisible = false
            }
        }

        val iconDrawable = firstAction?.icon?.loadDrawable(this)
        if (iconDrawable != null) {
            ivQrChipIcon.setImageDrawable(iconDrawable)
        } else {
            ivQrChipIcon.setImageResource(R.drawable.ic_shortcut_qr)
        }

        cardQrChip.setOnClickListener {
            if (firstAction?.pendingIntent != null) {
                try {
                    firstAction.pendingIntent.sendWithBalAllowed()
                } catch (_: PendingIntent.CanceledException) {
                    qrBottomSheetDialog.setQrResult(qrResult)
                }
            } else {
                qrBottomSheetDialog.setQrResult(qrResult)
            }
        }

        cardQrChip.setOnLongClickListener {
            qrBottomSheetDialog.setQrResult(qrResult)
            true
        }

        ivQrChipClose.setOnClickListener {
            viewModel.onQrDialogDismissed()
            chipView.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(150)
                .withEndAction { chipView.isVisible = false }
                .start()
        }

        if (!chipView.isVisible) {
            chipView.alpha = 0f
            chipView.translationY = 20f
            chipView.isVisible = true
            chipView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .start()
        }
    }

    private val isGoogleLensAvailable by lazy { GoogleLensUtils.isGoogleLensAvailable(this) }
    private var hasBoundCameraOnce = false

    private var viewFinderTouchEvent: MotionEvent? = null
    private val gestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                viewFinderTouchEvent = e
                return false
            }

            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                if (zoomGestureDetectorIsInProgress) {
                    return false
                }
                // Vertical scroll to zoom: drag UP zooms in, drag DOWN zooms out
                if (abs(distanceY) > abs(distanceX) * 1.1f && abs(distanceY) > 2f) {
                    val currentLinear = viewModel.zoomState.value?.linearZoom ?: 0f
                    val viewFinderH = viewFinder.height.toFloat().coerceAtLeast(300f)
                    val delta = distanceY / (viewFinderH * 0.7f)
                    val newLinear = (currentLinear + delta).coerceIn(0f, 1f)
                    viewModel.cameraController.setLinearZoom(newLinear)
                    zoomLevel.progress = newLinear
                    zoomLevel.isVisible = true
                    handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
                    handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
                    return true
                }
                return false
            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                return e1?.let {
                    if (!handler.hasMessages(MSG_ON_PINCH_TO_ZOOM) &&
                        abs(it.x - e2.x) > 75 * resources.displayMetrics.density
                    ) {
                        if (e2.x > it.x) {
                            // Left to right
                            viewModel.previousCameraMode()
                        } else {
                            // Right to left
                            viewModel.nextCameraMode()
                        }
                    }
                    true
                } ?: false
            }
        })
    }
    private val zoomGestureDetector by lazy {
        ZoomGestureDetector(this) {
            when (it) {
                is ZoomGestureDetector.ZoomEvent.Begin -> {
                    zoomGestureDetectorIsInProgress = true
                }

                is ZoomGestureDetector.ZoomEvent.Move -> {
                    viewModel.cameraController.onPinchToZoom(it.incrementalScaleFactor)
                    zoomLevel.isVisible = true
                    handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
                    handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
                    handler.removeMessages(MSG_ON_PINCH_TO_ZOOM)
                    handler.sendMessageDelayed(handler.obtainMessage(MSG_ON_PINCH_TO_ZOOM), 500)
                }

                is ZoomGestureDetector.ZoomEvent.End -> {
                    zoomGestureDetectorIsInProgress = false
                }
            }
            true
        }
    }
    private var zoomGestureDetectorIsInProgress = false

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_HIDE_ZOOM_SLIDER -> {
                    zoomLevel.isVisible = false
                }

                MSG_HIDE_FOCUS_RING -> {
                    viewFinderFocus.isVisible = false
                }

                MSG_HIDE_EXPOSURE_SLIDER -> {
                    exposureLevel.isVisible = false
                }
            }
        }
    }

    // Permissions
    private val permissionsManager = PermissionsManager(this)

    private val locationPermissionsDialog by lazy {
        LocationPermissionsDialog(this).also {
            it.onResultCallback = { result ->
                if (result) {
                    lifecycleScope.launch {
                        val permissionState = permissionsManager.requestPermission(
                            Permission.LOCATION
                        )
                        viewModel.setSaveLocation(permissionState == PermissionState.GRANTED)
                    }
                } else {
                    viewModel.setSaveLocation(false)
                }
            }
        }
    }

    private val forceTorchSnackbar by lazy {
        Snackbar.make(
            secondaryBottomBarLayout, R.string.force_torch_help, Snackbar.LENGTH_INDEFINITE
        )
            .setAnchorView(secondaryBottomBarLayout)
            .setAction(android.R.string.ok) {
                viewModel.setForceModeHelpShown(true)
            }
    }

    enum class ShutterAnimation(val resourceId: Int) {
        InitPhoto(R.drawable.avd_photo_capture),
        InitVideo(R.drawable.avd_mode_video_photo),

        PhotoCapture(R.drawable.avd_photo_capture),
        PhotoToVideo(R.drawable.avd_mode_photo_video),

        VideoToPhoto(R.drawable.avd_mode_video_photo),
        VideoStart(R.drawable.avd_video_start),
        VideoEnd(R.drawable.avd_video_end),
    }

    enum class VideoRecordingStateAnimation(val resourceId: Int) {
        Init(R.drawable.avd_video_recording_pause),
        ResumeToPause(R.drawable.avd_video_recording_pause),
        PauseToResume(R.drawable.avd_video_recording_resume),
    }

    private val intentActions = mapOf(
        // Intents
        MediaStore.ACTION_IMAGE_CAPTURE to {
            viewModel.initialCameraMode = CameraMode.PHOTO
            viewModel.inSingleCaptureMode.value = true
        },
        MediaStore.ACTION_IMAGE_CAPTURE_SECURE to {
            viewModel.initialCameraMode = CameraMode.PHOTO
            viewModel.inSingleCaptureMode.value = true
        },
        MediaStore.ACTION_VIDEO_CAPTURE to {
            viewModel.initialCameraMode = CameraMode.VIDEO
            viewModel.inSingleCaptureMode.value = true
        },
        MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA to {
            viewModel.initialCameraMode = CameraMode.PHOTO
        },
        MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE to {
            viewModel.initialCameraMode = CameraMode.PHOTO
        },
        MediaStore.INTENT_ACTION_VIDEO_CAMERA to {
            viewModel.initialCameraMode = CameraMode.VIDEO
        },

        // Shortcuts
        ShortcutsUtils.SHORTCUT_ID_SELFIE to {
            viewModel.initialCameraMode = CameraMode.PHOTO
            viewModel.initialCameraFacing = CameraFacing.FRONT
        },
        ShortcutsUtils.SHORTCUT_ID_VIDEO to {
            viewModel.initialCameraMode = CameraMode.VIDEO
            viewModel.initialCameraFacing = CameraFacing.BACK
        },
        ShortcutsUtils.SHORTCUT_ID_QR to {
            viewModel.initialCameraMode = CameraMode.QR
        },
    )
    private val assistantIntent
        get() = AssistantIntent.fromIntent(intent)
    private val launchedViaVoiceIntent
        get() = isVoiceInteractionRoot && intent.hasCategory(Intent.CATEGORY_VOICE)

    @Suppress("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        // Hide status bars for true fullscreen
        hideSystemBars()

        // Force dark navigation bar
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
            && keyguardManager.isKeyguardLocked
        ) {
            setShowWhenLocked(true)

            @Suppress("SourceLockedOrientationActivity")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // Register shortcuts
        lifecycleScope.launch(Dispatchers.IO) {
            ShortcutsUtils.registerShortcuts(this@CameraActivity)
        }

        // Initialize initial camera mode
        overrideInitialCameraMode()?.let {
            viewModel.initialCameraMode = it
        }

        // Handle intent
        intent.action?.let {
            intentActions[it]?.invoke()
        }

        // Handle assistant intent
        assistantIntent?.useFrontCamera?.let {
            viewModel.initialCameraFacing = if (it) {
                CameraFacing.FRONT
            } else {
                CameraFacing.BACK
            }
        }

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            findViewById<LinearLayout>(R.id.bbTopBar).setPadding(
                8.px,
                4.px,
                8.px,
                4.px
            )

            bbBottomDeck.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
                leftMargin = insets.left
                rightMargin = insets.right
            }

            cameraModeSelectorLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
                leftMargin = insets.left
                rightMargin = insets.right
            }

            capturePreviewLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
                leftMargin = insets.left
                rightMargin = insets.right
            }

            windowInsets
        }

        updateViewFinderFraming()

        // Set secondary top bar button callbacks
        aspectRatioButton.setOnClickListener { viewModel.cyclePhotoAspectRatio() }
        videoQualityButton.setOnClickListener { viewModel.cycleVideoQuality() }
        videoFrameRateButton.setOnClickListener { viewModel.cycleVideoFrameRate() }
        videoDynamicRangeButton.setOnClickListener { viewModel.cycleVideoDynamicRange() }
        effectButton.setOnClickListener { viewModel.cycleExtensionMode() }
        gridButton.setOnClickListener { viewModel.cycleGridMode() }
        timerButton.setOnClickListener { viewModel.toggleTimerMode() }
        micButton.setOnClickListener { viewModel.toggleVideoMicrophoneEnabled() }
        settingsButton.setOnClickListener { openSettings() }

        // Set secondary bottom bar button callbacks
        proButton.setOnClickListener {
            secondaryTopBarLayout.slide()
        }
        googleLensButton.setOnClickListener {
            dismissKeyguardAndRun {
                GoogleLensUtils.launchGoogleLens(this)
            }
        }
        flashButton.setOnClickListener { viewModel.cycleFlashMode(false) }
        flashButton.setOnLongClickListener { viewModel.cycleFlashMode(true) }

        // Observe manual focus
        viewFinder.setOnTouchListener { _, event ->
            if (zoomGestureDetector.onTouchEvent(event) && zoomGestureDetectorIsInProgress) {
                return@setOnTouchListener true
            }
            return@setOnTouchListener gestureDetector.onTouchEvent(event)
        }
        viewFinder.setOnClickListener {
            // Reset exposure level to 0 EV
            viewModel.setExposureCompensationLevel(0.5f)

            exposureLevel.isVisible = true
            handler.removeMessages(MSG_HIDE_EXPOSURE_SLIDER)
            handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_EXPOSURE_SLIDER), 2000)

            secondaryTopBarLayout.slideDown()
        }

        // Observe preview stream state
        viewFinder.previewStreamState.observe(this) {
            when (it) {
                PreviewView.StreamState.STREAMING -> {
                    // Show grid
                    gridView.alpha = 1f
                    gridView.previewView = viewFinder

                    // Hide preview blur
                    previewBlurView.isVisible = false

                    // Ensure flash/torch state is applied now that capture session is active
                    viewModel.cameraController.flashMode = viewModel.flashMode.value

                    // Issue capture if requested via assistant
                    if ((launchedViaVoiceIntent || assistantIntent?.cameraOpenOnly != null)
                        && assistantIntent?.cameraOpenOnly != true
                    ) {
                        shutterButton.performClick()
                    }
                }

                else -> {}
            }
        }

        zoomLevel.topLabel = "T"
        zoomLevel.bottomLabel = "W"
        zoomLevel.showTickers = true
        zoomLevel.onProgressChangedByUser = {
            viewModel.cameraController.setLinearZoom(it)
            handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
            handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
        }
        zoomLevel.textFormatter = {
            val ratio = viewModel.zoomState.value?.zoomRatio ?: 1f
            "%.1f×".format(ratio)
        }

        // Set expose level callback & text formatter
        exposureLevel.topLabel = "+"
        exposureLevel.bottomLabel = "−"
        exposureLevel.showTickers = true
        exposureLevel.onProgressChangedByUser = {
            viewModel.setExposureCompensationLevel(it)

            handler.removeMessages(MSG_HIDE_EXPOSURE_SLIDER)
            handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_EXPOSURE_SLIDER), 3000)
        }

        // Set primary bar button callbacks
        flipCameraButton.setOnClickListener { viewModel.flipCamera() }

        videoRecordingStateButton.setOnClickListener {
            viewModel.onVideoRecordingStateButtonPress()
        }

        shutterButton.setOnClickListener {
            // Shutter animation
            when (viewModel.cameraMode.value) {
                CameraMode.PHOTO, CameraMode.QR -> startShutterAnimation(ShutterAnimation.PhotoCapture)
                CameraMode.VIDEO -> {
                    if (countDownView.cancelCountDown()) {
                        viewModel.cameraState.value = CameraState.IDLE
                        startShutterAnimation(ShutterAnimation.VideoEnd)
                        return@setOnClickListener
                    }
                    if (viewModel.cameraState.value == CameraState.IDLE) {
                        startShutterAnimation(ShutterAnimation.VideoStart)
                    }
                }
            }

            startTimerAndRun {
                when (viewModel.cameraMode.value) {
                    CameraMode.PHOTO, CameraMode.QR -> viewModel.takePhoto()
                    CameraMode.VIDEO -> viewModel.captureVideo()
                }
            }
        }

        galleryButtonCardView.setOnClickListener { openGallery() }

        // Set lens switching callback
        lensSelectorLayout.onCameraChangeCallback = {
            viewModel.selectCamera(it)
        }
        lensSelectorLayout.onZoomRatioChangeCallback = {
            viewModel.smoothZoom(it)
            zoomLevel.isVisible = true
            handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
            handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
        }
        lensSelectorLayout.onResetZoomRatioCallback = {
            viewModel.resetZoom()
            zoomLevel.isVisible = true
            handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
            handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
        }
        lensSelectorLayout.setOnClickListener {
            zoomLevel.isVisible = !zoomLevel.isVisible
            if (zoomLevel.isVisible) {
                handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
                handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
            }
        }

        // Set capture preview callback
        capturePreviewLayout.onChoiceCallback = { input ->
            when (input) {
                null -> {
                    capturePreviewLayout.isVisible = false
                }

                is InputStream,
                is Uri -> sendIntentResultAndExit(input)

                else -> throw Exception("Invalid input")
            }
        }

        // Set mode selector callback
        cameraModeSelectorLayout.onModeSelectedCallback = {
            viewModel.setCameraMode(it)
        }

        // Bind viewfinder and preview blur view
        previewBlurView.previewView = viewFinder

        // Initialize BlackBerry Camera UI
        setupBbUi()

        // Check for updates
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2500)
            val update = org.lineageos.aperture.utils.UpdateChecker.checkUpdate(BuildConfig.VERSION_NAME)
            if (update != null && !isFinishing && !isDestroyed) {
                org.lineageos.aperture.utils.UpdateChecker.showUpdateDialog(this@CameraActivity, update)
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsManager.withPermissionGranted(Permission.CAMERA) {
                    loadData()
                }
            }
        }

        // Also collect location
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsManager.withPermissionGranted(Permission.LOCATION) {
                    viewModel.location.collect()
                }
            }
        }

        // Check for permissions
        lifecycleScope.launch {
            permissionsManager.permissionStateFlow(
                Permission.CAMERA
            ).collectLatest { permissionState ->
                if (permissionState != PermissionState.GRANTED) {
                    when (permissionsManager.requestPermission(Permission.CAMERA)) {
                        PermissionState.GRANTED -> {
                            // This is a good time to ask the user for location permissions
                            if (viewModel.saveLocation.value == null) {
                                locationPermissionsDialog.show()
                            }
                        }

                        else -> {
                            Toast.makeText(
                                this@CameraActivity,
                                R.string.app_permissions_toast,
                                Toast.LENGTH_LONG,
                            ).show()
                            finish()
                        }
                    }
                }
            }
        }

        // Initialize stuff after camera permissions are granted
        lifecycleScope.launch {
            queueSetupWithCameraPermissions()
        }
    }

    override fun onDestroy() {
        // Detach CameraController from ScreenFlashView
        screenFlashView.setController(null)

        // Detach CameraController from PreviewView
        viewFinder.controller = null

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?) = when (capturePreviewLayout.isVisible) {
        true -> super.onKeyDown(keyCode, event)
        false -> handleHardwareKeyDown(keyCode, event) ?: super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?) = when (capturePreviewLayout.isVisible) {
        true -> super.onKeyUp(keyCode, event)
        false -> handleHardwareKeyUp(keyCode, event) ?: super.onKeyUp(keyCode, event)
    }

    /**
     * This is a method that can be overridden to set the initial camera mode and facing.
     * It's gonna have priority over shared preferences and intents.
     */
    protected open fun overrideInitialCameraMode(): CameraMode? = null

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.event.collect { event ->
                when (event) {
                    is Event.NoCamera -> noCamera()

                    is Event.ShowForceTorchHelp -> if (!forceTorchSnackbar.isShownOrQueued) {
                        forceTorchSnackbar.show()
                    }

                    is Event.FlipCameraAnimation -> {
                        (flipCameraButton.drawable as? AnimatedVectorDrawable)?.start() ?: run {
                            flipCameraButton.animate().rotationBy(180f).setDuration(300).start()
                        }
                    }

                    is Event.PhotoCaptureStatus -> when (event) {
                        is Event.PhotoCaptureStatus.CaptureStarted -> {
                            viewFinder.foreground = ColorDrawable(Color.BLACK)
                            ValueAnimator.ofInt(0, 255, 0).apply {
                                addUpdateListener { anim ->
                                    viewFinder.foreground.alpha = anim.animatedValue as Int
                                }
                            }.start()
                        }

                        is Event.PhotoCaptureStatus.ImageSaved -> {
                            val savedUri = event.output.savedUri
                            if (savedUri != null && (currentBbFilter != BbFilter.NONE || isSquareMode || is3_2Mode || currentSelectedRatio == AspectRatio.RATIO_16_9 || currentTargetResolution != null)) {
                                processSavedPhoto(savedUri)
                            }
                            if (!viewModel.inSingleCaptureMode.value) {
                                onCapturedMedia(savedUri)
                            } else {
                                savedUri?.let {
                                    openCapturePreview(it, MediaType.PHOTO)
                                }
                                event.photoOutputStream?.use {
                                    openCapturePreview(
                                        ByteArrayInputStream(
                                            event.photoOutputStream.toByteArray()
                                        )
                                    )
                                }
                            }
                        }

                        is Event.PhotoCaptureStatus.Error -> {
                            // Do nothing
                        }
                    }
                }
            }
        }

        launch {
            viewModel.cameraConfiguration.collectLatest { cameraConfiguration ->
                bindCameraUseCases(cameraConfiguration)
            }
        }

        launch {
            viewModel.cameraMode.collectLatest { cameraMode ->
                // Hide secondary top bar
                secondaryTopBarLayout.isVisible = false

                // Update secondary top bar buttons
                aspectRatioButton.isVisible = cameraMode != CameraMode.VIDEO
                videoQualityButton.isVisible = cameraMode == CameraMode.VIDEO
                videoFrameRateButton.isVisible = cameraMode == CameraMode.VIDEO
                videoDynamicRangeButton.isVisible = cameraMode == CameraMode.VIDEO
                micButton.isVisible = cameraMode == CameraMode.VIDEO

                // Update secondary bottom bar buttons
                proButton.isVisible = cameraMode != CameraMode.QR
                googleLensButton.isVisible = cameraMode == CameraMode.QR && isGoogleLensAvailable

                // Update primary bar buttons (keep shutter visible in all modes including QR scanner)
                shutterButton.isInvisible = false

                // Update camera mode selector
                cameraModeSelectorLayout.setCurrentCameraMode(cameraMode)

                // Update BlackBerry UI
                updateModeGridHighlight()
                btnBbAspectRatio.isVisible = true
                btnBbHdr.isVisible = cameraMode == CameraMode.PHOTO
                updateLiveQrChip(null)
            }
        }

        launch {
            viewModel.cameraFacing.collectLatest { cameraFacing ->
                flipCameraButton.setImageResource(
                    if (cameraFacing == CameraFacing.FRONT) R.drawable.ic_camera_front_bb_white_24dp
                    else R.drawable.ic_camera_rear_bb_white_24dp
                )
            }
        }

        launch {
            viewModel.cameraModeTransition.collectLatest { (oldCameraMode, newCameraMode) ->
                when (oldCameraMode) {
                    CameraMode.PHOTO -> when (newCameraMode) {
                        CameraMode.VIDEO -> startShutterAnimation(ShutterAnimation.PhotoToVideo)
                        else -> startShutterAnimation(ShutterAnimation.InitPhoto)
                    }

                    CameraMode.VIDEO -> when (newCameraMode) {
                        CameraMode.PHOTO -> startShutterAnimation(ShutterAnimation.VideoToPhoto)
                        else -> startShutterAnimation(ShutterAnimation.InitVideo)
                    }

                    CameraMode.QR, null -> when (newCameraMode) {
                        CameraMode.PHOTO -> startShutterAnimation(ShutterAnimation.InitPhoto)

                        CameraMode.VIDEO -> startShutterAnimation(ShutterAnimation.InitVideo)

                        CameraMode.QR -> {}
                    }
                }
            }
        }

        launch {
            viewModel.lenses.collectLatest { (activeCamera, availableCameras) ->
                // Update lens selector
                activeCamera?.let {
                    lensSelectorLayout.setCamera(
                        it,
                        availableCameras,
                    )
                    updateOverlaysVisibility()
                }
            }
        }

        launch {
            viewModel.cameraXCameraState.collect { cameraXCameraState ->
                cameraXCameraState.error?.let {
                    // Log the error
                    Log.e(
                        LOG_TAG,
                        "Error: code: ${it.code}, type: ${it.type}",
                        it.cause
                    )

                    val showToast = { stringId: Int ->
                        Toast.makeText(
                            this@CameraActivity,
                            stringId,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    when (it.code) {
                        CameraXCameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                            // No way to fix it without user action, bail out
                            showToast(R.string.error_max_cameras_in_use)
                            finish()
                        }

                        CameraXCameraState.ERROR_CAMERA_IN_USE -> {
                            // No way to fix it without user action, bail out
                            showToast(R.string.error_camera_in_use)
                            finish()
                        }

                        CameraXCameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                            // Warn the user and don't do anything
                            showToast(R.string.error_other_recoverable_error)
                        }

                        CameraXCameraState.ERROR_STREAM_CONFIG -> {
                            // CameraX use case misconfiguration, no way to recover
                            showToast(R.string.error_stream_config)
                            finish()
                        }

                        CameraXCameraState.ERROR_CAMERA_DISABLED -> {
                            // No way to fix it without user action, bail out
                            showToast(R.string.error_camera_disabled)
                            finish()
                        }

                        CameraXCameraState.ERROR_CAMERA_FATAL_ERROR -> {
                            // No way to fix it without user action, bail out
                            showToast(R.string.error_camera_fatal_error)
                            finish()
                        }

                        CameraXCameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                            // No way to fix it without user action, bail out
                            showToast(R.string.error_do_not_disturb_mode_enabled)
                            finish()
                        }

                        else -> {
                            // We know anything about it, just check if it's recoverable or
                            // critical
                            when (it.type) {
                                CameraXCameraState.ErrorType.RECOVERABLE -> {
                                    showToast(R.string.error_unknown_recoverable)
                                }

                                CameraXCameraState.ErrorType.CRITICAL -> {
                                    showToast(R.string.error_unknown_critical)
                                    finish()
                                }

                                null -> {
                                    // Do nothing
                                }
                            }
                        }
                    }
                }
            }
        }

        launch {
            viewModel.inSingleCaptureMode.collectLatest { inSingleCaptureMode ->
                // Update primary bar buttons
                galleryButtonCardView.isInvisible = inSingleCaptureMode

                // Update camera mode selector
                cameraModeSelectorLayout.setInSingleCaptureMode(inSingleCaptureMode)
            }
        }

        launch {
            viewModel.cameraState.collectLatest { cameraState ->
                // Update secondary bar buttons
                timerButton.isEnabled = cameraState == CameraState.IDLE
                aspectRatioButton.isEnabled = cameraState == CameraState.IDLE
                effectButton.isEnabled = cameraState == CameraState.IDLE
                settingsButton.isEnabled = cameraState == CameraState.IDLE

                lensSelectorLayout.setCameraState(cameraState)
                updateOverlaysVisibility()

                // Update primary bar buttons
                galleryButtonCardView.isEnabled = cameraState == CameraState.IDLE
                // Shutter button must stay enabled
                flipCameraButton.isEnabled = cameraState == CameraState.IDLE
                videoRecordingStateButton.isVisible = cameraState.isRecordingVideo

                // Update camera mode selector
                cameraModeSelectorLayout.setCameraState(cameraState)
            }
        }

        launch {
            viewModel.screenRotation.collectLatest { screenRotation ->
                val compensationValue = screenRotation.compensationValue.toFloat()

                // Rotate sliders
                exposureLevel.screenRotation = screenRotation
                zoomLevel.screenRotation = screenRotation

                // Rotate info chip
                islandView.setScreenRotation(screenRotation)

                // Rotate secondary top bar buttons
                ConstraintLayout::class.safeCast(
                    secondaryTopBarLayout.getChildAt(0)
                )?.let { layout ->
                    for (child in layout.children) {
                        Button::class.safeCast(child)?.let {
                            it.smoothRotate(compensationValue)
                            ValueAnimator.ofFloat(
                                (it.layoutParams as ConstraintLayout.LayoutParams).verticalBias,
                                when (screenRotation) {
                                    Rotation.ROTATION_0 -> 0.0f
                                    Rotation.ROTATION_180 -> 1.0f
                                    Rotation.ROTATION_90,
                                    Rotation.ROTATION_270 -> 0.5f
                                }
                            ).apply {
                                addUpdateListener { anim ->
                                    it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                        verticalBias = anim.animatedValue as Float
                                    }
                                }
                            }.start()
                        }
                    }
                }

                // Rotate secondary bottom bar buttons
                proButton.smoothRotate(compensationValue)
                lensSelectorLayout.setScreenRotation(screenRotation)
                flashButton.smoothRotate(compensationValue)

                // Rotate primary bar buttons
                galleryButtonCardView.smoothRotate(compensationValue)
                shutterButton.smoothRotate(compensationValue)
                flipCameraButton.smoothRotate(compensationValue)

                // Rotate capture preview
                capturePreviewLayout.setScreenRotation(screenRotation)

                // Rotate count down view
                countDownView.setScreenRotation(screenRotation)
            }
        }

        launch {
            viewModel.capturedMedia.collectLatest { capturedMedia ->
                updateGalleryButton(capturedMedia.firstOrNull(), false)
            }
        }

        launch {
            viewModel.supportedFlashModes.collectLatest { supportedFlashModes ->
                // Hide the button if the only available mode is off,
                // we want the user to know if any other mode is being used
                flashButton.isVisible = supportedFlashModes.size != 1
                        || supportedFlashModes.first() != FlashMode.OFF
            }
        }

        launch {
            viewModel.flashMode.collectLatest { flashMode ->
                // Update secondary bar buttons
                flashButton.setImageResource(
                    when (flashMode) {
                        FlashMode.OFF -> R.drawable.ic_flash_off
                        FlashMode.AUTO -> R.drawable.ic_flash_auto
                        FlashMode.ON -> R.drawable.ic_flash_on
                        FlashMode.TORCH -> R.drawable.ic_flashlight_on
                        FlashMode.SCREEN -> R.drawable.ic_flash_screen
                    }
                )

                // Update BlackBerry Top Bar Flash button
                btnBbFlash.setImageResource(
                    when (flashMode) {
                        FlashMode.OFF -> R.drawable.ic_flash_off_white_24dp
                        FlashMode.AUTO -> R.drawable.ic_flash_auto_white_24dp
                        FlashMode.ON, FlashMode.TORCH, FlashMode.SCREEN -> R.drawable.ic_flash_on_white_24dp
                    }
                )
            }
        }

        launch {
            viewModel.isFlashButtonEnabled.collectLatest { isFlashButtonEnabled ->
                flashButton.isEnabled = isFlashButtonEnabled
            }
        }

        launch {
            viewModel.gridMode.collectLatest { gridMode ->
                gridView.mode = gridMode

                btnBbGrid.setImageResource(
                    when (gridMode) {
                        GridMode.OFF -> R.drawable.ic_grid_3x3_off
                        GridMode.ON_3 -> R.drawable.ic_grid_3x3
                        GridMode.ON_4 -> R.drawable.ic_grid_4x4
                        GridMode.ON_GOLDEN_RATIO -> R.drawable.ic_grid_goldenratio
                    }
                )

                // Update secondary bar buttons
                gridButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    when (gridMode) {
                        GridMode.OFF -> R.drawable.ic_grid_3x3_off
                        GridMode.ON_3 -> R.drawable.ic_grid_3x3
                        GridMode.ON_4 -> R.drawable.ic_grid_4x4
                        GridMode.ON_GOLDEN_RATIO -> R.drawable.ic_grid_goldenratio
                    },
                    0,
                    0
                )
                gridButton.setText(
                    when (gridMode) {
                        GridMode.OFF -> R.string.grid_off
                        GridMode.ON_3 -> R.string.grid_on_3
                        GridMode.ON_4 -> R.string.grid_on_4
                        GridMode.ON_GOLDEN_RATIO -> R.string.grid_on_goldenratio
                    }
                )
            }
        }

        launch {
            viewModel.timerMode.collectLatest { timerMode ->
                // Update secondary bar buttons
                timerButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    when (timerMode) {
                        TimerMode.OFF -> R.drawable.ic_timer_off
                        TimerMode.ON_3S -> R.drawable.ic_timer_3_alt_1
                        TimerMode.ON_10S -> R.drawable.ic_timer_10_alt_1
                    },
                    0,
                    0
                )
                timerButton.setText(
                    when (timerMode) {
                        TimerMode.OFF -> R.string.timer_off
                        TimerMode.ON_3S -> R.string.timer_3
                        TimerMode.ON_10S -> R.string.timer_10
                    }
                )

                // Update BlackBerry Top Bar Timer button
                btnBbTimer.setImageResource(
                    when (timerMode) {
                        TimerMode.OFF -> R.drawable.ic_timer_off_white_24dp
                        TimerMode.ON_3S -> R.drawable.ic_timer_3_white_24dp
                        TimerMode.ON_10S -> R.drawable.ic_timer_10_white_24dp
                    }
                )
            }
        }

        launch {
            viewModel.levelerEnabled.collectLatest { levelerEnabled ->
                levelerView.isVisible = levelerEnabled
            }
        }

        launch {
            viewModel.fullScreenBrightness.collectLatest { fullScreenBrightness ->
                window.attributes = window.attributes.apply {
                    screenBrightness = when (fullScreenBrightness) {
                        true -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                        false -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    }
                }
            }
        }

        launch {
            viewModel.thermalStatus.collectLatest { thermalStatus ->
                val showSnackBar = { stringId: Int ->
                    Snackbar.make(
                        secondaryBottomBarLayout,
                        stringId,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAnchorView(secondaryBottomBarLayout)
                        .setAction(android.R.string.ok) {
                            // Do nothing
                        }
                        .show()
                }

                when (thermalStatus) {
                    ThermalStatus.NONE,
                    ThermalStatus.LIGHT -> Unit

                    ThermalStatus.MODERATE -> {
                        showSnackBar(R.string.thermal_status_moderate)
                    }

                    ThermalStatus.SEVERE -> {
                        showSnackBar(R.string.thermal_status_severe)
                    }

                    ThermalStatus.CRITICAL -> {
                        showSnackBar(R.string.thermal_status_critical)
                    }

                    ThermalStatus.EMERGENCY -> {
                        showSnackBar(R.string.thermal_status_emergency)
                        finish()
                    }

                    ThermalStatus.SHUTDOWN -> {
                        showSnackBar(R.string.thermal_status_shutdown)
                        finish()
                    }
                }
            }
        }

        launch {
            viewModel.zoomState.collectLatest { zoomState ->
                zoomState?.takeIf { it.minZoomRatio != it.maxZoomRatio }?.let {
                    zoomLevel.progress = it.linearZoom
                    if (zoomLevel.isVisible) {
                        handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
                        handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
                    }
                    lensSelectorLayout.onZoomRatioChanged(it.zoomRatio)
                }
            }
        }

        launch {
            viewModel.tapToFocusInfoState.collectLatest { tapToFocusInfoState ->
                when (tapToFocusInfoState.focusState) {
                    CameraController.TAP_TO_FOCUS_STARTED -> {
                        viewFinderFocus.x =
                            tapToFocusInfoState.tapPoint!!.x - (viewFinderFocus.width / 2)
                        viewFinderFocus.y =
                            tapToFocusInfoState.tapPoint!!.y - (viewFinderFocus.height / 2)
                        viewFinderFocus.isVisible = true
                        handler.removeMessages(MSG_HIDE_FOCUS_RING)
                        ValueAnimator.ofInt(0.px, 8.px).apply {
                            addUpdateListener { anim ->
                                viewFinderFocus.setPadding(anim.animatedValue as Int)
                            }
                        }.start()
                    }

                    else -> {
                        handler.removeMessages(MSG_HIDE_FOCUS_RING)
                        ValueAnimator.ofInt(8.px, 0.px).apply {
                            addUpdateListener { anim ->
                                viewFinderFocus.setPadding(anim.animatedValue as Int)
                            }
                        }.start()

                        handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_FOCUS_RING), 500)
                    }
                }
            }
        }

        launch {
            viewModel.exposureCompensationRangeToLevel.collectLatest { (range, level) ->
                exposureLevel.steps = range.endInclusive - range.start
                exposureLevel.progress = level
                exposureLevel.textFormatter = {
                    val ev = Int.mapToRange(range, it)
                    when (ev == 0) {
                        true -> "0"
                        false -> EXPOSURE_LEVEL_FORMATTER.format(ev).toString()
                    }
                }
            }
        }

        launch {
            viewModel.isShutterButtonEnabled.collectLatest { isShutterButtonEnabled ->
                shutterButton.isEnabled = isShutterButtonEnabled
            }
        }

        launch {
            viewModel.photoAspectRatio.collectLatest { photoAspectRatio ->
                // Update secondary bar buttons
                aspectRatioButton.setText(
                    when (photoAspectRatio) {
                        AspectRatio.RATIO_4_3 -> R.string.aspect_ratio_4_3
                        AspectRatio.RATIO_16_9 -> R.string.aspect_ratio_16_9
                        else -> throw Exception("Unknown aspect ratio $photoAspectRatio")
                    }
                )

                if (!isSquareMode && !is3_2Mode) {
                    currentSelectedRatio = photoAspectRatio
                    updateViewFinderFraming()
                }

                // Update BlackBerry Top Bar Aspect Ratio button
                btnBbAspectRatio.setImageResource(
                    when {
                        isSquareMode -> R.drawable.ic_ratio_square_white_24dp
                        is3_2Mode -> R.drawable.ic_ratio_3_2_white_24dp
                        photoAspectRatio == AspectRatio.RATIO_16_9 -> R.drawable.ic_ratio_16_9_white_24dp
                        else -> R.drawable.ic_ratio_4_3_white_24dp
                    }
                )
            }
        }

        launch {
            viewModel.photoEffect.collectLatest { photoEffect ->
                // Update secondary bar buttons
                effectButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    when (photoEffect) {
                        ExtensionMode.NONE -> R.drawable.ic_blur_off
                        ExtensionMode.BOKEH -> R.drawable.ic_effect_bokeh
                        ExtensionMode.HDR -> R.drawable.ic_hdr_on
                        ExtensionMode.NIGHT -> R.drawable.ic_clear_night
                        ExtensionMode.FACE_RETOUCH -> R.drawable.ic_face_retouching_natural
                        ExtensionMode.AUTO -> R.drawable.ic_hdr_auto
                        else -> R.drawable.ic_blur_off
                    },
                    0,
                    0
                )
                effectButton.setText(
                    when (photoEffect) {
                        ExtensionMode.NONE -> R.string.effect_none
                        ExtensionMode.BOKEH -> R.string.effect_bokeh
                        ExtensionMode.HDR -> R.string.effect_hdr
                        ExtensionMode.NIGHT -> R.string.effect_night
                        ExtensionMode.FACE_RETOUCH -> R.string.effect_face_retouch
                        ExtensionMode.AUTO -> R.string.effect_auto
                        else -> R.string.effect_none
                    }
                )

                // Update BlackBerry Top Bar HDR button
                btnBbHdr.setImageResource(
                    when (photoEffect) {
                        ExtensionMode.HDR -> R.drawable.ic_hdr_on_white_24dp
                        ExtensionMode.AUTO -> R.drawable.ic_hdr_auto_white_24dp
                        else -> R.drawable.ic_hdr_off_white_24dp
                    }
                )
            }
        }

        launch {
            viewModel.isPhotoEffectButtonVisible.collectLatest { isPhotoEffectButtonVisible ->
                effectButton.isVisible = isPhotoEffectButtonVisible
            }
        }

        launch {
            viewModel.videoQuality.collectLatest { videoQuality ->
                // Update secondary bar buttons
                videoQualityButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    when (videoQuality) {
                        Quality.SD -> R.drawable.ic_sd
                        Quality.HD -> R.drawable.ic_hd
                        Quality.FHD -> R.drawable.ic_full_hd
                        Quality.UHD -> R.drawable.ic_4k
                        else -> throw Exception("Unknown video quality $videoQuality")
                    },
                    0,
                    0
                )
                videoQualityButton.setText(
                    when (videoQuality) {
                        Quality.SD -> R.string.video_quality_sd
                        Quality.HD -> R.string.video_quality_hd
                        Quality.FHD -> R.string.video_quality_fhd
                        Quality.UHD -> R.string.video_quality_uhd
                        else -> throw Exception("Unknown video quality $videoQuality")
                    }
                )
            }
        }

        launch {
            viewModel.isVideoQualityButtonEnabled.collectLatest { isVideoQualityButtonEnabled ->
                videoQualityButton.isEnabled = isVideoQualityButtonEnabled
            }
        }

        launch {
            viewModel.videoFrameRate.collectLatest { videoFrameRate ->
                // Update secondary bar buttons
                videoFrameRateButton.text = videoFrameRate?.let {
                    resources.getString(R.string.video_framerate_value, it.value)
                } ?: resources.getString(R.string.video_framerate_auto)
            }
        }

        launch {
            viewModel.isVideoFrameRateButtonEnabled.collectLatest { isVideoFrameRateButtonEnabled ->
                videoFrameRateButton.isEnabled = isVideoFrameRateButtonEnabled
            }
        }

        launch {
            viewModel.videoDynamicRange.collectLatest { videoDynamicRange ->
                // Update secondary bar buttons
                videoDynamicRangeButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    when (videoDynamicRange) {
                        VideoDynamicRange.SDR -> R.drawable.ic_hdr_off
                        VideoDynamicRange.HLG_10_BIT -> R.drawable.ic_hdr_on
                        VideoDynamicRange.HDR10_10_BIT -> R.drawable.ic_hdr_on
                        VideoDynamicRange.HDR10_PLUS_10_BIT -> R.drawable.ic_hdr_on
                        VideoDynamicRange.DOLBY_VISION_10_BIT -> R.drawable.ic_dolby
                        VideoDynamicRange.DOLBY_VISION_8_BIT -> R.drawable.ic_dolby
                    },
                    0,
                    0,
                )
                videoDynamicRangeButton.setText(
                    when (videoDynamicRange) {
                        VideoDynamicRange.SDR -> R.string.video_dynamic_range_sdr
                        VideoDynamicRange.HLG_10_BIT -> R.string.video_dynamic_range_hlg_10_bit
                        VideoDynamicRange.HDR10_10_BIT -> R.string.video_dynamic_range_hdr10_10_bit
                        VideoDynamicRange.HDR10_PLUS_10_BIT -> R.string.video_dynamic_range_hdr10_plus_10_bit
                        VideoDynamicRange.DOLBY_VISION_10_BIT -> R.string.video_dynamic_range_dolby_vision_10_bit
                        VideoDynamicRange.DOLBY_VISION_8_BIT -> R.string.video_dynamic_range_dolby_vision_8_bit
                    }
                )
            }
        }

        launch {
            viewModel.isVideoDynamicRangeButtonEnabled.collectLatest { isVideoDynamicRangeButtonEnabled ->
                videoDynamicRangeButton.isEnabled = isVideoDynamicRangeButtonEnabled
            }
        }

        launch {
            viewModel.videoMicMode.collectLatest { videoMicMode ->
                micButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    when (videoMicMode) {
                        true -> R.drawable.ic_mic_on
                        false -> R.drawable.ic_mic_off
                    },
                    0,
                    0
                )
                micButton.setText(
                    when (videoMicMode) {
                        true -> R.string.mic_on
                        false -> R.string.mic_off
                    }
                )
            }
        }

        launch {
            viewModel.isVideoMicButtonEnabled.collectLatest { isVideoMicButtonEnabled ->
                micButton.isEnabled = isVideoMicButtonEnabled
            }
        }

        launch {
            viewModel.videoRecordingDuration.collectLatest { videoRecordingDuration ->
                cameraModeSelectorLayout.setVideoRecordingDuration(videoRecordingDuration)
            }
        }

        launch {
            viewModel.videoRecordEvent.collect { videoRecordEvent ->
                when (videoRecordEvent) {
                    is VideoRecordEvent.Start -> runOnUiThread {
                        startVideoRecordingStateAnimation(
                            VideoRecordingStateAnimation.Init
                        )
                    }

                    is VideoRecordEvent.Pause -> runOnUiThread {
                        startVideoRecordingStateAnimation(
                            VideoRecordingStateAnimation.ResumeToPause
                        )
                    }

                    is VideoRecordEvent.Resume -> runOnUiThread {
                        startVideoRecordingStateAnimation(
                            VideoRecordingStateAnimation.PauseToResume
                        )
                    }

                    is VideoRecordEvent.Status -> {
                        // Do nothing
                    }

                    is VideoRecordEvent.Finalize -> {
                        runOnUiThread {
                            startShutterAnimation(ShutterAnimation.VideoEnd)
                        }
                        if (videoRecordEvent.error != VideoRecordEvent.Finalize.ERROR_NO_VALID_DATA) {
                            if (!viewModel.inSingleCaptureMode.value) {
                                onCapturedMedia(videoRecordEvent.outputResults.outputUri)
                            } else {
                                openCapturePreview(
                                    videoRecordEvent.outputResults.outputUri,
                                    MediaType.VIDEO,
                                )
                            }
                        }
                    }
                }
            }
        }

        launch {
            viewModel.liveQrResult.collectLatest { qrResult ->
                if (viewModel.cameraMode.value == CameraMode.QR) {
                    if (qrResult != null) {
                        qrBottomSheetDialog.setQrResult(qrResult)
                    }
                } else {
                    updateLiveQrChip(qrResult)
                }
            }
        }

        launch {
            viewModel.canFlipCamera.collectLatest { canFlipCamera ->
                flipCameraButton.isInvisible = !canFlipCamera
            }
        }

        launch {
            viewModel.islandItems.collectLatest { islandItems ->
                islandView.setItems(islandItems)
            }
        }
    }

    private fun updateOverlaysVisibility() {
        val isAnyPopupOpen = bbModeGridContainer.isVisible ||
                bbFilterRibbonContainer.isVisible ||
                bbProControlsContainer.isVisible ||
                llBbSubmenu.isVisible
        val isCountdown = viewModel.cameraState.value == CameraState.COUNTDOWN
        lensSelectorLayout.isVisible = !isAnyPopupOpen && !isCountdown
        if (isAnyPopupOpen || isCountdown) {
            liveQrChipContainer?.isVisible = false
        }
    }

    private fun setupBbUi() {
        setupTopBarSubmenus()
        setupProControls()

        // Bottom deck mode button & grid
        bbModeButton.setOnClickListener {
            val show = !bbModeGridContainer.isVisible
            bbModeGridContainer.isVisible = show
            if (show) {
                bbFilterRibbonContainer.isVisible = false
                bbProControlsContainer.isVisible = false
                llBbSubmenu.isVisible = false
            }
            updateModeGridHighlight()
            updateOverlaysVisibility()
        }

        modeItemPhoto.setOnClickListener {
            bbProControlsContainer.isVisible = false
            viewModel.setCameraMode(CameraMode.PHOTO)
            bbModeGridContainer.isVisible = false
            updateModeGridHighlight()
            updateManualControls()
            updateOverlaysVisibility()
        }

        modeItemVideo.setOnClickListener {
            bbProControlsContainer.isVisible = false
            viewModel.setCameraMode(CameraMode.VIDEO)
            bbModeGridContainer.isVisible = false
            updateModeGridHighlight()
            updateOverlaysVisibility()
        }

        modeItemPro.setOnClickListener {
            val show = !bbProControlsContainer.isVisible
            bbProControlsContainer.isVisible = show
            bbEvDial.isVisible = show
            bbProSubmenuScroll.isVisible = false
            if (show && viewModel.cameraMode.value != CameraMode.PHOTO) {
                viewModel.setCameraMode(CameraMode.PHOTO)
            }
            bbModeGridContainer.isVisible = false
            updateModeGridHighlight()
            updateOverlaysVisibility()
        }

        // Live filter ribbon
        bbFilterButton.setOnClickListener {
            val show = !bbFilterRibbonContainer.isVisible
            bbFilterRibbonContainer.isVisible = show
            if (show) {
                bbModeGridContainer.isVisible = false
                bbProControlsContainer.isVisible = false
                llBbSubmenu.isVisible = false
                setupFilterRibbon()
            }
            updateOverlaysVisibility()
        }
    }

    private fun updateModeGridHighlight() {
        val currentMode = viewModel.cameraMode.value
        val isPro = bbProControlsContainer.isVisible

        ivModeIconPhoto.setBackgroundResource(if (!isPro && currentMode == CameraMode.PHOTO) R.drawable.bb_mode_icon_bg_selected else R.drawable.bb_mode_icon_bg)
        ivModeIconVideo.setBackgroundResource(if (currentMode == CameraMode.VIDEO) R.drawable.bb_mode_icon_bg_selected else R.drawable.bb_mode_icon_bg)
        ivModeIconPro.setBackgroundResource(if (isPro) R.drawable.bb_mode_icon_bg_selected else R.drawable.bb_mode_icon_bg)

        bbModeButton.setImageResource(
            when {
                isPro -> R.drawable.ic_mode_pro
                currentMode == CameraMode.VIDEO -> R.drawable.ic_mode_video
                else -> R.drawable.ic_mode_photo
            }
        )
    }

    private fun setupTopBarSubmenus() {
        btnBbFlash.setOnClickListener {
            if (llBbSubmenu.isVisible && llBbSubmenu.tag == "flash") {
                llBbSubmenu.isVisible = false
            } else {
                showFlashSubmenu()
            }
            updateOverlaysVisibility()
        }

        btnBbTimer.setOnClickListener {
            if (llBbSubmenu.isVisible && llBbSubmenu.tag == "timer") {
                llBbSubmenu.isVisible = false
            } else {
                showTimerSubmenu()
            }
            updateOverlaysVisibility()
        }

        btnBbAspectRatio.setOnClickListener {
            if (llBbSubmenu.isVisible && llBbSubmenu.tag == "ratio") {
                llBbSubmenu.isVisible = false
            } else {
                showAspectRatioSubmenu()
            }
            updateOverlaysVisibility()
        }

        btnBbHdr.setOnClickListener {
            if (llBbSubmenu.isVisible && llBbSubmenu.tag == "hdr") {
                llBbSubmenu.isVisible = false
            } else {
                showHdrSubmenu()
            }
            updateOverlaysVisibility()
        }

        btnBbGrid.setOnClickListener {
            if (llBbSubmenu.isVisible && llBbSubmenu.tag == "grid") {
                llBbSubmenu.isVisible = false
            } else {
                showGridSubmenu()
            }
            updateOverlaysVisibility()
        }

        btnBbSettings.setOnClickListener {
            openSettings()
        }
    }

    private fun getScaledSubmenuDrawable(iconRes: Int, isSelected: Boolean): Drawable? {
        val iconHeight = 16.px
        return ContextCompat.getDrawable(this, iconRes)?.mutate()?.apply {
            val aspect = intrinsicWidth.toFloat() / intrinsicHeight.toFloat().coerceAtLeast(1f)
            val iconWidth = (iconHeight * aspect).toInt()
            setBounds(0, 0, iconWidth, iconHeight)
            setTint(if (isSelected) Color.BLACK else Color.WHITE)
        }
    }

    private fun showFlashSubmenu() {
        llBbSubmenu.removeAllViews()
        llBbSubmenu.tag = "flash"
        llBbSubmenu.isVisible = true
        updateOverlaysVisibility()

        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(12.px, 6.px, 12.px, 6.px)
        }

        val currentFlash = viewModel.flashMode.value
        val options = listOf(
            Triple(FlashMode.OFF, R.drawable.ic_flash_off_white_24dp, "OFF"),
            Triple(FlashMode.AUTO, R.drawable.ic_flash_auto_white_24dp, "AUTO"),
            Triple(FlashMode.ON, R.drawable.ic_flash_on_white_24dp, "ON")
        )

        for ((mode, iconRes, label) in options) {
            val isSelected = currentFlash == mode
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 36.px, 1f).apply {
                    setMargins(4.px, 0, 4.px, 0)
                }
                text = label
                textSize = 10f
                setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                val iconDrawable = getScaledSubmenuDrawable(iconRes, isSelected)
                setCompoundDrawables(iconDrawable, null, null, null)
                compoundDrawablePadding = 4.px
                gravity = Gravity.CENTER
                setPadding(8.px, 2.px, 8.px, 2.px)
                setOnClickListener {
                    viewModel.setFlashMode(mode)
                    viewModel.cameraController.flashMode = mode
                    btnBbFlash.setImageResource(iconRes)
                    llBbSubmenu.isVisible = false
                    updateOverlaysVisibility()
                }
            }
            row.addView(btn)
        }
        llBbSubmenu.addView(row)
    }

    private fun showTimerSubmenu() {
        llBbSubmenu.removeAllViews()
        llBbSubmenu.tag = "timer"
        llBbSubmenu.isVisible = true
        updateOverlaysVisibility()

        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(12.px, 6.px, 12.px, 6.px)
        }

        val currentTimer = viewModel.timerMode.value
        val options = listOf(
            Triple(TimerMode.OFF, R.drawable.ic_timer_off_white_24dp, "OFF"),
            Triple(TimerMode.ON_3S, R.drawable.ic_timer_3_white_24dp, "3s"),
            Triple(TimerMode.ON_10S, R.drawable.ic_timer_10_white_24dp, "10s")
        )

        for ((mode, iconRes, label) in options) {
            val isSelected = currentTimer == mode
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 36.px, 1f).apply {
                    setMargins(4.px, 0, 4.px, 0)
                }
                text = label
                textSize = 10f
                setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                val iconDrawable = getScaledSubmenuDrawable(iconRes, isSelected)
                setCompoundDrawables(iconDrawable, null, null, null)
                compoundDrawablePadding = 4.px
                gravity = Gravity.CENTER
                setPadding(8.px, 2.px, 8.px, 2.px)
                setOnClickListener {
                    viewModel.setTimerMode(mode)
                    btnBbTimer.setImageResource(iconRes)
                    llBbSubmenu.isVisible = false
                    updateOverlaysVisibility()
                }
            }
            row.addView(btn)
        }
        llBbSubmenu.addView(row)
    }

    data class RatioOption(
        val iconRes: Int,
        val ratio: Int,
        val isSquare: Boolean,
        val is3_2: Boolean,
        val name: String
    )

    private fun showAspectRatioSubmenu() {
        llBbSubmenu.removeAllViews()
        llBbSubmenu.tag = "ratio"
        llBbSubmenu.isVisible = true
        updateOverlaysVisibility()

        // 1. Aspect Ratio selector row
        val ratioRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8.px, 4.px, 8.px, 4.px)
        }

        val ratioOptions = listOf(
            RatioOption(R.drawable.ic_ratio_4_3_white_24dp, AspectRatio.RATIO_4_3, false, false, "4:3"),
            RatioOption(R.drawable.ic_ratio_16_9_white_24dp, AspectRatio.RATIO_16_9, false, false, "16:9"),
            RatioOption(R.drawable.ic_ratio_square_white_24dp, AspectRatio.RATIO_4_3, true, false, "1:1"),
            RatioOption(R.drawable.ic_ratio_3_2_white_24dp, AspectRatio.RATIO_4_3, false, true, "3:2")
        )

        for (opt in ratioOptions) {
            val isCurrent = when {
                opt.isSquare -> isSquareMode
                opt.is3_2 -> is3_2Mode
                else -> !isSquareMode && !is3_2Mode && currentSelectedRatio == opt.ratio
            }

            val btn = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 36.px, 1f).apply {
                    setMargins(4.px, 0, 4.px, 0)
                }
                setBackgroundResource(if (isCurrent) R.drawable.lens_selector_button_background else android.R.color.transparent)
                backgroundTintList = ColorStateList.valueOf(
                    if (isCurrent) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                setImageResource(opt.iconRes)
                imageTintList = ColorStateList.valueOf(if (isCurrent) Color.BLACK else Color.WHITE)
                setPadding(6.px, 6.px, 6.px, 6.px)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setOnClickListener {
                    currentSelectedRatio = opt.ratio
                    isSquareMode = opt.isSquare
                    is3_2Mode = opt.is3_2
                    updateViewFinderFraming()
                    btnBbAspectRatio.setImageResource(opt.iconRes)

                    // Ensure target resolution matches this format
                    val resList = getAvailableResolutions(opt.ratio, opt.isSquare, opt.is3_2)
                    val def = resList.find { it.size == currentTargetResolution } ?: resList.getOrNull(0) ?: resList.first()
                    currentTargetResolution = def.size
                    currentCaptureResolution = def.captureSize
                    currentResolutionLabel = def.label

                    val underlyingRatio = if (opt.isSquare || opt.is3_2) AspectRatio.RATIO_4_3 else opt.ratio
                    viewModel.setPhotoAspectRatio(underlyingRatio)
                    viewModel.rebindCamera()
                    showAspectRatioSubmenu()
                }
            }
            ratioRow.addView(btn)
        }
        llBbSubmenu.addView(ratioRow)

        // 2. Divider / Header
        // 2. Resolutions row
        val resList = getAvailableResolutions(currentSelectedRatio, isSquareMode, is3_2Mode)
        if (currentTargetResolution == null || resList.none { it.size == currentTargetResolution }) {
            resList.firstOrNull()?.let {
                currentTargetResolution = it.size
                currentCaptureResolution = it.captureSize
                currentResolutionLabel = it.label
            }
        }

        // Header
        val header = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 6.px, 0, 4.px)
            }
            text = getString(R.string.bb_resolution_header, currentResolutionLabel)
            textSize = 10f
            setTextColor(Color.parseColor("#BBBBBB"))
            typeface = Typeface.DEFAULT_BOLD
        }
        llBbSubmenu.addView(header)

        val scroll = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            isHorizontalScrollBarEnabled = false
        }
        val resRow = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(12.px, 2.px, 12.px, 2.px)
        }

        for (opt in resList) {
            val isSelected = currentTargetResolution == opt.size || (currentTargetResolution == null && opt == resList.getOrNull(1))
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 36.px).apply {
                    setMargins(4.px, 0, 4.px, 0)
                }
                text = "${opt.label}\n${opt.mp}"
                textSize = 9f
                setLineSpacing(0f, 0.9f)
                setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                setPadding(10.px, 2.px, 10.px, 2.px)
                setOnClickListener {
                    currentTargetResolution = opt.size
                    currentCaptureResolution = opt.captureSize
                    currentResolutionLabel = opt.label
                    viewModel.rebindCamera()
                    Toast.makeText(this@CameraActivity, "${opt.label} (${opt.mp})", Toast.LENGTH_SHORT).show()
                    showAspectRatioSubmenu()
                }
            }
            resRow.addView(btn)
        }
        scroll.addView(resRow)
        llBbSubmenu.addView(scroll)

        // 4. Capture mode options (Min latency or Max quality)
        val captureHeader = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 6.px, 0, 4.px)
            }
            text = getString(R.string.bb_capture_mode_header)
            textSize = 10f
            setTextColor(Color.parseColor("#BBBBBB"))
            typeface = Typeface.DEFAULT_BOLD
        }
        llBbSubmenu.addView(captureHeader)

        val captureModeRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(12.px, 2.px, 12.px, 4.px)
        }

        val currentCaptureMode = viewModel.getPhotoCaptureModePreference()
        val isMinLatency = currentCaptureMode == ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        val isMaxQuality = currentCaptureMode == ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        val isZsl = viewModel.getEnableZslPreference()

        val primaryOptions = listOf(
            Triple(getString(R.string.bb_min_latency), isMinLatency) {
                viewModel.setCaptureModePreference(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY, isZsl)
                viewModel.rebindCamera()
                Toast.makeText(this@CameraActivity, getString(R.string.bb_toast_min_latency), Toast.LENGTH_SHORT).show()
                showAspectRatioSubmenu()
            },
            Triple(getString(R.string.bb_max_quality), isMaxQuality) {
                viewModel.setCaptureModePreference(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY, false)
                viewModel.rebindCamera()
                Toast.makeText(this@CameraActivity, getString(R.string.bb_toast_max_quality), Toast.LENGTH_SHORT).show()
                showAspectRatioSubmenu()
            }
        )

        for ((label, isSelected, onClick) in primaryOptions) {
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 36.px, 1f).apply {
                    setMargins(4.px, 0, 4.px, 0)
                }
                text = label
                textSize = 10f
                setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                setPadding(6.px, 2.px, 6.px, 2.px)
                setOnClickListener { onClick() }
            }
            captureModeRow.addView(btn)
        }
        llBbSubmenu.addView(captureModeRow)

        // 5. If min latency is selected: show ZSL toggle options!
        if (isMinLatency) {
            val zslRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(12.px, 2.px, 12.px, 6.px)
            }

            val zslOptions = listOf(
                Triple(getString(R.string.bb_zsl_enabled), isZsl) {
                    viewModel.setCaptureModePreference(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY, true)
                    viewModel.rebindCamera()
                    Toast.makeText(this@CameraActivity, getString(R.string.bb_toast_zsl_on), Toast.LENGTH_SHORT).show()
                    showAspectRatioSubmenu()
                },
                Triple(getString(R.string.bb_zsl_disabled), !isZsl) {
                    viewModel.setCaptureModePreference(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY, false)
                    viewModel.rebindCamera()
                    Toast.makeText(this@CameraActivity, getString(R.string.bb_toast_zsl_off), Toast.LENGTH_SHORT).show()
                    showAspectRatioSubmenu()
                }
            )

            for ((label, isSelected, onClick) in zslOptions) {
                val btn = Button(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 32.px, 1f).apply {
                        setMargins(4.px, 0, 4.px, 0)
                    }
                    text = label
                    textSize = 9.5f
                    setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    setBackgroundResource(R.drawable.lens_selector_button_background)
                    backgroundTintList = ColorStateList.valueOf(
                        if (isSelected) Color.WHITE else Color.parseColor("#22FFFFFF")
                    )
                    setPadding(6.px, 2.px, 6.px, 2.px)
                    setOnClickListener { onClick() }
                }
                zslRow.addView(btn)
            }
            llBbSubmenu.addView(zslRow)
        }
    }

    private fun showHdrSubmenu() {
        llBbSubmenu.removeAllViews()
        llBbSubmenu.tag = "hdr"
        llBbSubmenu.isVisible = true
        updateOverlaysVisibility()

        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(12.px, 6.px, 12.px, 6.px)
        }

        val options = listOf(
            Triple(false, R.drawable.ic_hdr_off_white_24dp, "HDR OFF"),
            Triple(true, R.drawable.ic_hdr_auto_white_24dp, "HDR AUTO"),
            Triple(true, R.drawable.ic_hdr_on_white_24dp, "HDR ON")
        )

        for ((enabled, iconRes, label) in options) {
            val isSelected = (enabled == isHdrEnabled && (if (enabled) btnBbHdr.tag == label else true))
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 36.px, 1f).apply {
                    setMargins(4.px, 0, 4.px, 0)
                }
                text = label.replace("HDR ", "")
                textSize = 10f
                setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                val iconDrawable = getScaledSubmenuDrawable(iconRes, isSelected)
                setCompoundDrawables(iconDrawable, null, null, null)
                compoundDrawablePadding = 4.px
                gravity = Gravity.CENTER
                setPadding(8.px, 2.px, 8.px, 2.px)
                setOnClickListener {
                    isHdrEnabled = enabled
                    btnBbHdr.tag = label
                    updateManualControls()
                    btnBbHdr.setImageResource(iconRes)
                    llBbSubmenu.isVisible = false
                    updateOverlaysVisibility()
                    Toast.makeText(
                        this@CameraActivity,
                        if (enabled) "$label activat" else "HDR desactivat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            row.addView(btn)
        }
        llBbSubmenu.addView(row)
    }

    private fun showGridSubmenu() {
        llBbSubmenu.removeAllViews()
        llBbSubmenu.tag = "grid"
        llBbSubmenu.isVisible = true
        updateOverlaysVisibility()

        // 1. Grid Options Row
        val gridRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(10.px, 4.px, 10.px, 2.px)
        }

        val currentGrid = viewModel.gridMode.value
        val gridOptions = listOf(
            Triple(GridMode.OFF, R.drawable.ic_grid_3x3_off, "OFF"),
            Triple(GridMode.ON_3, R.drawable.ic_grid_3x3, "3×3"),
            Triple(GridMode.ON_4, R.drawable.ic_grid_4x4, "4×4"),
            Triple(GridMode.ON_GOLDEN_RATIO, R.drawable.ic_grid_goldenratio, "Φ Golden")
        )

        for ((mode, iconRes, label) in gridOptions) {
            val isSelected = currentGrid == mode
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 36.px, 1f).apply {
                    setMargins(3.px, 0, 3.px, 0)
                }
                text = label
                textSize = 9.5f
                setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                val iconDrawable = getScaledSubmenuDrawable(iconRes, isSelected)
                setCompoundDrawables(iconDrawable, null, null, null)
                compoundDrawablePadding = 3.px
                gravity = Gravity.CENTER
                setPadding(4.px, 2.px, 4.px, 2.px)
                setOnClickListener {
                    viewModel.setGridMode(mode)
                    showGridSubmenu()
                }
            }
            gridRow.addView(btn)
        }
        llBbSubmenu.addView(gridRow)

        // 2. Leveler Section Header
        val levelerHeader = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 6.px, 0, 4.px)
            }
            text = getString(R.string.leveler_title).uppercase()
            textSize = 10f
            setTextColor(Color.parseColor("#BBBBBB"))
            typeface = Typeface.DEFAULT_BOLD
        }
        llBbSubmenu.addView(levelerHeader)

        // 3. Leveler Options Row
        val levelerRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(10.px, 2.px, 10.px, 6.px)
        }

        val isLevelerOn = viewModel.levelerEnabled.value
        val offLabel = getString(R.string.bb_zsl_disabled).replace("ZSL: ", "")
        val onLabel = getString(R.string.bb_zsl_enabled).replace("ZSL: ", "")
        val levelerOptions = listOf(
            Pair("${getString(R.string.leveler_title)}: $offLabel", false),
            Pair("${getString(R.string.leveler_title)}: $onLabel", true)
        )

        for ((label, enabled) in levelerOptions) {
            val isSelected = isLevelerOn == enabled
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 32.px, 1f).apply {
                    setMargins(3.px, 0, 3.px, 0)
                }
                text = label
                textSize = 9.5f
                setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.WHITE else Color.parseColor("#33FFFFFF")
                )
                val iconDrawable = getScaledSubmenuDrawable(R.drawable.ic_screen_rotation, isSelected)
                setCompoundDrawables(iconDrawable, null, null, null)
                compoundDrawablePadding = 3.px
                gravity = Gravity.CENTER
                setPadding(4.px, 2.px, 4.px, 2.px)
                setOnClickListener {
                    viewModel.setLevelerEnabled(enabled)
                    showGridSubmenu()
                }
            }
            levelerRow.addView(btn)
        }
        llBbSubmenu.addView(levelerRow)
    }

    private fun setupProControls() {
        btnProEv.setOnClickListener {
            bbEvDial.isVisible = !bbEvDial.isVisible
            bbProSubmenuScroll.isVisible = false
        }

        btnProIso.setOnClickListener {
            showProSubmenu("ISO", listOf("AUTO", "100", "200", "400", "800", "1600", "3200")) { value ->
                currentIso = if (value == "AUTO") null else value.toIntOrNull()
                btnProIso.text = if (currentIso == null) "ISO" else "ISO $currentIso"
                updateManualControls()
            }
        }

        btnProShutter.setOnClickListener {
            val shutterOptions = listOf(
                "AUTO" to null,
                "1/1000s" to 1_000_000L,
                "1/500s" to 2_000_000L,
                "1/250s" to 4_000_000L,
                "1/125s" to 8_000_000L,
                "1/60s" to 16_666_666L,
                "1/30s" to 33_333_333L,
                "1/15s" to 66_666_666L,
                "1/8s" to 125_000_000L,
                "1/4s" to 250_000_000L,
                "1/2s" to 500_000_000L
            )
            showProSubmenu("SEC", shutterOptions.map { it.first }) { label ->
                currentExposureNanos = shutterOptions.firstOrNull { it.first == label }?.second
                btnProShutter.text = if (currentExposureNanos == null) "SEC" else label
                updateManualControls()
            }
        }

        btnProWb.setOnClickListener {
            val wbOptions = listOf(
                "AUTO" to null,
                "Incandescent" to CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT,
                "Fluorescent" to CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT,
                "Daylight" to CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT,
                "Cloudy" to CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
            )
            showProSubmenu("WB", wbOptions.map { it.first }) { label ->
                currentAwbMode = wbOptions.firstOrNull { it.first == label }?.second
                btnProWb.text = if (currentAwbMode == null) "WB" else label.take(4)
                updateManualControls()
            }
        }

        btnProMf.setOnClickListener {
            val focusOptions = listOf(
                "AUTO" to null,
                "Macro" to 10.0f,
                "Infinit" to 0.0f
            )
            showProSubmenu("MF", focusOptions.map { it.first }) { label ->
                currentFocusDistance = focusOptions.firstOrNull { it.first == label }?.second
                btnProMf.text = if (currentFocusDistance == null) "MF" else label
                updateManualControls()
            }
        }

        btnProAuto.setOnClickListener {
            currentIso = null
            currentExposureNanos = null
            currentAwbMode = null
            currentFocusDistance = null
            btnProIso.text = "ISO"
            btnProShutter.text = "SEC"
            btnProWb.text = "WB"
            btnProMf.text = "MF"
            val zeroTick = findViewById<View>(R.id.evTick_0)
            if (zeroTick != null) {
                setEv(0f, zeroTick)
            }
            updateManualControls()
            bbProSubmenuScroll.isVisible = false
            Toast.makeText(this, "Pro: Tot en mode Automàtic", Toast.LENGTH_SHORT).show()
        }

        // Ticks for EV dial
        val evTicks = listOf(
            findViewById<View>(R.id.evTick_m15) to -1.5f,
            findViewById<View>(R.id.evTick_m10) to -1.0f,
            findViewById<View>(R.id.evTick_m05) to -0.5f,
            findViewById<View>(R.id.evTick_0) to 0.0f,
            findViewById<View>(R.id.evTick_p05) to 0.5f,
            findViewById<View>(R.id.evTick_p10) to 1.0f,
            findViewById<View>(R.id.evTick_p15) to 1.5f
        )
        for ((tickView, evValue) in evTicks) {
            tickView?.setOnClickListener {
                setEv(evValue, tickView)
            }
        }
    }

    private fun setEv(ev: Float, targetView: View) {
        val state = viewModel.cameraController.cameraInfo?.exposureState
        if (state != null && state.isExposureCompensationSupported) {
            val step = state.exposureCompensationStep.toFloat()
            val index = if (step > 0) Math.round(ev / step) else 0
            val clamped = index.coerceIn(state.exposureCompensationRange.lower, state.exposureCompensationRange.upper)
            viewModel.cameraController.cameraControl?.setExposureCompensationIndex(clamped)
        }
        targetView.post {
            val centerX = targetView.x + targetView.width / 2f
            tvEvIndicator.x = centerX - tvEvIndicator.width / 2f
        }
    }

    private fun showProSubmenu(tag: String, items: List<String>, onSelect: (String) -> Unit) {
        if (bbProSubmenuScroll.isVisible && bbProSubmenuScroll.tag == tag) {
            bbProSubmenuScroll.isVisible = false
            return
        }
        bbProSubmenuScroll.tag = tag
        bbProSubmenuList.removeAllViews()
        bbProSubmenuScroll.isVisible = true

        for (item in items) {
            val tv = TextView(this).apply {
                text = item
                setTextColor(Color.WHITE)
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(18.px, 10.px, 18.px, 10.px)
                setBackgroundResource(R.drawable.lens_selector_button_background)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"))
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(6.px, 0, 6.px, 0)
                }
                layoutParams = lp
                setOnClickListener {
                    onSelect(item)
                    bbProSubmenuScroll.isVisible = false
                }
            }
            bbProSubmenuList.addView(tv)
        }
    }

    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    private fun updateManualControls() {
        val camera2Control = viewModel.cameraController.camera2CameraControl ?: return
        val builder = CaptureRequestOptions.Builder()
        if (currentIso != null || currentExposureNanos != null) {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            currentIso?.let { builder.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, it) }
            currentExposureNanos?.let { builder.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, it) }
        }
        if (currentFocusDistance != null) {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
            builder.setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, currentFocusDistance)
        } else {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        }
        if (currentAwbMode != null) {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, currentAwbMode)
        } else {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        }
        if (isHdrEnabled) {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE)
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_HDR)
        } else {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_DISABLED)
        }
        camera2Control.setCaptureRequestOptions(builder.build())
    }

    private fun setupFilterRibbon() {
        bbFilterItemList.removeAllViews()
        for (filter in BbFilter.entries) {
            val itemView = layoutInflater.inflate(R.layout.bb_filter_item, bbFilterItemList, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvFilterTitle)
            val ivThumbnail = itemView.findViewById<ImageView>(R.id.ivFilterThumbnail)
            val card = itemView.findViewById<MaterialCardView>(R.id.cardFilterThumbnail)

            tvTitle.setText(filter.titleRes)
            ivThumbnail.setImageResource(filter.drawableRes)

            val isSelected = filter == currentBbFilter
            card.strokeWidth = if (isSelected) 3.px else 0
            card.strokeColor = Color.WHITE

            itemView.setOnClickListener {
                applyFilter(filter)
                setupFilterRibbon()
            }
            bbFilterItemList.addView(itemView)
        }
    }

    private fun applyFilter(filter: BbFilter) {
        currentBbFilter = filter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = filter.colorMatrix?.let {
                RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(it))
            }
            viewFinder.setRenderEffect(effect)
        }
        bbFilterButton.setImageResource(
            if (filter == BbFilter.NONE) R.drawable.ic_filter_off_color_24dp
            else R.drawable.ic_filter_on_color_24dp
        )
    }

    private fun processSavedPhoto(uri: Uri) {
        try {
            val transform = contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                ExifUtils.getTransform(pfd.fileDescriptor)
            } ?: org.lineageos.aperture.models.Transform.DEFAULT

            val rawBitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: return

            // Ensure upright orientation from EXIF so faces and documents align correctly
            var outputBitmap = rawBitmap.transform(transform)
            if (outputBitmap != rawBitmap) {
                rawBitmap.recycle()
            }

            // 1. Crop according to active aspect ratio from full 4:3 sensor capture
            if (isSquareMode) {
                val size = minOf(outputBitmap.width, outputBitmap.height)
                val x = (outputBitmap.width - size) / 2
                val y = (outputBitmap.height - size) / 2
                val square = Bitmap.createBitmap(outputBitmap, x, y, size, size)
                if (outputBitmap != rawBitmap) outputBitmap.recycle()
                outputBitmap = square
            } else if (currentSelectedRatio == AspectRatio.RATIO_16_9 && !isSquareMode && !is3_2Mode) {
                val isLandscape = outputBitmap.width >= outputBitmap.height
                val targetW = if (isLandscape) outputBitmap.width else (outputBitmap.height * 9) / 16
                val targetH = if (isLandscape) (outputBitmap.width * 9) / 16 else outputBitmap.height
                if (targetW <= outputBitmap.width && targetH <= outputBitmap.height) {
                    val x = (outputBitmap.width - targetW) / 2
                    val y = (outputBitmap.height - targetH) / 2
                    val cropped169 = Bitmap.createBitmap(outputBitmap, x, y, targetW, targetH)
                    if (outputBitmap != rawBitmap) outputBitmap.recycle()
                    outputBitmap = cropped169
                }
            } else if (is3_2Mode) {
                val isLandscape = outputBitmap.width >= outputBitmap.height
                val targetW = if (isLandscape) outputBitmap.width else (outputBitmap.height * 2) / 3
                val targetH = if (isLandscape) (outputBitmap.width * 2) / 3 else outputBitmap.height
                if (targetW <= outputBitmap.width && targetH <= outputBitmap.height) {
                    val x = (outputBitmap.width - targetW) / 2
                    val y = (outputBitmap.height - targetH) / 2
                    val cropped32 = Bitmap.createBitmap(outputBitmap, x, y, targetW, targetH)
                    if (outputBitmap != rawBitmap) outputBitmap.recycle()
                    outputBitmap = cropped32
                }
            }

            // 2. Scale to requested resolution if specified
            val targetRes = currentTargetResolution
            if (targetRes != null) {
                val maxTarget = maxOf(targetRes.width, targetRes.height)
                val maxCurr = maxOf(outputBitmap.width, outputBitmap.height)
                if (maxCurr > maxTarget) {
                    val scale = maxTarget.toFloat() / maxCurr.toFloat()
                    val targetW = (outputBitmap.width * scale).toInt()
                    val targetH = (outputBitmap.height * scale).toInt()
                    val scaled = Bitmap.createScaledBitmap(outputBitmap, targetW, targetH, true)
                    if (outputBitmap != rawBitmap) outputBitmap.recycle()
                    outputBitmap = scaled
                }
            }

            // 3. Apply color filter if not NONE
            val filterMatrix = currentBbFilter.colorMatrix
            if (filterMatrix != null) {
                val filtered = Bitmap.createBitmap(
                    outputBitmap.width,
                    outputBitmap.height,
                    outputBitmap.config ?: Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(filtered)
                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(filterMatrix)
                }
                canvas.drawBitmap(outputBitmap, 0f, 0f, paint)
                if (outputBitmap != rawBitmap) outputBitmap.recycle()
                outputBitmap = filtered
            }

            // 4. Write back upright to URI
            contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                outputBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }
            try {
                contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                    val exif = androidx.exifinterface.media.ExifInterface(pfd.fileDescriptor)
                    exif.setAttribute(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL.toString()
                    )
                    exif.saveAttributes()
                }
            } catch (e: Exception) {
                Log.w(LOG_TAG, "Failed to update EXIF orientation attribute", e)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to post-process photo", e)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateViewFinderFraming()
    }

    private fun updateViewFinderFraming() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val ratioStr = when {
            isSquareMode -> "1:1"
            is3_2Mode -> if (isLandscape) "3:2" else "2:3"
            currentSelectedRatio == AspectRatio.RATIO_16_9 -> if (isLandscape) "16:9" else "9:16"
            else -> if (isLandscape) "4:3" else "3:4"
        }
        val params = viewFinder.layoutParams as? ConstraintLayout.LayoutParams ?: return
        params.constrainedWidth = true
        params.constrainedHeight = true
        params.dimensionRatio = ratioStr
        viewFinder.layoutParams = params
        viewFinder.scaleType = PreviewView.ScaleType.FILL_CENTER
    }

    private suspend fun queueSetupWithCameraPermissions() {
        var initialized = false

        permissionsManager.permissionStateFlow(Permission.CAMERA).collect { permissionState ->
            if (permissionState == PermissionState.GRANTED && !initialized) {
                // Attach CameraController to PreviewView
                viewFinder.controller = viewModel.cameraController

                // Attach CameraController to ScreenFlashView
                screenFlashView.setController(viewModel.cameraController)
                screenFlashView.setScreenFlashWindow(window)

                if (viewModel.initialCameraMode == CameraMode.VIDEO && !viewModel.isVideoRecordingAvailable()) {
                    // If an app asked for a video we have to bail out
                    if (viewModel.inSingleCaptureMode.value) {
                        Toast.makeText(
                            this@CameraActivity,
                            R.string.camcorder_unsupported_toast,
                            Toast.LENGTH_LONG,
                        ).show()
                        finish()
                        return@collect
                    }
                    // Fallback to photo mode
                    viewModel.initialCameraMode = CameraMode.PHOTO
                }

                if (!viewModel.initializeCameraConfiguration()) {
                    noCamera()
                }

                initialized = true
            }
        }
    }

    private fun startShutterAnimation(shutterAnimation: ShutterAnimation) {
        val drawable = ContextCompat.getDrawable(
            this, shutterAnimation.resourceId
        ) as AnimatedVectorDrawable

        shutterButton.setImageDrawable(drawable)

        when (shutterAnimation) {
            ShutterAnimation.InitPhoto,
            ShutterAnimation.InitVideo -> drawable.reset()
            else -> drawable.start()
        }
    }

    private fun startVideoRecordingStateAnimation(animation: VideoRecordingStateAnimation) {
        // Get appropriate drawable
        val drawable = ContextCompat.getDrawable(
            this, animation.resourceId
        ) as AnimatedVectorDrawable

        // Update current drawable
        videoRecordingStateButton.setImageDrawable(drawable)

        // Start or reset animation
        when (animation) {
            VideoRecordingStateAnimation.Init -> drawable.reset()
            else -> drawable.start()
        }
    }

    /**
     * Rebind cameraProvider use cases
     */
    private fun bindCameraUseCases(cameraConfiguration: CameraConfiguration) {
        require(viewModel.cameraState.value == CameraState.IDLE) {
            "Trying to rebind camera while camera is not in IDLE state"
        }

        if (hasBoundCameraOnce) {
            // Show blurred preview
            previewBlurView.freeze()
            previewBlurView.isVisible = true

            // Unbind previous use cases
            viewModel.cameraController.unbind()
        } else {
            hasBoundCameraOnce = true
            previewBlurView.isVisible = false
        }

        // Hide grid until preview is ready
        gridView.alpha = 0f

        // If the current camera doesn't support the selected camera mode, give up
        require(cameraConfiguration.camera.supportsCameraMode(cameraConfiguration.cameraMode)) {
            "Selected camera mode (${
                cameraConfiguration.cameraMode
            }) not supported by camera ${cameraConfiguration.camera.cameraId}"
        }

        // Make sure the extension mode is supported
        require(
            cameraConfiguration.camera.supportsExtensionMode(
                cameraConfiguration.extensionMode
            )
        ) {
            "Selected extension mode (${
                cameraConfiguration.extensionMode
            }) not supported by camera ${cameraConfiguration.camera.cameraId}"
        }

        // Initialize the use case we want and set its properties
        val cameraUseCases = when (cameraConfiguration) {
            is CameraConfiguration.Photo -> {
                require(
                    cameraConfiguration.photoCaptureMode != ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
                            || cameraConfiguration.camera.supportsZsl
                ) {
                    "Requested ZSL capture mode when the camera doesn't support it"
                }

                require(
                    cameraConfiguration.camera.supportedPhotoOutputFormats.contains(
                        cameraConfiguration.photoOutputFormat
                    )
                ) {
                    "Selected photo output format (${
                        cameraConfiguration.photoOutputFormat
                    } not supported by camera ${cameraConfiguration.camera.cameraId})"
                }

                viewModel.cameraController.imageCaptureMode = cameraConfiguration.photoCaptureMode

                viewModel.cameraController.imageOutputFormat = when (
                    cameraConfiguration.photoOutputFormat
                ) {
                    PhotoOutputFormat.JPEG -> ImageCapture.OUTPUT_FORMAT_JPEG
                    PhotoOutputFormat.JPEG_ULTRA_HDR -> ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR
                    PhotoOutputFormat.RAW -> ImageCapture.OUTPUT_FORMAT_RAW
                    PhotoOutputFormat.RAW_JPEG -> ImageCapture.OUTPUT_FORMAT_RAW_JPEG
                }

                // Always use native 4:3 for CameraX pipeline to leverage full sensor area
                val previewResSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(
                        AspectRatioStrategy(
                            AspectRatio.RATIO_4_3,
                            AspectRatioStrategy.FALLBACK_RULE_AUTO,
                        )
                    )
                    .build()
                viewModel.cameraController.previewResolutionSelector = previewResSelector

                val resBuilder = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(
                        AspectRatioStrategy(
                            AspectRatio.RATIO_4_3,
                            AspectRatioStrategy.FALLBACK_RULE_AUTO,
                        )
                    )

                val targetRes = currentCaptureResolution ?: currentTargetResolution
                if (targetRes != null) {
                    resBuilder.setResolutionStrategy(
                        ResolutionStrategy(
                            targetRes,
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                } else {
                    resBuilder.setAllowedResolutionMode(
                        if (cameraConfiguration.enableHighResolution) {
                            ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE
                        } else {
                            ResolutionSelector.PREFER_CAPTURE_RATE_OVER_HIGHER_RESOLUTION
                        }
                    )
                }

                val builtResSelector = resBuilder.build()
                viewModel.cameraController.imageCaptureResolutionSelector = builtResSelector

                viewModel.cameraController.setImageAnalysisAnalyzer(
                    viewModel.cameraExecutor, viewModel.qrImageAnalyzer
                )

                CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
            }

            is CameraConfiguration.Video -> {
                viewModel.cameraController.clearImageAnalysisAnalyzer()

                // Check whether or not the video quality is supported
                val videoQualityInfo = cameraConfiguration.camera.supportedVideoQualities[
                    cameraConfiguration.videoQuality
                ] ?: error("Video quality not supported by the camera")

                require(
                    videoQualityInfo.supportedFrameRates.contains(
                        cameraConfiguration.videoFrameRate
                    )
                ) {
                    "Video frame rate not supported with the requested video quality"
                }

                require(
                    videoQualityInfo.supportedDynamicRanges.contains(
                        cameraConfiguration.videoDynamicRange
                    )
                ) {
                    "Video dynamic range not supported with the requested video quality"
                }

                // Set the quality
                viewModel.cameraController.videoCaptureQualitySelector =
                    QualitySelector.from(cameraConfiguration.videoQuality)

                // Set the dynamic range
                viewModel.cameraController.videoCaptureDynamicRange =
                    cameraConfiguration.videoDynamicRange.dynamicRange

                // Set video mirror mode
                viewModel.cameraController.videoCaptureMirrorMode =
                    when (cameraConfiguration.videoMirrorMode) {
                        VideoMirrorMode.OFF -> MirrorMode.MIRROR_MODE_OFF
                        VideoMirrorMode.ON -> MirrorMode.MIRROR_MODE_ON
                        VideoMirrorMode.ON_FFC_ONLY -> when (
                            cameraConfiguration.camera.cameraFacing
                        ) {
                            CameraFacing.FRONT -> MirrorMode.MIRROR_MODE_ON
                            else -> MirrorMode.MIRROR_MODE_OFF
                        }
                    }

                CameraController.VIDEO_CAPTURE
            }

            is CameraConfiguration.Qr -> {
                viewModel.cameraController.setImageAnalysisAnalyzer(
                    viewModel.cameraExecutor, viewModel.qrImageAnalyzer
                )

                CameraController.IMAGE_ANALYSIS or CameraController.IMAGE_CAPTURE
            }
        }

        // Get the camera selector
        val cameraSelector = viewModel.getExtensionEnabledCameraSelector(
            cameraConfiguration.camera, cameraConfiguration.extensionMode
        )

        // Workaround: We cannot set flash mode to screen with a non front facing camera.
        // VM will set the correct value later on
        if (cameraConfiguration.camera.cameraFacing != CameraFacing.FRONT
            && viewModel.cameraController.flashMode == FlashMode.SCREEN
        ) {
            viewModel.cameraController.flashMode = FlashMode.OFF
        }

        // Bind use cases to camera
        viewModel.cameraController.cameraSelector = cameraSelector
        viewModel.cameraController.setEnabledUseCases(cameraUseCases)

        // Bind camera controller to lifecycle
        viewModel.cameraController.bindToLifecycle(this)

        // Wait for camera to be ready
        lifecycleScope.launch {
            viewModel.cameraController.initializationFuture.await()

            val camera2CameraControl = viewModel.cameraController.camera2CameraControl ?: run {
                Log.wtf(LOG_TAG, "Camera2CameraControl not available even with camera ready?")
                return@launch
            }

            val camera2Options = cameraConfiguration.camera2Options

            // Set Camera2 CaptureRequest options
            camera2CameraControl.setCaptureRequestOptions(CaptureRequestOptions.Builder()
                .setFrameRate(
                    when (cameraConfiguration) {
                        is CameraConfiguration.Video -> cameraConfiguration.videoFrameRate
                        else -> null
                    }
                )
                .setVideoStabilizationMode(
                    when (cameraConfiguration) {
                        is CameraConfiguration.Video -> when (
                            cameraConfiguration.enableVideoStabilization
                        ) {
                            true -> VideoStabilizationMode.getMode(cameraConfiguration.camera)
                            false -> null
                        }

                        else -> null
                    } ?: VideoStabilizationMode.OFF
                )
                .setEdgeMode(camera2Options.edgeMode)
                .setNoiseReductionMode(camera2Options.noiseReductionMode)
                .setShadingMode(camera2Options.shadingMode)
                .setColorCorrectionAberrationMode(camera2Options.colorCorrectionAberrationMode)
                .setDistortionCorrectionMode(camera2Options.distortionCorrectionMode)
                .setHotPixelMode(camera2Options.hotPixelMode)
                .build()
            )

            viewModel.cameraController.flashMode = viewModel.flashMode.value
        }

        // Restore settings that can be set on the fly
        viewModel.setVideoMicrophoneEnabled(viewModel.videoMicMode.value)

        // Reset exposure level
        viewModel.setExposureCompensationLevel(0.5f)
    }

    private fun updateGalleryButton(uri: Uri?, fromCapture: Boolean) {
        runOnUiThread {
            val keyguardLocked = keyguardManager.isKeyguardLocked

            galleryButtonIconImageView.setImageResource(
                when (keyguardLocked) {
                    true -> R.drawable.ic_lock
                    false -> R.drawable.ic_image
                }
            )

            if (keyguardLocked && !fromCapture) {
                return@runOnUiThread
            }

            galleryButtonUri = uri

            // When keyguard is unlocked, we want media from MediaStore, else we only trust the
            // ones coming from the capture
            uri?.also {
                galleryButtonPreviewImageView.load(uri) {
                    decoderFactory(VideoFrameDecoder.Factory())
                    crossfade(true)
                    scale(Scale.FILL)
                    size(75.px)
                    error(R.drawable.ic_image)
                    fallback(R.drawable.ic_image)
                    listener(
                        onCancel = {
                            galleryButtonPreviewImageView.isVisible = false
                            galleryButtonIconImageView.isVisible = true
                        },
                        onError = { _, _ ->
                            galleryButtonPreviewImageView.isVisible = false
                            galleryButtonIconImageView.isVisible = true
                        },
                        onSuccess = { _, _ ->
                            galleryButtonPreviewImageView.isVisible = true
                            galleryButtonIconImageView.isVisible = false
                        }
                    )
                }
            } ?: run {
                galleryButtonIconImageView.isVisible = true
                galleryButtonPreviewImageView.isVisible = false
            }
        }
    }

    private fun dismissKeyguardAndRun(runnable: () -> Unit) {
        if (!keyguardManager.isKeyguardLocked) {
            runnable()
            return
        }

        keyguardManager.requestDismissKeyguard(
            this,
            object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()
                    runnable()
                }
            }
        )
    }

    private fun openGallery() {
        lifecycleScope.launch {
            // secureMediaUris will be cleared if keyguard is unlocked
            // or none of the items were found.
            withContext(Dispatchers.IO) {
                updateSecureMediaUris(keyguardManager.isKeyguardLocked)
            }

            if (keyguardManager.isKeyguardLocked) {
                // In this state the only thing we can do is launch a secure review intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && secureMediaUris.isNotEmpty()
                ) {
                    val intent = Intent(
                        MediaStore.ACTION_REVIEW_SECURE, secureMediaUris.first()
                    ).apply {
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        if (secureMediaUris.size > 1) {
                            clipData = ClipData.newUri(
                                contentResolver, null, secureMediaUris[1]
                            ).apply {
                                for (i in 2 until secureMediaUris.size) {
                                    addItem(contentResolver, ClipData.Item(secureMediaUris[i]))
                                }
                            }
                        }
                    }
                    runCatching {
                        startActivity(intent)
                        return@launch
                    }
                }
            }

            galleryButtonUri?.also { uri ->
                // Try to open the Uri in the non secure gallery
                dismissKeyguardAndRun {
                    mutableListOf<String>().apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            add(MediaStore.ACTION_REVIEW)
                        }
                        add(Intent.ACTION_VIEW)
                    }.forEach {
                        val intent = Intent(it, uri).apply {
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        runCatching {
                            startActivity(intent)
                            return@dismissKeyguardAndRun
                        }
                    }
                }
            } ?: run {
                // If the Uri is null, attempt to launch non secure-gallery
                dismissKeyguardAndRun {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        type = "image/*"
                    }
                    runCatching {
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun openCapturePreview(uri: Uri, mediaType: MediaType) {
        runOnUiThread {
            capturePreviewLayout.updateSource(uri, mediaType)
            capturePreviewLayout.isVisible = true
        }
    }

    private fun openCapturePreview(photoInputStream: InputStream) {
        runOnUiThread {
            capturePreviewLayout.updateSource(photoInputStream)
            capturePreviewLayout.isVisible = true
        }
    }

    /**
     * When the user took a photo or a video and confirmed it, its URI gets sent back to the
     * app that sent the intent and closes the camera.
     */
    private fun sendIntentResultAndExit(input: Any) {
        // The user confirmed the choice
        var outputUri: Uri? = null
        if (intent.extras?.containsKey(MediaStore.EXTRA_OUTPUT) == true) {
            outputUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.extras?.getParcelable(MediaStore.EXTRA_OUTPUT, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.extras?.get(MediaStore.EXTRA_OUTPUT) as Uri
            }
        }

        outputUri?.let {
            try {
                contentResolver.openOutputStream(it, "wt").use { outputStream ->
                    when (input) {
                        is InputStream -> input.use {
                            input.copyTo(outputStream!!)
                        }

                        is Uri -> contentResolver.openInputStream(input).use { inputStream ->
                            inputStream!!.copyTo(outputStream!!)
                        }

                        else -> throw IllegalStateException("Input is not Uri or InputStream")
                    }
                }

                setResult(RESULT_OK)
            } catch (exc: FileNotFoundException) {
                Log.e(LOG_TAG, "Failed to open URI")
                setResult(RESULT_CANCELED)
            }
        } ?: setResult(RESULT_OK, Intent().apply {
            when (input) {
                is InputStream -> {
                    // No output URI provided, so return the photo inline as a downscaled Bitmap.
                    action = "inline-data"
                    val transform = ExifUtils.getTransform(input)
                    val bitmap = input.use { BitmapFactory.decodeStream(input) }
                    val scaledAndRotatedBitmap = bitmap.scale(
                        SINGLE_CAPTURE_INLINE_MAX_SIDE_LEN_PIXELS
                    ).transform(transform)
                    putExtra("data", scaledAndRotatedBitmap)
                }

                is Uri -> {
                    // We saved the media (video), so return the URI that we saved.
                    data = input
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(MediaStore.EXTRA_OUTPUT, input)
                }

                else -> throw IllegalStateException("Input is not Uri or InputStream")
            }
        })

        finish()
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun startTimerAndRun(runnable: () -> Unit) {
        // Allow forcing timer if requested by the assistant
        val timerModeSeconds =
            assistantIntent?.timerDurationSeconds ?: viewModel.timerMode.value.seconds

        if (timerModeSeconds <= 0 || !viewModel.canRestartCamera()) {
            runnable()
            return
        }

        val previousCameraState = viewModel.cameraState.value
        viewModel.cameraState.value = CameraState.COUNTDOWN

        countDownView.onPreviewAreaChanged(Rect().apply {
            viewFinder.getGlobalVisibleRect(this)
        })
        countDownView.startCountDown(timerModeSeconds) {
            viewModel.cameraState.value = previousCameraState
            runnable()
        }
    }

    /**
     * Show a toast warning the user that no camera is available and close the activity.
     */
    private fun noCamera() {
        Toast.makeText(
            this, R.string.error_no_cameras_available, Toast.LENGTH_LONG
        ).show()
        finish()
    }

    /**
     * Method called when a new media have been successfully captured and saved.
     * Keep track of media items captured while in a secure lockscreen state so that they
     * can be passed to the gallery for viewing without unlocking the device. However, if the
     * keyguard is no longer locked, clear any existing URIs, and do not add this one.
     */
    private fun onCapturedMedia(item: Uri?) {
        updateGalleryButton(item, true)

        if (!keyguardManager.isKeyguardLocked) {
            secureMediaUris.clear()
        } else {
            item?.let {
                secureMediaUris.addFirst(it)
            }
        }
    }

    /**
     * If keyguard is not locked, remove any URIs that were stored while in the secure camera state.
     * Otherwise, remove any URIs that no longer exist.
     */
    private fun updateSecureMediaUris(keyguardLocked: Boolean) {
        if (!keyguardLocked) {
            secureMediaUris.clear()
        } else {
            secureMediaUris.removeIf { !viewModel.fileExists(it) }
        }
    }

    private fun adjustZoomByStep(step: Float) {
        val currentLinear = viewModel.zoomState.value?.linearZoom ?: 0f
        val newLinear = (currentLinear + step).coerceIn(0f, 1f)
        viewModel.cameraController.setLinearZoom(newLinear)
        zoomLevel.progress = newLinear
        zoomLevel.isVisible = true
        handler.removeMessages(MSG_HIDE_ZOOM_SLIDER)
        handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ZOOM_SLIDER), 2500)
    }

    private fun adjustExposureByStep(step: Float) {
        viewModel.adjustExposureCompensationStep(step)
        exposureLevel.progress = viewModel.currentExposureLevel
        exposureLevel.isVisible = true
        handler.removeMessages(MSG_HIDE_EXPOSURE_SLIDER)
        handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_EXPOSURE_SLIDER), 2500)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL) {
            val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            if (abs(vScroll) > 0.001f) {
                val halfWidth = (window.decorView.width.takeIf { it > 0 } ?: 1080) / 2f
                val isLeftSide = event.x < halfWidth
                // Inverted vertical scroll: scroll UP (+step) = Zoom IN / Brighter, scroll DOWN (-step) = Zoom OUT / Darker
                val step = -vScroll * 0.05f
                Log.d(LOG_TAG, "dispatchGenericMotionEvent: x=${event.x}, vScroll=$vScroll, left=$isLeftSide")
                if (isLeftSide) {
                    adjustZoomByStep(step)
                } else {
                    adjustExposureByStep(-vScroll * 1.5f)
                }
                return true
            }
        }
        return super.dispatchGenericMotionEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
        if (abs(vScroll) > 0.001f) {
            val halfWidth = (window.decorView.width.takeIf { it > 0 } ?: 1080) / 2f
            val isLeftSide = event.x < halfWidth
            // Inverted vertical scroll: scroll UP (+step) = Zoom IN / Brighter, scroll DOWN (-step) = Zoom OUT / Darker
            val step = -vScroll * 0.05f
            Log.d(LOG_TAG, "onGenericMotionEvent: x=${event.x}, vScroll=$vScroll, left=$isLeftSide")
            if (isLeftSide) {
                adjustZoomByStep(step)
            } else {
                adjustExposureByStep(-vScroll * 1.5f)
            }
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onTrackballEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            val deltaY = event.y // Inverted direction
            if (abs(deltaY) > 0.001f) {
                val halfWidth = (window.decorView.width.takeIf { it > 0 } ?: 1080) / 2f
                val isLeftSide = event.x < halfWidth
                val step = deltaY * 0.05f
                Log.d(LOG_TAG, "onTrackballEvent: x=${event.x}, y=${event.y}, left=$isLeftSide")
                if (isLeftSide) {
                    adjustZoomByStep(step)
                } else {
                    adjustExposureByStep(deltaY * 1.5f)
                }
                return true
            }
        }
        return super.onTrackballEvent(event)
    }

    private fun handleHardwareKeyDown(
        keyCode: Int, event: KeyEvent?
    ): Boolean? {
        if (keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (viewModel.cameraMode.value == CameraMode.VIDEO
                && viewModel.isShutterButtonEnabled.value
                && event?.repeatCount == 0
            ) {
                shutterButton.performClick()
            }
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            adjustZoomByStep(0.05f)
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            adjustZoomByStep(-0.05f)
            return true
        }

        return HardwareKey.match(keyCode)?.let { (hardwareKey, tempIncrease) ->
            val increase = when (viewModel.getHardwareKeyInvert(hardwareKey)) {
                true -> !tempIncrease
                false -> tempIncrease
            }

            val gestureAction = viewModel.getHardwareKeyAction(hardwareKey)

            if (gestureAction.isTwoWayAction && !hardwareKey.isTwoWayKey) {
                Log.wtf(
                    LOG_TAG,
                    "${gestureAction.name} requires two-way key but ${hardwareKey.name} is not"
                )
                return@let true
            }

            when (gestureAction) {
                GestureAction.SHUTTER -> {
                    if (viewModel.cameraMode.value == CameraMode.VIDEO
                        && viewModel.isShutterButtonEnabled.value
                        && event?.repeatCount == 0
                    ) {
                        shutterButton.performClick()
                    }
                    true
                }

                GestureAction.FOCUS -> {
                    if (event?.repeatCount == 0) {
                        viewFinderTouchEvent = null
                        viewFinder.performClick()
                    }
                    true
                }

                GestureAction.MIC_MUTE -> {
                    if (viewModel.cameraMode.value == CameraMode.VIDEO && micButton.isEnabled &&
                        event?.repeatCount == 0
                    ) {
                        viewModel.toggleVideoMicrophoneEnabled()
                    }
                    true
                }

                GestureAction.ZOOM -> {
                    when (increase) {
                        true -> viewModel.zoomIn()
                        false -> viewModel.zoomOut()
                    }
                    true
                }

                GestureAction.DEFAULT -> {
                    if (hardwareKey.supportsDefault) {
                        super.onKeyDown(keyCode, event)
                    } else {
                        Log.wtf(
                            LOG_TAG,
                            "Got GestureAction.DEFAULT for ${hardwareKey.name} which doesn't support it"
                        )
                        true
                    }
                }

                GestureAction.NOTHING -> {
                    // Do nothing
                    true
                }
            }
        }
    }

    private fun handleHardwareKeyUp(
        keyCode: Int, event: KeyEvent?
    ): Boolean? {
        if (keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (viewModel.isShutterButtonEnabled.value) {
                shutterButton.performClick()
            }
            return true
        }

        return HardwareKey.match(keyCode)?.let { (hardwareKey, _) ->
            val gestureAction = viewModel.getHardwareKeyAction(hardwareKey)

            if (gestureAction.isTwoWayAction && !hardwareKey.isTwoWayKey) {
                Log.wtf(
                    LOG_TAG,
                    "${gestureAction.name} requires two-way key but ${hardwareKey.name} is not"
                )
                return@let true
            }

            when (gestureAction) {
                GestureAction.SHUTTER -> {
                    if (viewModel.isShutterButtonEnabled.value) {
                        shutterButton.performClick()
                    }
                    true
                }

                GestureAction.FOCUS -> {
                    true
                }

                GestureAction.MIC_MUTE -> {
                    true
                }

                GestureAction.ZOOM -> {
                    true
                }

                GestureAction.DEFAULT -> {
                    if (hardwareKey.supportsDefault) {
                        super.onKeyDown(keyCode, event)
                    } else {
                        Log.wtf(
                            LOG_TAG,
                            "Got GestureAction.DEFAULT for ${hardwareKey.name} which doesn't support it"
                        )
                        true
                    }
                }

                GestureAction.NOTHING -> {
                    // Do nothing
                    true
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = CameraActivity::class.simpleName!!

        private const val MSG_HIDE_ZOOM_SLIDER = 0
        private const val MSG_HIDE_FOCUS_RING = 1
        private const val MSG_HIDE_EXPOSURE_SLIDER = 2
        private const val MSG_ON_PINCH_TO_ZOOM = 3

        // We need to return something small enough so as not to overwhelm Binder. 1MB is the
        // per-process limit across all transactions. Camera2 sets a max pixel count of 51200.
        // We set a max side length of 256, for a max pixel count of 65536. Even at 4 bytes per
        // pixel, this is only 256K, well within the limits. (Note: It's not clear if any modern
        // app expects a photo to be returned inline, rather than providing an output URI.)
        // https://developer.android.com/guide/components/activities/parcelables-and-bundles#sdbp
        private const val SINGLE_CAPTURE_INLINE_MAX_SIDE_LEN_PIXELS = 256

        private val EXPOSURE_LEVEL_FORMATTER = DecimalFormat("+#;-#")
    }
}
