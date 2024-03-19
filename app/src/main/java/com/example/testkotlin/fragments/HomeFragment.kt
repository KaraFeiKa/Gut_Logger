package com.example.testkotlin.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyCallback
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testkotlin.Location.LocationModel
import com.example.testkotlin.Location.ServiceBack
import com.example.testkotlin.Location.SignalModel
import com.example.testkotlin.R
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
            if(it[Manifest.permission.ACCESS_FINE_LOCATION]  == true && it[Manifest.permission.READ_PHONE_STATE]  == true && it[Manifest.permission.POST_NOTIFICATIONS]  == true && it[Manifest.permission.ACCESS_COARSE_LOCATION]  == true && it[Manifest.permission.FOREGROUND_SERVICE_LOCATION]  == true)  {

            } else {
                Toast.makeText(activity, "Вы не дали разрешения!", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun checkPermission() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.READ_PHONE_STATE)
            &&checkPermission(Manifest.permission.POST_NOTIFICATIONS)
            && checkPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION)

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
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
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
        activity?.stopService(Intent(activity, ServiceBack::class.java))
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




    private fun registerLocReceiver(){
        val locFilter = IntentFilter(ServiceBack.LOC_MODLE_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverLoc, locFilter)

    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}

