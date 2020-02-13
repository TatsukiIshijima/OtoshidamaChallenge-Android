package com.io.tatsuki.otoshidamachallenge

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Rational
import android.view.*
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.camera_fragment.*

class CameraFragment : Fragment() {

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
        private const val WIDTH_CROP_PERCENT = 8
        private const val HEIGHT_CROP_PERCENT = 74
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private lateinit var viewModel: CameraViewModel

    override fun onResume() {
        super.onResume()
        if (!PermissionFragment.hasPermissions(requireContext())) {
            // TODO:ダイアログ表示してアプリ終了
            requireActivity().finish()
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

        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)

        viewModel.isReadyEvent
            .observe(viewLifecycleOwner, Observer { event ->
                event.getContentIfNotHandled()?.let {

                    if (it) {
                        viewFinder.post {
                            startCamera()
                        }
                        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                            updateTransform()
                        }
                        overlay.apply {
                            setZOrderOnTop(true)
                            holder.setFormat(PixelFormat.TRANSPARENT)
                            holder.addCallback(object : SurfaceHolder.Callback {

                                override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                                    // noop
                                }

                                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                                    // noop
                                }

                                override fun surfaceCreated(holder: SurfaceHolder?) {
                                    holder?.let { drawOverlay(it) }
                                }
                            })
                        }
                    }
                }
        })

        viewModel.resultText
            .observe(viewLifecycleOwner, Observer {
                if (!it.isNullOrEmpty()) {
                    Log.d(TAG, it)
                }
            })

        viewModel.checkTrainedData(requireContext())
    }

    private fun startCamera() {

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {

            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            val analyzerThread = HandlerThread(
                "TextAnalysis"
            ).apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
        }.build()

        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            analyzer = TextAnalyzer(
                viewModel.resultText,
                WIDTH_CROP_PERCENT,
                HEIGHT_CROP_PERCENT
            )
        }

        //CameraX.bindToLifecycle(viewLifecycleOwner, preview, analyzerUseCase)
        // 以下実験用
        CameraX.bindToLifecycle(viewLifecycleOwner, preview)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        val rotationDegrees = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        viewFinder.setTransform(matrix)
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
        val rectLeft = surfaceWidth * WIDTH_CROP_PERCENT / 2 / 100f
        val rectRight = surfaceWidth * (1 - WIDTH_CROP_PERCENT / 2 / 100f)
        val rectBottom = surfaceHeight * (1 - HEIGHT_CROP_PERCENT / 2 / 100f)
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, rectPaint
        )
        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, outlinePaint
        )
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
