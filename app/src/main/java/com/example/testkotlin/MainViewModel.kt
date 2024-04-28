package com.example.testkotlin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testkotlin.Info.BWModel
import com.example.testkotlin.Info.BsInfoModel
import com.example.testkotlin.Info.CallInfoModel
import com.example.testkotlin.Info.InfoNeiborhood
import com.example.testkotlin.Info.LocationModel
import com.example.testkotlin.Info.SignalModel
import com.example.testkotlin.Info.SpeedModel

class MainViewModel : ViewModel() {
    val locationUpdates = MutableLiveData<LocationModel>()
    val signalUpdate = MutableLiveData<SignalModel>()
    val bwUpdate = MutableLiveData<BWModel>()
    val speedUpdate = MutableLiveData<SpeedModel>()
    val callUpdate = MutableLiveData<CallInfoModel>()
    val bsinfoUpdate = MutableLiveData<BsInfoModel>()
    val neighboursUpdate = MutableLiveData<InfoNeiborhood>()
}