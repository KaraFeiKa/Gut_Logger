package com.example.testkotlin.Info

import java.io.Serializable


data class LocationModel(
    val lat: Double =0.0,
    val lon: Double = 0.0,
//    val geoPointsList: ArrayList<GeoPoint>


) : Serializable
