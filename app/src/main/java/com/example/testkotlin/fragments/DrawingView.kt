package com.example.testkotlin.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import java.io.IOException

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var imageBitmap: Bitmap? = null
    val points = mutableListOf<PointF>()

    private val originalPoints = mutableListOf<PointF>()
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    var isDrawingMode: Boolean = false

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            lastTouchX = e.x
            lastTouchY = e.y
            return true
        }
    })

    private var _rsrp: Int = 0
    var rsrp: Int
        get() = _rsrp
        set(value) {
            _rsrp = value
        }
//    private var _rssi: Int = 0
//    var rssi: Int
//        get() = _rssi
//        set(value) {
//            rssi = value
//        }

    private val pointColors = mutableListOf<Int>()

    init {
        // Копируем исходные точки в список originalPoints
        originalPoints.addAll(points)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Применяем масштабирование и смещение к canvas
        canvas.save()
        val matrix = Matrix()
        matrix.setScale(scale, scale, lastTouchX, lastTouchY)
        matrix.postTranslate(offsetX, offsetY)
        canvas.concat(matrix)


        Log.d("ViewScale", "Scale: $scale, OffsetX: $offsetX, OffsetY: $offsetY")
        // Рисуем изображение
        imageBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // Рисуем оригинальные точки и линии поверх изображения
        drawPoints(canvas)
        drawLines(canvas)

        // Восстанавливаем исходное состояние canvas
        canvas.restore()
    }

    private val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val linePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    fun zoomIn() {
        scale *= SCALE_FACTOR
        invalidate()
    }

    fun zoomOut() {
        val newScale = scale / SCALE_FACTOR
        if (newScale >= MIN_SCALE) {
            scale = newScale
            invalidate()
        }
    }


    private fun drawPoints(canvas: Canvas) {
        for (index in points.indices) {
            val point = points[index]
            val pointColor = pointColors[index]

            // Установка цвета кисти
            pointPaint.color = pointColor

            // Рисуем квадрат
            val squareSize = 40f // Размер квадрата
            val squarePaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(point.x - squareSize, point.y - squareSize / 2, point.x, point.y + squareSize / 2, squarePaint)

            // Рисуем номер точки
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 30f // Размер текста
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText((index + 1).toString(), point.x - squareSize / 2, point.y + 10f, textPaint)

            // Рисуем саму точку
            canvas.drawCircle(point.x, point.y, 10f, pointPaint)
        }
    }

    private fun drawLines(canvas: Canvas) {
        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, linePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        val invertedMatrix = Matrix()
        val matrix = Matrix()
        matrix.setScale(scale, scale, lastTouchX, lastTouchY)

        matrix.postTranslate(offsetX, offsetY)
        matrix.invert(invertedMatrix)

        val pts = floatArrayOf(event.x, event.y)
        invertedMatrix.mapPoints(pts)

        val x = pts[0]
        val y = pts[1]

        if (isDrawingMode) {
            // Если включен режим рисования, обрабатываем события касания для рисования
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Действие при касании экрана
                    lastTouchX = x
                    lastTouchY = y
                    val pointColor = when {
                        rsrp >= -70 -> Color.rgb(0,255,0)
                        rsrp >= -80 -> Color.rgb(0, 128, 0)
                        rsrp >= -90 -> Color.rgb(255, 255, 0)
                        rsrp >= -100 -> Color.rgb(255, 0, 0)
                        else -> Color.rgb(128, 0, 0)
                    }
                    pointColors.add(pointColor)
                    points.add(PointF(x, y))
                    invalidate()
                    Log.d("TouchEvent", "ACTION_DOWN: X=$x, Y=$y")
                }
            }
        } else {
            // Если включен режим перемещения, обрабатываем события касания для перемещения
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Действие при касании экрана
                    lastTouchX = x
                    lastTouchY = y
                }
                MotionEvent.ACTION_MOVE -> {
                    // Действие при перемещении пальца по экрану
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY
                    offsetX += dx
                    offsetY += dy
                    invalidate()
                    lastTouchX = x
                    lastTouchY = y
                }
            }
        }

        return true
    }

    fun setImageUri(uri: Uri?) {
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                imageBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                // Очищаем список точек
                points.clear()
                pointColors.clear()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun undoLastPoint() {
        if (points.isNotEmpty()) {
            points.removeAt(points.size - 1)
            pointColors.removeAt(pointColors.size - 1)
            invalidate()
        }
    }

    fun clearAllPoints() {
        points.clear()
        pointColors.clear()
        invalidate()
    }

    fun saveResult(originalBitmap: Bitmap): Bitmap? {
        // Создаем новый Bitmap с теми же размерами, что и исходное изображение
        val resultBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        // Создаем Canvas для рисования на Bitmap
        val canvas = Canvas(resultBitmap)

        // Рисуем исходное изображение
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        // Рисуем оригинальные точки и линии поверх изображения
        drawPoints(canvas)
        drawLines(canvas)

        // Возвращаем Bitmap с нарисованным содержимым
        return resultBitmap
    }

    companion object {
        private const val SCALE_FACTOR = 1f
        private const val MIN_SCALE = 1
    }
}
