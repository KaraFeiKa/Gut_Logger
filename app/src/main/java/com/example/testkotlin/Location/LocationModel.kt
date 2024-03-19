package com.example.testkotlin.Location

import org.osmdroid.util.GeoPoint
import java.io.Serializable


data class SignalModel(
    val rssi: Int = 0,
    val rsrp: Int = 0,
    val rsrq: Int = 0,
    val snr: Int = 0,
    val cqi: Int = 0
): Serializable
data class LocationModel(
    val lat: Double =0.0,
    val lon: Double = 0.0,
//    val geoPointsList: ArrayList<GeoPoint>


) : Serializable
