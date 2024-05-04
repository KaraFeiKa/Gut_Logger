package com.example.testkotlin.fragments

import android.app.Activity
import android.app.AlertDialog
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
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testkotlin.Info.ServiceBack
import com.example.testkotlin.Info.SignalModel
import com.example.testkotlin.MainViewModel
import com.example.testkotlin.R
import com.ortiz.touchview.TouchImageView

class IndoorFragment : Fragment() {

    private lateinit var binding: FragmentIndoorBinding
    private lateinit var drawingView: DrawingView
    private val model : MainViewModel by activityViewModels()
    var FUL: Float = 0.0F
    var FDL: Float = 0.0F
    var lat: Double  = 0.0
    var lon: Double  = 0.0
    var Operator: CharSequence? = ""
    var rrc: String = ""
    var mnc: String = ""
    var mcc: String = ""
    var ULSpeed: String = ""
    var DLSpeed: String = ""
    var NameR: String = ""
    var Mode: String = ""
    var ci: Int = 0
    var band: Any = intArrayOf()
    var eNB: Int = 0
    var Earfcn: Int = 0
    var pci: Int = 0
    var tac: Int = 0
    var rssi: Int = 0
    var rsrp: Int = 0
    var rsrq: Int = 0
    var snr: Int = 0
    var cqi: Int = 0
    var ta: Int = 0
    var BandPlus: Int = 0
    var rnc: Int = 0
    var psc: Int = 0
    var ber: Int = 0
    var bsic: Int = 0
    var Arfcn: Int = 0
    var Uarfcn: Int = 0
    var EcNo: Int = 0
    var bandwidnths = intArrayOf(0)
    var convertedBands  = intArrayOf(0)
    var net: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIndoorBinding.inflate(inflater, container, false)
        drawingView = binding.drawingView
        binding.buttons.Load.setOnClickListener {
            chooseImageFromGallery()
        }
        binding.buttons.ClearAll.setOnClickListener {
            cleanImage()
        }

        binding.buttons.ClearLast.setOnClickListener {
            deleteLastPoint()
        }

        binding.buttons.Save.setOnClickListener {
            saveImageToGallery()
        }

        binding.zoomInButton.setOnClickListener {
            drawingView.zoomIn()
        }

        binding.zoomOutButton.setOnClickListener {
            drawingView.zoomOut()
        }

        binding.buttons.toggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Включаем режим рисования
                drawingView.isDrawingMode = true
            } else {
                // Включаем режим перемещения
                drawingView.isDrawingMode = false
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signalUpdate()
    }

    private fun saveImageToGallery() {
        val originalBitmap = drawingView.imageBitmap ?: return
        // Сохраняем результат
        val resultBitmap = drawingView.saveResult(originalBitmap)
        if (resultBitmap != null) {
            // Сохраняем изображение в галерее устройства
            MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                resultBitmap,
                "drawing_result_${System.currentTimeMillis()}",
                "Drawing Result"
            )
            Toast.makeText(requireContext(), "Результат сохранен", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка сохранения результата", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cleanImage() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Вы точно хотите удалить все точки?")
            .setPositiveButton("Да") { dialog, _ ->
                drawingView.clearAllPoints()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_FROM_GALLERY)
    }

    private fun deleteLastPoint(){
        drawingView.undoLastPoint()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            drawingView.setImageUri(imageUri)
        }
    }

    private fun signalUpdate() {
        // Остальной код

        model.signalUpdate.observe(viewLifecycleOwner) { signalModel ->
            // Обновляем значение rsrp
            rsrp = if (signalModel.rsrp != Int.MAX_VALUE && signalModel.rsrp < 0) {
                signalModel.rsrp

            } else {
                -401 // Значение по умолчанию, если rsrp не в диапазоне
            }
            rssi = if (signalModel.rssi!= Int.MAX_VALUE && signalModel.rssi >= -140 && signalModel.rssi <= -43){
                signalModel.rssi
            }else{
                -401 // Значение по умолчанию, если rsrp не в диапазоне
            }
            rsrq = if (signalModel.rsrq!= Int.MAX_VALUE){
                signalModel.rsrq
            }else{
                -401 // Значение по умолчанию, если rsrp не в диапазоне
            }
            snr = if (signalModel.snr!= Int.MAX_VALUE ){
                signalModel.snr
            }else{
                -401 // Значение по умолчанию, если rsrp не в диапазоне
            }

            binding.infoForIndoor.rerRsrp.text = "RSRP: $rsrp дБм"
            binding.infoForIndoor.resRssi.text = "RSSI: $rssi дБм"
            binding.infoForIndoor.resRsrq.text = "RSRQ: $rsrq дБ"
            binding.infoForIndoor.resSnr.text = "SNR: $snr дБ"
            binding.drawingView.rsrp = rsrp
//            binding.drawingView.rssi = rssi
        }
    }



    companion object {
        @JvmStatic
        fun newInstance() = IndoorFragment()
        private const val REQUEST_IMAGE_FROM_GALLERY = 100
    }
}