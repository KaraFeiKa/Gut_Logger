package com.example.testkotlin.Info

import java.io.Serializable

data class InfoNeiborhood(

    val Type: String = "",
    val pci: Int = 0,
    val Earfcn: Int = 0,
    val band: Any,
    val rssi: Int = 0,
    val rsrp: Int = 0,
    val rsrq: Int = 0,
    val ta: Int = 0,
    val psc: Int = 0,
    val Uarfcn: Int = 0,
    val ss: Int = 0,
    val lac: Int = 0,
    val ci: Int = 0,
    val Arfcn: Int = 0,
    val bsic: Int = 0,
    val rssi2g: Int = 0,
): Serializable
