package com.example.testkotlin.fragmentsimport

import android.util.Log
import io.ktor.client.HttpClient

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Cell(
    val id: Int,
    val radio: String,
    val mcc: Int,
    val mnc: Int,
    val lac: Int,
    val cid: Int,
    val long: Double,
    val lat: Double,
    val range: Int,
    val samples: Int,
);

@Serializable
data class Cells(
    val cells: List<Cell>,
    val count: Int,
    val error: Boolean,
    val message: String
)

class SignalStrengthServer{
    private var client: HttpClient;
    private var baseURL: String;
    constructor (baseURL: String, login: String, password: String) {
        this.client = HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
            }
            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = login, password = password)
                    }
                }
            }
        }
        this.baseURL = baseURL
    }

    //     http://ss.sut.dchudinov.ru/api/v1/cells?lat=59.903119&long=30.488665&radius=0.001
    suspend fun getCells(lat: Double, long: Double, radius: Double): Cells{
        val cells: Cells = this.client.get(
            String.Companion.format("%s/cells?lat=%s&long=%s&radius=%s", this.baseURL, lat.toString(), long.toString(), radius.toString())
        ).body()
        return cells;
    }
//    http://ss.sut.dchudinov.ru/api/v1/signals?lat=59.90358805&long=30.48996893&radius=0.02&mnc=2&network=4G&dateStart=2023-08-31&dateEnd=2024-04-23&display=table
    fun getSignalsTable(lat: Int, long: Int, radius: Int){

    }
//        http://ss.sut.dchudinov.ru/api/v1/signals?lat=59.90358805&long=30.48996893&radius=0.02&
//        mnc=2&network=4G&dateStart=2023-08-31&dateEnd=2024-04-23
    fun getSignals(mnc:Int, network:String, lat: Double, long: Double, radius: Double,
                   dateStart: String, dateEnd: String){

    }
}
