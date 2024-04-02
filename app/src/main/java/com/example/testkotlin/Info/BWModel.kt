package com.example.testkotlin.Info

import android.telephony.ServiceState
import java.io.Serializable

data class BWModel(
    val BW: ServiceState
): Serializable
