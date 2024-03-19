package com.example.testkotlin.TrafficSpeed

interface ITrafficSpeedListener {
    fun onTrafficSpeedMeasured(upStream: Double, downStream: Double)
}