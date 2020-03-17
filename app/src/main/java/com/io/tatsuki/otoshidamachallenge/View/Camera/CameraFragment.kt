package com.io.tatsuki.otoshidamachallenge.View.Camera

import android.content.Context
import android.graphics.*
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.io.tatsuki.otoshidamachallenge.DI.AppContainer
import com.io.tatsuki.otoshidamachallenge.DI.CameraContainer
import com.io.tatsuki.otoshidamachallenge.MatchingRank
import com.io.tatsuki.otoshidamachallenge.OtoshidamaChallengeApplication
import com.io.tatsuki.otoshidamachallenge.R
import com.io.tatsuki.otoshidamachallenge.TextAnalyzer
import com.io.tatsuki.otoshidamachallenge.View.Permission.PermissionFragment
import kotlinx.android.synthetic.main.camera_fragment.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraFragment : Fragment() {

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
        private const val WIDTH_CROP_PERCENT = 65
        private const val HEIGHT_CROP_PERCENT = 74
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private lateinit var appContainer: AppContainer
    private lateinit var viewModel: CameraViewModel
    private lateinit var cameraExecutor: ExecutorService

    private var displayId: Int = -1
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var preview: Preview? = null

    private val lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Log.d(TAG, "Rotaion changed ${view.display.rotation}")
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PermissionFragment.hasPermissions(
                requireContext()
            )
        ) {
            findNavController().navigate(R.id.goToPermissionFromCamera)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!PermissionFragment.hasPermissions(
                requireContext()
            )
        ) {
            MaterialAlertDialogBuilder(requireContext())
                .setCancelable(false)
                .setTitle(R.string.camera_permission_denied_title)
                .setMessage(R.string.camera_permission_denied_message)
                .setPositiveButton(R.string.alert_dialog_positive_button_title) { _, _ ->
                    requireActivity().finish()
                }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appContainer = (requireActivity().application as OtoshidamaChallengeApplication).appContainer
        appContainer.cameraContainer = CameraContainer(appContainer.lotteryNumbersRepository)
        val cameraContainer =
            appContainer.cameraContainer ?: throw NullPointerException("CameraContainer is null.")
        viewModel = cameraContainer.cameraViewModelFactory.create()
        cameraExecutor = Executors.newSingleThreadExecutor()
        displayManager.registerDisplayListener(displayListener, null)

        overlay.apply {
            setZOrderOnTop(true)
            holder.setFormat(PixelFormat.TRANSPARENT)
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) = Unit
                override fun surfaceDestroyed(holder: SurfaceHolder?) = Unit
                override fun surfaceCreated(holder: SurfaceHolder?) {
                    holder?.let { drawOverlay(it) }
                }
            })
        }

        viewModel.isCompletedGetLotteryNumbersData.observe(
            viewLifecycleOwner,
            Observer {
                if (it) {
                    viewFinder.post {
                        displayId = viewFinder.display.displayId
                        startCamera()
                    }
                }
            }
        )

        viewModel.combinedAnalyzeResult.observe(
            viewLifecycleOwner,
            Observer {
                if (!it.isNullOrEmpty()) {
                    recognitionResult.text = it
                    viewModel.matchLotteryNumbers(it)
                }
            }
        )

        viewModel.matchingRankResult.observe(
            viewLifecycleOwner,
            Observer {
                when(it) {
                    MatchingRank.FIRST -> matchingRank.text = getString(R.string.hit_first_class)
                    MatchingRank.SECOND -> matchingRank.text = getString(R.string.hit_second_class)
                    MatchingRank.THIRD -> matchingRank.text = getString(R.string.hit_third_class)
                    MatchingRank.SPECIAL_PRIMARY -> matchingRank.text = getString(R.string.hit_special_primary_class)
                    MatchingRank.SPECIAL_SECONDARY -> matchingRank.text = getString(R.string.hit_special_secondary_class)
                    MatchingRank.NONE -> matchingRank.text = ""
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        viewModel.getLotteryNumbers()
    }

    override fun onDestroyView() {
        appContainer.cameraContainer = null
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
        super.onDestroyView()
    }

    private fun startCamera() {

        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(TAG, "Preview aspect ratio: ${screenAspectRatio}")

        val rotation = viewFinder.display.rotation

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            preview?.setSurfaceProvider(viewFinder.previewSurfaceProvider)

            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        TextAnalyzer(
                            viewModel.classNumberAnalyzeResult,
                            viewModel.lotteryNumberAnalyzeResult,
                            WIDTH_CROP_PERCENT,
                            HEIGHT_CROP_PERCENT
                        )
                    )
                }

            cameraProvider.unbindAll()

            try {
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return RATIO_4_3_VALUE.toInt()
        }
        return RATIO_16_9_VALUE.toInt()
    }

    private fun drawOverlay(holder: SurfaceHolder) {
        val canvas = holder.lockCanvas()
        val bgPaint = Paint().apply {
            alpha = 140
        }
        canvas.drawPaint(bgPaint)
        val rectPaint = Paint()
        rectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = Color.WHITE
        val outlinePaint = Paint()
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.color = Color.WHITE
        outlinePaint.strokeWidth = 4f
        val surfaceWidth = holder.surfaceFrame.width()
        val surfaceHeight = holder.surfaceFrame.height()

        val cornerRadius = 25f
        // Set rect centered in frame
        val rectTop = surfaceHeight * HEIGHT_CROP_PERCENT / 2 / 100f
        val rectBottom = surfaceHeight * (1 - HEIGHT_CROP_PERCENT / 2 / 100f)
        val classNumberRectLeft = (surfaceWidth * WIDTH_CROP_PERCENT / 2 / 100f) - (surfaceWidth / 4)
        val classNumberRectRight = (surfaceWidth * (1 - WIDTH_CROP_PERCENT / 2 / 100f)) - (surfaceWidth / 4)
        val classNumberRect =
            RectF(classNumberRectLeft, rectTop, classNumberRectRight, rectBottom)
        val lotteryNumberRectLeft = (surfaceWidth * WIDTH_CROP_PERCENT / 2 / 100f) + (surfaceWidth / 4)
        val lotteryNumberRectRight = (surfaceWidth * (1 - WIDTH_CROP_PERCENT / 2 / 100f)) + (surfaceWidth / 4)
        val lotteryNumberRect =
            RectF(lotteryNumberRectLeft, rectTop, lotteryNumberRectRight, rectBottom)

        canvas.drawRoundRect(classNumberRect, cornerRadius, cornerRadius, rectPaint)
        canvas.drawRoundRect(classNumberRect, cornerRadius, cornerRadius, outlinePaint)
        canvas.drawRoundRect(lotteryNumberRect, cornerRadius, cornerRadius, rectPaint)
        canvas.drawRoundRect(lotteryNumberRect, cornerRadius, cornerRadius, outlinePaint)

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 50F

        val overlayText = getString(R.string.overlay_help)
        val textBounds = Rect()
        textPaint.getTextBounds(overlayText, 0, overlayText.length, textBounds)
        val textX = (surfaceWidth - textBounds.width()) / 2f
        val textY = rectBottom + textBounds.height() + 15f // put text below rect and 15f padding
        canvas.drawText(getString(R.string.overlay_help), textX, textY, textPaint)
        holder.unlockCanvasAndPost(canvas)
    }
}
