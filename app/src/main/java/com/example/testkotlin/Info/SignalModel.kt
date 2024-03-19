package com.example.testkotlin.Info

import java.io.Serializable

data class SignalModel(
    val rssi: Int = 0,
    val rsrp: Int = 0,
    val rsrq: Int = 0,
    val snr: Int = 0,
    val cqi: Int = 0,
    val ta: Int = 0
): Serializable
