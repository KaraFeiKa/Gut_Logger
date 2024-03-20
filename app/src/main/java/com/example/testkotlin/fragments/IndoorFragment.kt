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
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.testkotlin.databinding.FragmentIndoorBinding
import android.net.Uri
import android.view.MotionEvent

class IndoorFragment : Fragment() {


    private var bitmap: Bitmap? = null
    private val points = mutableListOf<Pair<Float, Float>>()
    private lateinit var binding: FragmentIndoorBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIndoorBinding.inflate(inflater, container, false)



        binding.buttForIndoor.Load.setOnClickListener {
            openGallery()
        }

        drawLine()

        binding.buttForIndoor.ClearAll.setOnClickListener {
            cleanImage()
        }
        binding.buttForIndoor.ClearLast.setOnClickListener {
            deleteLastPoint()
        }

        return binding.root
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!
            bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedImageUri)
            binding.imageView.setImageBitmap(bitmap)
            points.clear()
        }
    }

    private fun drawLine(){
        binding.imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                points.add(Pair(event.x, event.y))
                drawPoints()
            }
            true
        }
    }

    private fun drawPoints() {

        val tempBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(tempBitmap!!)
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second, paint)
        }

        binding.imageView.setImageBitmap(tempBitmap)
    }

    private fun cleanImage() {
        binding.imageView.setImageBitmap(bitmap)
    }

    private fun deleteLastPoint() {
        if (points.isNotEmpty()) {
            points.removeAt(points.size - 1)
            drawPoints()
        }
    }
    companion object {
        @JvmStatic
        fun newInstance() = IndoorFragment()
    }
}

