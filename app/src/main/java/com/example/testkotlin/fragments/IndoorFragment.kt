package com.example.testkotlin.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.testkotlin.databinding.FragmentIndoorBinding
import android.net.Uri
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testkotlin.Info.ServiceBack
import com.example.testkotlin.Info.SignalModel
import com.example.testkotlin.R
import com.ortiz.touchview.TouchImageView

class IndoorFragment : Fragment() {

    var rssi: Int = 0
    var rsrp: Int = 0
    var rsrq: Int = 0
    var snr: Int = 0
    var cqi: Int = 0

    private lateinit var imageView: TouchImageView
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var binding: FragmentIndoorBinding
    private val points = mutableListOf<Pair<Float, Float>>()

    private var drawingMode = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIndoorBinding.inflate(inflater, container, false)
        imageView = binding.imageView
        imageView.setZoom(1f)

        imageView.setOnTouchListener { _, event ->
            if (drawingMode) {
                handleTouch(event)
            } else {
                false
            }
        }

        binding.buttForIndoor.Load.setOnClickListener {
            loadImageFromGallery()
        }

        binding.buttForIndoor.ClearAll.setOnClickListener {
            cleanImage()
        }

        binding.buttForIndoor.ClearLast.setOnClickListener {
            deleteLastPoint()
        }

        binding.buttForIndoor.Save.setOnClickListener {
            saveImageToGallery()
        }

        binding.buttForIndoor.btnToggleMode.setOnClickListener {
            drawingMode = !drawingMode
            if (drawingMode) {
                // Режим рисования
                Toast.makeText(requireContext(), "Режим рисования включен", Toast.LENGTH_SHORT).show()
            } else {
                // Режим перемещения/приближения
                Toast.makeText(requireContext(), "Режим рисования выключен", Toast.LENGTH_SHORT).show()
            }
        }

        bitmap = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerLocReceiver()
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val touchPoint = floatArrayOf(event.x, event.y)
        val inverted = Matrix()
        imageView.getImageMatrix().invert(inverted)
        inverted.mapPoints(touchPoint)

        val bitmapX = touchPoint[0]
        val bitmapY = touchPoint[1]

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                points.add(Pair(bitmapX, bitmapY))
                drawPoints()
            }
            MotionEvent.ACTION_MOVE -> {
                // Not needed for drawing points and lines
            }
        }

        return true
    }

    private fun loadImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri: Uri = data.data!!
            try {
                val selectedBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                // Resize the selected bitmap to fit the canvas size
                val scaledBitmap = Bitmap.createScaledBitmap(selectedBitmap, bitmap.width, bitmap.height, true)
                // Draw the selected bitmap on the canvas
                canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        points.clear()
    }

    private fun drawPoints() {
        val tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tempCanvas = Canvas(tempBitmap)
        drawPoints(tempCanvas)
        drawLines(tempCanvas)
        imageView.setImageBitmap(tempBitmap)
    }
//    (rsrp >= -80)
//    (rsrp <= -80 && rsrp >= -90)
//    (rsrp <= -90 && rsrp >= -100)
//    (rsrp <= -100)
    private fun drawPoints(canvas: Canvas) {
        val paint = Paint().apply {

            style = Paint.Style.FILL
        }
        for (point in points) {
            canvas.drawCircle(point.first, point.second, 10f, paint)
        }
    }

    private fun drawLines(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second, paint)
        }
    }

    private fun cleanImage() {
        points.clear()
        binding.imageView.setImageBitmap(bitmap)
    }

    private fun deleteLastPoint() {
        if (points.isNotEmpty()) {
            points.removeAt(points.size - 1)
            drawPoints()
        }
    }

    private fun saveImageToGallery() {
        val tempBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true) ?: return
        val canvas = Canvas(tempBitmap)
        drawPoints(canvas)
        drawLines(canvas)

        MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            tempBitmap,
            "IndoorImage_${System.currentTimeMillis()}",
            "Image with lines and points"
        )

        Toast.makeText(requireContext(), "Сохранено в галлерею", Toast.LENGTH_SHORT).show()
    }
    private val receiverSignal = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, q: Intent?) {
            if (q?.action == ServiceBack.SIGNAL_MODLE_INTENT){
                val signalModel = q.getSerializableExtra(ServiceBack.SIGNAL_MODLE_INTENT) as SignalModel

                if (signalModel.rssi != Int.MAX_VALUE && signalModel.rssi >= -140 && signalModel.rssi <= -43){
                    rssi = signalModel.rssi
                }else{
                    rssi = -0
                }
                if (signalModel.rsrp != Int.MAX_VALUE && signalModel.rsrp < 0){
                    rsrp = signalModel.rsrp

                }else{
                    rsrp = -0

                }
                if (signalModel.rsrq != Int.MAX_VALUE){
                    rsrq = signalModel.rsrq

                }else{
                    rsrq = -0
                }
                if (signalModel.snr != Int.MAX_VALUE){
                    snr = signalModel.snr

                }else{
                    snr = -0
                }
                if (signalModel.cqi != Int.MAX_VALUE){
                    cqi = signalModel.cqi
                }else{
                    cqi = -0
                }
            }
        }
    }

    private fun registerLocReceiver(){
        val signalFilter = IntentFilter(ServiceBack.SIGNAL_MODLE_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverSignal, signalFilter)


    }

    companion object {
        @JvmStatic
        fun newInstance() = IndoorFragment()
        private const val PICK_IMAGE_REQUEST = 1

    }
}