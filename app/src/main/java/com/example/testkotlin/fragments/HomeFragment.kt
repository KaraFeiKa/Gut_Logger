package com.example.testkotlin.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testkotlin.Info.BWModel
import com.example.testkotlin.Info.BsInfoModel
import com.example.testkotlin.Info.CallInfoModel
import com.example.testkotlin.Info.LocationModel
import com.example.testkotlin.Info.ServiceBack
import com.example.testkotlin.Info.SignalModel
import com.example.testkotlin.Info.SpeedModel
import com.example.testkotlin.databinding.FragmentHomeBinding
import com.example.testkotlin.utils.checkPermission
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.internal.Intrinsics.Kotlin

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private var ColorLogButt = false
    var lastFileName: String = ""
    lateinit var writer: CSVWriter
    val nocProjectDirInDownload: String = "GUT_Logger"
    var csv: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + nocProjectDirInDownload+ "/" + lastFileName

    var FUL: Float = 0.0F
    var FDL: Float = 0.0F
    var lat: Double  = 0.0
    var lon: Double  = 0.0
    var Operator: CharSequence? = ""
    var mnc: String = ""
    var mcc: String = ""
    var ULSpeed: String = ""
    var DLSpeed: String = ""
    var NameR: String = ""
    var Mode: String = ""
    var ci: Int = 0
    var band: Any = intArrayOf()
    var eNB: Int = 0
    var Earfcn: Int = 0
    var pci: Int = 0
    var tac: Int = 0
    var rssi: Int = 0
    var rsrp: Int = 0
    var rsrq: Int = 0
    var snr: Int = 0
    var cqi: Int = 0
    var ta: Int = 0
    var BandPlus: Int = 0
    var rnc: Int = 0
    var psc: Int = 0
    var ber: Int = 0
    var bsic: Int = 0
    var Arfcn: Int = 0
    var Uarfcn: Int = 0
    var EcNo: Int = 0
    var bandwidnths = intArrayOf(0)
    var convertedBands  = intArrayOf(0)




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        LogButton()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissions()
        registerLocReceiver()
        ColorButt()
        CreatDirect()




    }

    private fun workSersive(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, ServiceBack::class.java))
        } else {
            activity?.startService(Intent(activity, ServiceBack::class.java))
        }
    }


    private fun registerPermissions(){

        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            if(it[Manifest.permission.ACCESS_FINE_LOCATION]  == true && it[Manifest.permission.READ_PHONE_STATE]  == true
                && it[Manifest.permission.POST_NOTIFICATIONS]  == true && it[Manifest.permission.ACCESS_COARSE_LOCATION]  == true
                )  {

            } else {
                Toast.makeText(activity, "Вы не дали разрешения!", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun checkPermission() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.READ_PHONE_STATE)
            &&checkPermission(Manifest.permission.POST_NOTIFICATIONS)


        ) {
            workSersive()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }


    private fun checkLocationEnabled(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!isEnabled){
            Toast.makeText(activity, "GPS выключен!", Toast.LENGTH_LONG).show()
        }
    }

    private fun CreatDirect(){
        try {
            Log.d("public directory", csv)
            val appDir = File(csv)
            if (!appDir.exists() && !appDir.isDirectory) {
                if (appDir.mkdirs()) {
                    Log.d("public directory", "created")
                } else {
                    Log.d("public directory", "not created")
                    csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun LogButton(){
        val colorRed = Color.RED
        val colorGreen = Color.rgb(76,175,80)
        binding.Log.button.setOnClickListener {
            if (ServiceBack.WriterIsWorking == false){
                binding.Log.button.text = "Остановить запись"
                binding.Log.infoLog.text = "Идет запись"
                binding.Log.button.setBackgroundColor(colorRed)
                ServiceBack.WriterIsWorking = true
                writeCVS()


            }
            else{
                binding.Log.button.text = "Начать запись"
                binding.Log.infoLog.text = "Запись сохранена!"
                binding.Log.button.setBackgroundColor(colorGreen)
                ServiceBack.WriterIsWorking = false
                ColorLogButt = true
                try {
                    writer.close()
                }catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun ColorButt(){
        val colorRed = Color.RED
        val colorGreen = Color.rgb(76,175,80)
        ColorLogButt = ServiceBack.WriterIsWorking
        if (ColorLogButt){
            binding.Log.button.setBackgroundColor(colorRed)
            binding.Log.infoLog.text = "Идет запись"
        }else{
            binding.Log.button.setBackgroundColor(colorGreen)
        }
    }
    private fun writeCVS(){
        try {
            val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val now: LocalDateTime = LocalDateTime.now()
            this.lastFileName = "$csv/${dtf.format(now)}_Main.csv"
            writer = CSVWriter(FileWriter(lastFileName))
            val data: MutableList<Array<String>> = ArrayList()
            data.add(arrayOf("lat", "log", "Operator", "Network", "mcc", "mnc", "Mode",
                "TAC/LAC", "CID", "eNB", "Band", "Bandwidnths, MHz", "    Earfcn",
                "Uarfcn", "Arfcn", "UL, MHz", "DL, MHz", "PCI", "PSC", "RNC",
                "BSIC", "RSSI, dBm", "RSRP, dBm", "RSRQ, dB",
                "SNR, dB", "EcNo, dB", "BER", "Cqi", "dBm", "Level", "Asulevel", "Ta","UP Speed","DL Speed"))
            writer.writeAll(data)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        checkPermission()
        Log.d("life", "Resume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("life", "Destroy")

    }


    val receiverLoc = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, i: Intent?) {
            if (i?.action == ServiceBack.LOC_MODLE_INTENT){
                val locModel = i.getSerializableExtra(ServiceBack.LOC_MODLE_INTENT) as LocationModel
                lat = locModel.lat
                lon = locModel.lon
                binding.gpsInfo.resLat.text = lat.toString()
                binding.gpsInfo.resLon.text = lon.toString()

                if(ServiceBack.WriterIsWorking == true){
                    writeLTEInfo()
                }
            }

        }
    }

    private val receiverSpeed = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, s: Intent?) {
            if (s?.action == ServiceBack.Speed_MODLE_INTENT){
                val speedModel = s.getSerializableExtra(ServiceBack.Speed_MODLE_INTENT) as SpeedModel
                DLSpeed = speedModel.UL
                ULSpeed = speedModel.DL
                binding.speed.speedUL.text= DLSpeed
                binding.speed.speedDL.text = ULSpeed
            }
        }
    }

    private val receiverBS = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, w: Intent?) {
            if (w?.action == ServiceBack.BS_MODLE_INTENT){
                val bsInfoModel = w.getSerializableExtra(ServiceBack.BS_MODLE_INTENT) as BsInfoModel

                if (bsInfoModel.net == "4G"){
                    calc()
                    binding.bsInfo.resNetworkType.text = bsInfoModel.net
                    Operator = bsInfoModel.operator
                    mcc = bsInfoModel.mcc
                    mnc = bsInfoModel.mnc
                    binding.bsInfo.resMCC.text = mcc
                    binding.bsInfo.resMnc.text = mnc
                    ci = bsInfoModel.ci
                    tac = bsInfoModel.tac
                    eNB = bsInfoModel.eNB
                    pci = bsInfoModel.pci
                    Earfcn =bsInfoModel.Earfcn
                    binding.bsInfo.textView8.text = "TAC:"
                    binding.bsInfo.textView20.text = "EARFCN:"
                    binding.bsInfo.textView12.text = "eNB:"
                    binding.bsInfo.textView14.text = "PCI:"
                    binding.bsInfo.TA.text = "TA:"
                    if (bsInfoModel.band != Unit){
                        band = bsInfoModel.band
                    }
                    else{
                        band = BandPlus
                    }
                    binding.bsInfo.resOperator.text = Operator
                    binding.bsInfo.resCellId.text = ci.toString()
                    binding.bsInfo.resTacLac.text = tac.toString()
                    binding.bsInfo.resENB.text = eNB.toString()
                    binding.bsInfo.resPci.text = pci.toString()
                    binding.bsInfo.resEARFCN.text = Earfcn.toString()
                    binding.bsInfo.resBand.text = "$band ($NameR)"
                    binding.bsInfo.textView22.text = "Полоса:"
                    binding.bsInfo.resDl.text = "$FDL МГц"
                    binding.bsInfo.resUl.text = "$FUL МГц"
                }

                if (bsInfoModel.net == "3G"){
                    calcUmts()
                    binding.bsInfo.resNetworkType.text = bsInfoModel.net
                    Operator = bsInfoModel.operator
                    mcc = bsInfoModel.mcc
                    mnc = bsInfoModel.mnc
                    binding.bsInfo.resMCC.text = mcc
                    binding.bsInfo.resMnc.text = mnc
                    ci = bsInfoModel.ci
                    tac = bsInfoModel.tac
                    rnc = bsInfoModel.eNB
                    psc = bsInfoModel.pci
                    binding.bsInfo.resDl.text = "$FDL МГц"
                    binding.bsInfo.resUl.text = "$FUL МГц"
                    band = BandPlus
                    binding.bsInfo.resBand.text = "$band ($NameR)"
                    binding.bsInfo.textView8.text = "LAC:"
                    binding.bsInfo.textView20.text = "UARFCN:"
                    Uarfcn =bsInfoModel.Earfcn
                    binding.bsInfo.resEARFCN.text = Uarfcn.toString()
                    binding.bsInfo.resOperator.text = Operator
                    binding.bsInfo.resCellId.text = ci.toString()
                    binding.bsInfo.resTacLac.text = tac.toString()
                    binding.bsInfo.textView12.text = "Rnc:"
                    binding.bsInfo.resENB.text = rnc.toString()
                    binding.bsInfo.textView14.text = "Psc:"
                    binding.bsInfo.resPci.text = psc.toString()
                    binding.bsInfo.resBW.text = ""
                    binding.bsInfo.textView22.text = ""

                }

                if (bsInfoModel.net == "2G"){
                    calcArfcn()
                    binding.bsInfo.resNetworkType.text = bsInfoModel.net
                    Operator = bsInfoModel.operator
                    mcc = bsInfoModel.mcc
                    mnc = bsInfoModel.mnc
                    binding.bsInfo.resMCC.text = mcc
                    binding.bsInfo.resMnc.text = mnc
                    ci = bsInfoModel.ci
                    tac = bsInfoModel.tac
                    bsic = bsInfoModel.pci
                    binding.bsInfo.textView8.text = "LAC"
                    binding.bsInfo.textView20.text = "ARFCN:"
                    Arfcn =bsInfoModel.Earfcn
                    binding.bsInfo.resDl.text = "$FDL МГц"
                    binding.bsInfo.resUl.text = "$FUL МГц"
                    band = BandPlus
                    binding.bsInfo.resBand.text = "$band ($NameR)"
                    binding.bsInfo.resEARFCN.text = Arfcn.toString()
                    binding.bsInfo.resOperator.text = Operator
                    binding.bsInfo.resCellId.text = ci.toString()
                    binding.bsInfo.resTacLac.text = tac.toString()
                    binding.bsInfo.textView12.text = "bsic:"
                    binding.bsInfo.resENB.text = bsic.toString()
                    binding.bsInfo.textView14.text = "TA:"
                    binding.bsInfo.resBW.text = ""
                    binding.bsInfo.textView22.text = ""
                    binding.bsInfo.TA.text = ""
                    binding.bsInfo.resTA.text=""
                    binding.bsInfo.resDl.text = "$FDL МГц"
                    binding.bsInfo.resUl.text = "$FUL МГц"
                }

            }
        }
    }

    private val receiverSignal = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, q: Intent?) {
            if (q?.action == ServiceBack.SIGNAL_MODLE_INTENT){
                val signalModel = q.getSerializableExtra(ServiceBack.SIGNAL_MODLE_INTENT) as SignalModel

                if (signalModel.net == "4G"){

                    binding.signalInfo.textView27.text = "CQI:"
                    binding.signalInfo.textView3.text = "RSRP:"
                    binding.signalInfo.textView5.text = "RSRQ:"


                    if (signalModel.rssi != Int.MAX_VALUE && signalModel.rssi >= -140 && signalModel.rssi <= -43){
                        rssi = signalModel.rssi
                        binding.signalInfo.resRssi.text = rssi.toString() + " дБм"
                    }else{
                        rssi = -0
                        binding.signalInfo.resRssi.text = "N/a"
                    }
                    if (signalModel.rsrp != Int.MAX_VALUE && signalModel.rsrp < 0){
                        rsrp = signalModel.rsrp
                        binding.signalInfo.resRsrp.text = "$rsrp дБм"
                    }else{
                        rsrp = -0
                        binding.signalInfo.resRsrp.text = "N/a"
                    }
                    if (signalModel.rsrq != Int.MAX_VALUE){
                        rsrq = signalModel.rsrq
                        binding.signalInfo.resRsrq.text = "$rsrq дБ"
                    }else{
                        rsrq = -0
                        binding.signalInfo.resRsrq.text = "N/a"
                    }
                    if (signalModel.snr != Int.MAX_VALUE){
                        snr = signalModel.snr
                        binding.signalInfo.resSnr.text = "$snr дБ"
                    }else{
                        snr = -0
                        binding.signalInfo.resSnr.text = "N/a"
                    }
                    if (signalModel.cqi != Int.MAX_VALUE){
                        cqi = signalModel.cqi
                        binding.signalInfo.resCqi.text = cqi.toString()
                    }else{
                        cqi = -0
                        binding.signalInfo.resCqi.text = "N/a"
                    }
                    if (signalModel.ta != Int.MAX_VALUE){
                        ta = signalModel.ta
                        binding.bsInfo.resTA.text = ta.toString()
                    }else{
                        ta = -0
                        binding.bsInfo.resTA.text = "N/a"
                    }
                }
                if (signalModel.net == "3G"){
                    binding.bsInfo.resTA.text = ""
                    binding.bsInfo.TA.text=""
                    rssi = signalModel.rssi
                    binding.signalInfo.resRssi.text = "${rssi} дБм"
                    binding.signalInfo.textView3.text = "RSCP:"
                    rsrp = signalModel.rsrp
                    binding.signalInfo.resRsrp.text = "${rsrp} дБм"
                    binding.signalInfo.textView5.text = "Ec/N0:"
                    EcNo = signalModel.snr
                    binding.signalInfo.resRsrq.text = "${EcNo} дБ"
                    binding.signalInfo.textView27.text = ""
                    binding.signalInfo.resCqi.text = ""

                }
                if (signalModel.net == "2G"){
                    Log.d("GSM", signalModel.toString())
                    rssi = signalModel.rssi
                    binding.signalInfo.resRssi.text = "${rssi} дБм"
                    binding.signalInfo.textView3.text = ""

                    if (signalModel.snr != Int.MAX_VALUE && signalModel.snr != 99){
                        ber = signalModel.snr
                        binding.signalInfo.resRsrq.text = "${ber} дБм"
                    }else{
                        ber = -0
                        binding.signalInfo.resRsrq.text = "N/a"
                    }
                    binding.signalInfo.textView5.text = "BER:"
                    binding.signalInfo.textView27.text = ""
                }



            }
        }
    }

    private val receiverBW = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, bw: Intent?) {
            if (bw?.action == ServiceBack.BW_MODLE_INTENT) {
                val bwModel = bw.getSerializableExtra(ServiceBack.BW_MODLE_INTENT) as BWModel
                Log.d ("BW1", bwModel.BW.toString())

                bandwidnths = bwModel.BW.cellBandwidths
                if (bandwidnths.isNotEmpty()) {
                    convertedBands = IntArray(bandwidnths.size)
                    for (i in 0 until bandwidnths.size) {
                        convertedBands[i] = bandwidnths[i] / 1000
                        Log.d ("BW2", convertedBands[i].toString())
                    }
                    if (convertedBands.size >1){
                        binding.bsInfo.resBW.text = "[${convertedBands.joinToString("/")}] CA, МГц"
                    }else{
                        binding.bsInfo.resBW.text = convertedBands.joinToString("/") + " МГц"
                    }

                }
            }
        }
    }

    private val receiverCall = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, c: Intent?) {
            if (c?.action == ServiceBack.Call_MODLE_INTENT){
                val callInfoModel = c.getSerializableExtra(ServiceBack.Call_MODLE_INTENT) as CallInfoModel
                binding.bsInfo.resRRC.text = callInfoModel.State
            }
        }
    }


    private fun registerLocReceiver(){
        val locFilter = IntentFilter(ServiceBack.LOC_MODLE_INTENT)
        val signalFilter = IntentFilter(ServiceBack.SIGNAL_MODLE_INTENT)
        val bsinfoFilter = IntentFilter(ServiceBack.BS_MODLE_INTENT)
        val speedFilter = IntentFilter(ServiceBack.Speed_MODLE_INTENT)
        val BWfilter = IntentFilter(ServiceBack.BW_MODLE_INTENT)
        val Callfilter = IntentFilter(ServiceBack.Call_MODLE_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverCall,Callfilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverBW,BWfilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverSpeed,speedFilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverBS,bsinfoFilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverSignal, signalFilter)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverLoc, locFilter)

    }
    fun writeLTEInfo() {
        writer?.let { writer ->
            var bandwidth = ""
           convertedBands?.let {
                bandwidth = it.joinToString("/")
            }
            val str: Array<String> = arrayOf(
                lat.toString(),
                lon.toString(),
                Operator.toString(),
                "4G",
                mcc,
                mnc,
                Mode,
                tac.toString(),
                ci.toString(),
                eNB.toString(),
                band.toString(),
                bandwidth,
                Earfcn.toString(),
                "",
                "",
                FUL.toString(),
                FDL.toString(),
                pci.toString(),
                "",
                "",
                "",
                rssi.toString(),
                rsrp.toString(),
                rsrq.toString(),
                snr.toString(),
                "",
                "",
                cqi.toString(),
                "delite",
                "delite",
                "delite",
                ta.toString(),
                ULSpeed,
                DLSpeed
            )
            writer.writeNext(str, false)
        }
    }
    private fun calc() {
        var FDL_low: Float
        var NDL: Int
        var NOffs_DL: Int
        var FUL_low: Float
        var NUL: Int
        var NOffs_UL: Int

        if (0 <=     Earfcn &&     Earfcn <= 599) {
            NameR = "2100"
            Mode = "FDD"
            NDL =     Earfcn
            FDL_low = 2110.0F
            NOffs_DL = 0
            BandPlus = 1
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat()
            NUL =     Earfcn + 18000
            FUL_low = 1920.0F
            NOffs_UL = 18000
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat()
        }
        if (600 <=     Earfcn &&     Earfcn <= 1199) {
            NameR = "1900 PCS" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 1930F 
            NOffs_DL = 600 
            BandPlus = 2
            FDL = (FDL_low + (0.1 * (NDL - NOffs_DL))).toFloat()
            NUL =     Earfcn + 18000 
            FUL_low = 1850F 
            NOffs_UL = 18600 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat()) 
        }
        if (1200 <=     Earfcn &&     Earfcn <= 1949) {
            NameR = "1800+" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 1805F 
            NOffs_DL = 1200 
            BandPlus = 3 
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat()
            NUL =     Earfcn + 18000 
            FUL_low = 1710F 
            NOffs_UL = 19200 
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat() 
        }
        if (1950 <=     Earfcn &&     Earfcn <= 2399) {
            NameR = "AWS-1" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 2110F 
            NOffs_DL = 1950 
            BandPlus = 4 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat()) 
            NUL =     Earfcn + 18000 
            FUL_low = 1710F 
            NOffs_UL = 19950 
            FUL =  ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat()) 
        }
        if (2400 <=     Earfcn &&     Earfcn <= 2649) {
            NameR = "850" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 869F 
            NOffs_DL = 2400 
            BandPlus = 5 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat()) 
            NUL =     Earfcn + 18000 
            FUL_low = 824F 
            NOffs_UL = 20400 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat()) 
        }
        if (2750 <=     Earfcn &&     Earfcn <= 3449) {
            NameR = "2600" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 2620F 
            NOffs_DL = 2750 
            BandPlus = 7 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat()) 
            NUL =     Earfcn + 18000 
            FUL_low = 2500F 
            NOffs_UL = 20750 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat()) 
        }
        if (3450 <=     Earfcn &&     Earfcn <= 3799) {
            NameR = "900 GSM" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 925F
            NOffs_DL = 3450 
            BandPlus = 8 
            FDL =  ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 880F
            NOffs_UL = 21450 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (3800 <=     Earfcn &&     Earfcn <= 4149) {
            NameR = "1800" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 1844.9F 
            NOffs_DL = 3800 
            BandPlus = 9 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 1749.9F 
            NOffs_UL = 21800 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }

        if (4150 <=     Earfcn &&     Earfcn <= 4749) {
            NameR = "AWS-3" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 2110F
            NOffs_DL = 4150 
            BandPlus = 10 
            FDL =  ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 1710F
            NOffs_UL = 22150 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (4750 <=     Earfcn &&     Earfcn <= 4949) {
            NameR = "1500 Lower" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 1475.9F
            NOffs_DL = 4750 
            BandPlus = 11 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 1427.9F
            NOffs_UL = 22750 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (5010 <=     Earfcn &&     Earfcn <= 5179) {
            NameR = "700 a" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 729F
            NOffs_DL = 5010 
            BandPlus = 12 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 699F
            NOffs_UL = 23010 
            FUL =  ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (5180 <=     Earfcn &&     Earfcn <= 5279) {
            NameR = "700 c" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 746F
            NOffs_DL = 5180 
            BandPlus = 13 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 777F
            NOffs_UL = 23180 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (5280 <=     Earfcn &&     Earfcn <= 5379) {
            NameR = "700 PS" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 758F
            NOffs_DL = 5280 
            BandPlus = 14 
            FDL =  ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000
            FUL_low = 788F
            NOffs_UL = 23280 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (5730 <=     Earfcn &&     Earfcn <= 5849) {
            NameR = "700 b" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 734F
            NOffs_DL = 5730 
            BandPlus = 17 
            FDL =  ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 704F
            NOffs_UL = 23730 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (5850 <=     Earfcn &&     Earfcn <= 5999) {
            NameR = "800 Lower" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 860F
            NOffs_DL = 5850 
            BandPlus = 18 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 815F
            NOffs_UL = 23850 
            FUL =  ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (6000 <=     Earfcn &&     Earfcn <= 6149) {
            NameR = "800 Upper" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 875F
            NOffs_DL = 6000 
            BandPlus = 19 
            FDL =  ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 830F
            NOffs_UL = 24000 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (6150 <=     Earfcn &&     Earfcn <= 6449) {
            NameR = "800 DD" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 791F
            NOffs_DL = 6150 
            BandPlus = 20 
            FDL =  ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 832F
            NOffs_UL = 24150 
            FUL =  ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (6450 <=     Earfcn &&     Earfcn <= 6599) {
            NameR = "1500 Upper" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 1495.9F
            NOffs_DL = 6450 
            BandPlus = 21 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 1447.9F
            NOffs_UL = 24450 
            FUL =  ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (6600 <=     Earfcn &&     Earfcn <= 7399) {
            NameR = "3500" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 3510F
            NOffs_DL = 6600 
            BandPlus = 22 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 3410F
            NOffs_UL = 24600 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (7700 <=     Earfcn &&     Earfcn <= 8039) {
            NameR = "1600 L-band" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 1525F
            NOffs_DL = 7700 
            BandPlus = 24 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 1626.5F
            NOffs_UL = 25700 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (8040 <=     Earfcn &&     Earfcn <= 8689) {
            NameR = "1900+" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 1930F
            NOffs_DL = 8040 
            BandPlus = 25 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 1850F
            NOffs_UL = 26040 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (8690 <=     Earfcn &&     Earfcn <= 9039) {
            NameR = "850+" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 859F
            NOffs_DL = 8690 
            BandPlus = 26 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 814F
            NOffs_UL = 26690 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (9040 <=     Earfcn &&     Earfcn <= 9209) {
            NameR = "800 SMR" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 852F
            NOffs_DL = 8690 
            BandPlus = 27 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 814F
            NOffs_UL = 26690 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (9210 <=     Earfcn &&     Earfcn <= 9659) {
            NameR = "700 APT" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 758F
            NOffs_DL = 9210 
            BandPlus = 28 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 703F
            NOffs_UL = 27210 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (9660 <=     Earfcn &&     Earfcn <= 9769) {
            NameR = "700 d" 
            Mode = "SDL" 
            NDL =     Earfcn 
            FDL_low = 717F
            NOffs_DL = 9660 
            BandPlus = 29 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())

        }
        if (9770 <=     Earfcn &&     Earfcn <= 9869) {
            NameR = "2300 WCS" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 2350F
            NOffs_DL = 9770 
            BandPlus = 30 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 2305F
            NOffs_UL = 27660 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (9870 <=     Earfcn &&     Earfcn <= 9919) {
            NameR = "450" 
            Mode = "FDD" 
            NDL =     Earfcn 
            FDL_low = 462.5F
            NOffs_DL = 9870 
            BandPlus = 31 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            NUL =     Earfcn + 18000 
            FUL_low = 452.5F
            NOffs_UL = 27760 
            FUL = ((FUL_low + 0.1 * (NUL - NOffs_UL)).toFloat())
        }
        if (9920 <=     Earfcn &&     Earfcn <= 10359) {
            NameR = "1500 L-band" 
            Mode = "SDL" 
            NDL =     Earfcn 
            FDL_low = 1452F
            NOffs_DL = 9920 
            BandPlus = 32 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            FUL = 0.0F
        }
        if (36000 <=     Earfcn &&     Earfcn <= 36199) {
            NameR = "TD 1900" 
            Mode = "TDD" 
            NDL =     Earfcn 
            FDL_low = 1900F
            NOffs_DL = 36000 
            BandPlus = 33 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            FUL = 0.0F
        }
        if (36200 <=     Earfcn &&     Earfcn <= 36349) {
            NameR = "TD 2000" 
            Mode = "TDD" 
            NDL =     Earfcn 
            FDL_low = 2010F
            NOffs_DL = 36200 
            BandPlus = 34 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            FUL = 0.0F
        }
        if (36200 <=     Earfcn &&     Earfcn <= 36349) {
            NameR = "TD PCS Lower" 
            Mode = "TDD" 
            NDL =     Earfcn 
            FDL_low = 1850F
            NOffs_DL = 36350 
            BandPlus = 35 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            FUL = 0.0F
        }
        if (36950 <=     Earfcn &&     Earfcn <= 37549) {
            NameR = "TD PCS Upper" 
            Mode = "TDD" 
            NDL =     Earfcn 
            FDL_low = 1930F
            NOffs_DL = 36950 
            BandPlus = 36 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            FUL = 0.0F
        }
        if (37550 <=     Earfcn &&     Earfcn <= 37749) {
            NameR = "TD PCS Center gap" 
            Mode = "TDD" 
            NDL =     Earfcn 
            FDL_low = 1910F
            NOffs_DL = 37550 
            BandPlus = 37 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            FUL = 0.0F
        }
        if (37750 <=     Earfcn &&     Earfcn <= 38249) {
            NameR = "TD 2600" 
            Mode = "TDD" 
            NDL =     Earfcn 
            FDL_low = 2570F
            NOffs_DL = 37750 
            BandPlus = 38 
            FDL = ((FDL_low + 0.1 * (NDL - NOffs_DL)).toFloat())
            FUL = 0.0F
        }


    }

    private fun calcUmts() {
        var NDL: Double
        var NOffs_DL: Double
        var NUL: Double
        var NOffs_UL: Double
        when {
            Uarfcn in 10562..10838 -> {
                NameR = "2100"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 0.0
                BandPlus = 1
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 950).toDouble()
                NOffs_UL = 0.0
                FUL= (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 9662..9938 -> {
                NameR = "1900 PCS"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 0.0
                BandPlus = 2
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 400).toDouble()
                NOffs_UL = 0.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 1162..1513 -> {
                NameR = "1800 DCS"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 1575.0
                BandPlus = 3
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 225).toDouble()
                NOffs_UL = 1525.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 1537..1738 -> {
                NameR = "AWS-1"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 1805.0
                BandPlus = 4
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 225).toDouble()
                NOffs_UL = 1450.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 4357..4458 -> {
                NameR = "850"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 0.0
                BandPlus = 5
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 225).toDouble()
                NOffs_UL = 0.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 2237..2563 -> {
                NameR = "2600"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 2175.0
                BandPlus = 7
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 225).toDouble()
                NOffs_UL = 2100.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 3112..3388 -> {
                NameR = "AWS-1+"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 1490.0
                BandPlus = 10
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 225).toDouble()
                NOffs_UL = 1135.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 3712..3787 -> {
                NameR = "1500 Lower"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = 736.0
                BandPlus= 11
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 225).toDouble()
                NOffs_UL = 733.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
            Uarfcn in 3842..3903 -> {
                NameR = "700 a"
                Mode = "FDD"
                NDL = Uarfcn.toDouble()
                NOffs_DL = -37.0
                BandPlus = 12
                FDL = (NOffs_DL + NDL / 5).toFloat()
                NUL = (Uarfcn - 225).toDouble()
                NOffs_UL = -22.0
                FUL = (NOffs_UL + NUL / 5).toFloat()
            }
        }
    }

    private fun calcArfcn() {
        when {
            Arfcn in 0..124 -> {
                NameR = "E-GSM"
                FUL = (890 + 0.2 * Arfcn).toFloat()
                FDL = FUL + 45
            }
            Arfcn in 512..885 -> {
                NameR = "DCS 1800"
                FUL = (1710.2 + 0.2 * (Arfcn - 512)).toFloat()
                FDL = FUL + 95
            }
        }
    }



    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()

    }
}

