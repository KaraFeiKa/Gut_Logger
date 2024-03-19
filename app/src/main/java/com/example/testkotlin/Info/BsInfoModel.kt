package com.example.testkotlin.Info

import java.io.Serializable

data class BsInfoModel(
    val mcc: String = "",
    val mnc: String = "",
    val ci: Int = 0,
    val band: Any = intArrayOf(),
    val eNB: Int = 0,
    val Earfcn: Int = 0,
    val pci: Int = 0,
    val tac: Int = 0,
    val operator: CharSequence? = ""

): Serializable
