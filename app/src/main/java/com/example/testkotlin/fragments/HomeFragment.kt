package com.example.testkotlin.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testkotlin.Info.BsInfoModel
import com.example.testkotlin.Info.LocationModel
import com.example.testkotlin.Info.ServiceBack
import com.example.testkotlin.Info.SignalModel
import com.example.testkotlin.Info.SpeedModel
import com.example.testkotlin.databinding.FragmentHomeBinding
import com.example.testkotlin.utils.checkPermission

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissions()
        registerLocReceiver()




    }

    private fun workSersive(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, ServiceBack::class.java))
        } else {
            activity?.startService(Intent(activity, ServiceBack::class.java))
        }
    }


    private fun registerPermissions(){

        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            if(it[Manifest.permission.ACCESS_FINE_LOCATION]  == true && it[Manifest.permission.READ_PHONE_STATE]  == true && it[Manifest.permission.POST_NOTIFICATIONS]  == true && it[Manifest.permission.ACCESS_COARSE_LOCATION]  == true)  {

            } else {
                Toast.makeText(activity, "Вы не дали разрешения!", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun checkPermission() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.READ_PHONE_STATE)
            &&checkPermission(Manifest.permission.POST_NOTIFICATIONS)


        ) {
            workSersive()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            )
        }
    }


    private fun checkLocationEnabled(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!isEnabled){
            Toast.makeText(activity, "GPS выключен!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("life", "Destroy")

    }


    private val receiverLoc = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, i: Intent?) {
            if (i?.action == ServiceBack.LOC_MODLE_INTENT){
                val locModel = i.getSerializableExtra(ServiceBack.LOC_MODLE_INTENT) as LocationModel
                Log.d("Check", locModel.lat.toString())
                binding.gpsInfo.resLat.text = locModel.lat.toString()
                binding.gpsInfo.resLon.text = locModel.lon.toString()
            }
        }
    }

    private val receiverSpeed = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, s: Intent?) {
            if (s?.action == ServiceBack.Speed_MODLE_INTENT){
                val speedModel = s.getSerializableExtra(ServiceBack.Speed_MODLE_INTENT) as SpeedModel
                Log.d("Check", speedModel.toString())
                binding.speed.speedUL.text= speedModel.UL
                binding.speed.speedDL.text = speedModel.DL
            }
        }
    }

    private val receiverBS = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, w: Intent?) {
            if (w?.action == ServiceBack.BS_MODLE_INTENT){
                val bsInfoModel = w.getSerializableExtra(ServiceBack.BS_MODLE_INTENT) as BsInfoModel
                binding.bsInfo.resMCC.text = bsInfoModel.mcc
                binding.bsInfo.resMnc.text = bsInfoModel.mnc
                binding.bsInfo.resOperator.text = bsInfoModel.operator
                binding.bsInfo.resCellId.text = bsInfoModel.ci.toString()
                binding.bsInfo.resTacLac.text = bsInfoModel.tac.toString()
                binding.bsInfo.resENB.text = bsInfoModel.eNB.toString()
                binding.bsInfo.resPci.text = bsInfoModel.pci.toString()
                binding.bsInfo.resEARFCN.text= bsInfoModel.Earfcn.toString()
                binding.bsInfo.resBand.text = bsInfoModel.band.toString()

            }
        }
    }

    private val receiverSignal = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, q: Intent?) {
            if (q?.action == ServiceBack.SIGNAL_MODLE_INTENT){
                val signalModel = q.getSerializableExtra(ServiceBack.SIGNAL_MODLE_INTENT) as SignalModel
                Log.d("Check", signalModel.toString())
                if (signalModel.rssi != Int.MAX_VALUE && signalModel.rssi >= -140 && signalModel.rssi <= -43){
                    binding.signalInfo.resRssi.text = signalModel.rssi.toString() + " дБм"
                }else{
                    binding.signalInfo.resRssi.text = "N/a"
                }
                if (signalModel.rsrp != Int.MAX_VALUE && signalModel.rssi < 0){
                    binding.signalInfo.resRsrp.text = signalModel.rsrp.toString() + " дБм"
                }else{
                    binding.signalInfo.resRsrp.text = "N/a"
                }
                if (signalModel.rsrq != Int.MAX_VALUE){
                    binding.signalInfo.resRsrq.text = signalModel.rsrq.toString() + " дБ"
                }else{
                    binding.signalInfo.resRsrq.text = "N/a"
                }
                if (signalModel.snr != Int.MAX_VALUE){
                    binding.signalInfo.resSnr.text = signalModel.snr.toString() + " дБ"
                }else{
                    binding.signalInfo.resSnr.text = "N/a"
                }
                if (signalModel.cqi != Int.MAX_VALUE){
                    binding.signalInfo.resSnr.text = signalModel.cqi.toString()
                }else{
                    binding.signalInfo.resCqi.text = "N/a"
                }
                if (signalModel.ta != Int.MAX_VALUE){
                    binding.bsInfo.resTA.text = signalModel.ta.toString()
                }else{
                    binding.bsInfo.resTA.text = "N/a"
                }
            }
        }
    }




    private fun registerLocReceiver(){
        val locFilter = IntentFilter(ServiceBack.LOC_MODLE_INTENT)
        val signalFilter = IntentFilter(ServiceBack.SIGNAL_MODLE_INTENT)
        val bsinfoFilter = IntentFilter(ServiceBack.BS_MODLE_INTENT)
        val speedFilter = IntentFilter(ServiceBack.Speed_MODLE_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverSpeed,speedFilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverBS,bsinfoFilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverSignal, signalFilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverLoc, locFilter)

    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}

