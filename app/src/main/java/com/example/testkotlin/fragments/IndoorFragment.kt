package com.example.testkotlin.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

class IndoorFragment : Fragment() {

    private var bitmap: Bitmap? = null
    private val points = mutableListOf<Pair<Float, Float>>()
    private lateinit var binding: FragmentIndoorBinding
    private var originalBitmap: Bitmap? = null

    private var previousX = 0f
    private var previousY = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIndoorBinding.inflate(inflater, container, false)

        binding.buttForIndoor.Load.setOnClickListener {
            openGallery()
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

        drawLine()

        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data ?: return
            originalBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedImageUri)
            bitmap = originalBitmap?.copy(Bitmap.Config.ARGB_8888, true)
            binding.imageView.setImageBitmap(bitmap)
            points.clear()
        }
    }

    private fun drawLine() {
        binding.imageView.setOnTouchListener { _, event ->
            if (bitmap != null) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        previousX = event.x
                        previousY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.x - previousX
                        val dy = event.y - previousY
                        moveImage(dx, dy)
                        previousX = event.x
                        previousY = event.y
                    }
                    MotionEvent.ACTION_UP -> {
                        val imageViewWidth = binding.imageView.width
                        val imageViewHeight = binding.imageView.height
                        val bitmapWidth = bitmap?.width ?: 1
                        val bitmapHeight = bitmap?.height ?: 1

                        val scaleX = event.x * (bitmapWidth.toFloat() / imageViewWidth.toFloat()) / binding.imageView.scaleX
                        val scaleY = event.y * (bitmapHeight.toFloat() / imageViewHeight.toFloat()) / binding.imageView.scaleY

                        points.add(Pair(scaleX, scaleY))
                        drawPoints()
                    }
                }
            }
            true
        }
    }

    private fun moveImage(dx: Float, dy: Float) {
        binding.imageView.translationX += dx
        binding.imageView.translationY += dy
    }

    private fun drawPoints() {
        val tempBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true) ?: return
        val canvas = Canvas(tempBitmap)
        drawPoints(canvas)
        drawLines(canvas)
        binding.imageView.setImageBitmap(tempBitmap)
    }

    private fun drawPoints(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        for (point in points) {
            canvas.drawCircle(point.first, point.second, 10f, paint)
        }
    }

    private fun drawLines(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second, paint)
        }
    }

    private fun cleanImage() {
        points.clear()
        bitmap = originalBitmap?.copy(Bitmap.Config.ARGB_8888, true)
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

    companion object {
        @JvmStatic
        fun newInstance() = IndoorFragment()
    }
}

