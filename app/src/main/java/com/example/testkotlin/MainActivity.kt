package com.example.testkotlin

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellLocation
import android.telephony.CellSignalStrengthLte
import android.telephony.PhysicalChannelConfig
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log

import androidx.annotation.RequiresApi
import com.example.testkotlin.databinding.ActivityMainBinding
import com.example.testkotlin.fragments.HomeFragment
import com.example.testkotlin.fragments.IndoorFragment
import com.example.testkotlin.fragments.MapFragment
import com.example.testkotlin.fragments.NeighborsFragment
import com.example.testkotlin.fragments.SettingsFragment
import com.example.testkotlin.utils.openFragment
import com.yandex.mapkit.MapKitFactory

class MainActivity : AppCompatActivity() {
        private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBottClick()
        MapKitFactory.setApiKey(MapFragment.MAPKIT_API_KEY)
        openFragment(HomeFragment.newInstance())
    }


    private fun onBottClick(){
        binding.BNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Bot_Home -> openFragment(HomeFragment.newInstance())
                R.id.Bot_Second -> openFragment(NeighborsFragment.newInstance())
                R.id.Bot_Map -> openFragment(MapFragment.newInstance())
                R.id.Bot_Indoor -> openFragment(IndoorFragment.newInstance())
                R.id.Bot_Setting -> openFragment(SettingsFragment.newInstance())
            }
            true
        }
    }


}

