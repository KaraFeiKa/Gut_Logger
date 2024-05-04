package com.example.testkotlin.Info

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthWcdma
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testkotlin.MainActivity
import com.example.testkotlin.R
import com.example.testkotlin.TrafficSpeed.ITrafficSpeedListener
import com.example.testkotlin.TrafficSpeed.TrafficSpeedMeasurer
import com.example.testkotlin.TrafficSpeed.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


@Suppress("IMPLICIT_CAST_TO_ANY")
class ServiceBack: Service() {
    private lateinit var tm:TelephonyManager
    val myTelephonyCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MyTelephonyCallback(this)
    } else {
        MyPhoneStateListener(this)
    }
    val myTelephonyCallbackNeiborhood = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MyTelephonyCallbackNeiborhood(this)
    } else {
        MyPhoneStateListenerNeiborhood(this)
    }
    val myTelephonyCallbackCall = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MyTelephonyCallbackCall(this)
    } else {
        MyPhoneStateListenerCall(this)
    }
    val myTelephonyCallbackState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MyTelephonyCallbackState(this)
    } else {
        MyPhoneStateListenerState(this)
    }
    val myTelephonyCallbackSignalStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MyTelephonyCallbackSignalStrength(this)
    } else {
        MyPhoneStateListenerSignal(this)
    }


    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locRequest: LocationRequest
    private val SHOW_SPEED_IN_BITS = false

    private lateinit var mTrafficSpeedMeasurer: TrafficSpeedMeasurer


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startLocUp()

        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()

        startNotification()
        initLocation()
        tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(mainExecutor, myTelephonyCallback as TelephonyCallback)
        }else{
            tm.listen(myTelephonyCallback as PhoneStateListener?, PhoneStateListener.LISTEN_CELL_INFO)
        }
        mTrafficSpeedMeasurer = TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.MOBILE)
        mTrafficSpeedMeasurer.startMeasuring()
        mTrafficSpeedMeasurer.registerListener(mStreamSpeedListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(mainExecutor,
                myTelephonyCallbackSignalStrength as TelephonyCallback
            )
        }else{
            tm.listen(myTelephonyCallbackSignalStrength as PhoneStateListener?, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(mainExecutor,
                myTelephonyCallbackState as TelephonyCallback
            )
        }else{
            tm.listen(myTelephonyCallbackState as PhoneStateListener?, PhoneStateListener.LISTEN_SERVICE_STATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(mainExecutor, myTelephonyCallbackCall as TelephonyCallback)
        }else{
            tm.listen(myTelephonyCallbackCall as PhoneStateListener?, PhoneStateListener.LISTEN_CALL_STATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(mainExecutor,
                myTelephonyCallbackNeiborhood as TelephonyCallback
            )
        }else{
            tm.listen(myTelephonyCallbackNeiborhood as PhoneStateListener?, PhoneStateListener.LISTEN_CELL_INFO)
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    class MyTelephonyCallbackSignalStrength(val activity: ServiceBack) : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener{
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            val strengthAmplitude = signalStrength.cellSignalStrengths
            for (cellSignalStrength in strengthAmplitude) {
                if (cellSignalStrength is CellSignalStrengthLte) {
                    Log.d("Signal2", cellSignalStrength.toString())

//                    val signalModel=SignalModel(
////                    cellSignalStrength.rssi,
////                    cellSignalStrength.rsrp,
////                    cellSignalStrength.rsrq,
////                    cellSignalStrength.rssnr,
////                    cellSignalStrength.cqi,
//                    )
//                    activity.sendSignData(signalModel)
                }
            }
        }
    }
    class MyPhoneStateListenerSignal(val activity: ServiceBack) :PhoneStateListener(){
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            val strengthAmplitude = signalStrength?.cellSignalStrengths
            for (cellSignalStrength in strengthAmplitude!!) {
                if (cellSignalStrength is CellSignalStrengthLte) {
                    Log.d("Signal2", cellSignalStrength.toString())

//                    val signalModel=SignalModel(
////                    cellSignalStrength.rssi,
////                    cellSignalStrength.rsrp,
////                    cellSignalStrength.rsrq,
////                    cellSignalStrength.rssnr,
////                    cellSignalStrength.cqi,
//                    )
//                    activity.sendSignData(signalModel)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    class MyTelephonyCallbackState(val activity: ServiceBack) : TelephonyCallback(), TelephonyCallback.ServiceStateListener {

        override fun onServiceStateChanged(serviceState: ServiceState) {

            val bwModel = BWModel(
                serviceState

            )
            activity.BwSandData(bwModel)
        }
    }

class MyPhoneStateListenerState(val activity: ServiceBack) : PhoneStateListener(){
    override fun onServiceStateChanged(serviceState: ServiceState?) {
        super.onServiceStateChanged(serviceState)
        val bwModel = serviceState?.let {
            BWModel(
                it

            )
        }
        if (bwModel != null) {
            activity.BwSandData(bwModel)
        }
    }

}

    @RequiresApi(Build.VERSION_CODES.S)
    class MyTelephonyCallbackCall(val activity: ServiceBack) : TelephonyCallback(),TelephonyCallback.CallStateListener{
        override fun onCallStateChanged(state: Int) {
                var idle =""
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    idle = "IDLE"
                }

                TelephonyManager.CALL_STATE_RINGING -> {
                    idle = "RINGING"
                }

                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    idle = "OFFHOOK"
                }

            }
            val callInfoModel = CallInfoModel(
                idle
            )
            activity.sendCallData(callInfoModel)
        }
    }

    class MyPhoneStateListenerCall(val activity: ServiceBack): PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            var idle =""
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    idle = "IDLE"
                }

                TelephonyManager.CALL_STATE_RINGING -> {
                    idle = "RINGING"
                }

                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    idle = "OFFHOOK"
                }

            }
            val callInfoModel = CallInfoModel(
                idle
            )
            activity.sendCallData(callInfoModel)
            super.onCallStateChanged(state, phoneNumber)
        }
    }

    fun DecToHex(dec: Int): String {
        return dec.toString(16)
    }

    fun HexToDec(hex: String): Int {
        return hex.toInt(16)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    class MyTelephonyCallback(val activity: ServiceBack) : TelephonyCallback(), TelephonyCallback.CellInfoListener{
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
            cellInfo.forEach { cell ->
                if (cell is CellInfoLte){
                    if (cell.isRegistered){
                        isNetworksType = "4G"
                        Log.d("Signal11", cell.cellSignalStrength.toString())
                        val signalModel=SignalModel(
                            "4G",
                            cell.cellSignalStrength.rssi,
                            cell.cellSignalStrength.rsrp,
                            cell.cellSignalStrength.rsrq,
                            cell.cellSignalStrength.rssnr,
                            cell.cellSignalStrength.cqi,
                            cell.cellSignalStrength.timingAdvance,
                        )
                        activity.sendSignData(signalModel)
                        var bands = cell.cellIdentity.bands
                        var band = if (bands.isNotEmpty()){
                            bands[0]
                        } else { }
                        val CID = cell.cellIdentity.ci
                        val cellidHex = activity.DecToHex(CID)
                        val eNBHex = cellidHex.substring(0, cellidHex.length - 2)

                        val bsInfoModel= cell.cellIdentity.mccString?.let {
                            cell.cellIdentity.mncString?.let { it1 ->
                                BsInfoModel(
                                    "4G",
                                    it,
                                    it1,
                                    cell.cellIdentity.ci,
                                    band,
                                    activity.HexToDec(eNBHex),
                                    cell.cellIdentity.earfcn,
                                    cell.cellIdentity.pci,
                                    cell.cellIdentity.tac,
                                    cell.cellIdentity.operatorAlphaLong,
                                )
                            }
                        }
                        bsInfoModel?.let { activity.sendBSData(it) }

                    }
                }
                if (cell is CellInfoWcdma){
                    if (cell.isRegistered){
                        isNetworksType = "3G"
                        val CellSignalStrengthArr: List<String> =cell.cellSignalStrength.toString().split(" ")
                        var ss = 0
                        if (CellSignalStrengthArr.size > 1) {
                            val elem = CellSignalStrengthArr[1].split("=".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            if (elem[0].contains("ss")) {
                                ss = elem[1].toInt()
                            }
                        }
                        val signalModel=SignalModel(
                            "3G",
                            ss,
                            cell.cellSignalStrength.dbm,
                            0,
                            cell.cellSignalStrength.ecNo
                        )
                        activity.sendSignData(signalModel)

                        var RNCID = cell.cellIdentity.cid / 65536;
                        val bsInfoModel = cell.cellIdentity.mccString?.let {
                            cell.cellIdentity.mncString?.let { it1 ->
                                BsInfoModel(
                                    "3G",
                                    it,
                                    it1,
                                    cell.cellIdentity.cid,
                                    0,
                                    RNCID,
                                    cell.cellIdentity.uarfcn,
                                    cell.cellIdentity.psc,
                                    cell.cellIdentity.lac,
                                    cell.cellIdentity.operatorAlphaLong
                                )
                            }
                        }
                        bsInfoModel?.let { activity.sendBSData(it) }
                    }
                }
                if (cell is CellInfoGsm){
                    if (cell.isRegistered){
                        isNetworksType = "2G"
                        val signalModel=SignalModel(
                            "2G",
                            cell.cellSignalStrength.rssi,
                            cell.cellSignalStrength.dbm,
                            0,
                            cell.cellSignalStrength.bitErrorRate,
                            0,
                            cell.cellSignalStrength.timingAdvance
                        )
                        activity.sendSignData(signalModel)
                        val bsInfoModel = cell.cellIdentity.mccString?.let {
                            cell.cellIdentity.mncString?.let { it1 ->
                                BsInfoModel(
                                    "2G",
                                    it,
                                    it1,
                                    cell.cellIdentity.cid,
                                    0,
                                    0,
                                    cell.cellIdentity.arfcn,
                                    cell.cellIdentity.bsic,
                                    cell.cellIdentity.lac,
                                    cell.cellIdentity.operatorAlphaLong
                                )
                            }
                        }
                        bsInfoModel?.let { activity.sendBSData(it) }

                    }
                }
            }
        }
    }
    class MyPhoneStateListener(val activity: ServiceBack): PhoneStateListener(){
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED || androidx.core.app.ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.READ_PHONE_STATE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            cellInfo?.forEach { cell ->
                if (cell is CellInfoLte){
                    if (cell.isRegistered){
                        isNetworksType = "4G"
                        Log.d("Signal12", cell.cellSignalStrength.toString())
                        val signalModel=SignalModel(
                            "4G",
                            cell.cellSignalStrength.rssi,
                            cell.cellSignalStrength.rsrp,
                            cell.cellSignalStrength.rsrq,
                            cell.cellSignalStrength.rssnr,
                            cell.cellSignalStrength.cqi,
                            cell.cellSignalStrength.timingAdvance,
                        )
                        activity.sendSignData(signalModel)
                        var bands = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            cell.cellIdentity.bands
                        } else {
                            null
                        }
                        var band = if (bands?.isNotEmpty() == true){
                            bands[0]
                        } else { 0 }
                        val CID = cell.cellIdentity.ci
                        val cellidHex = activity.DecToHex(CID)
                        val eNBHex = cellidHex.substring(0, cellidHex.length - 2)

                        val bsInfoModel= cell.cellIdentity.mccString?.let {
                            cell.cellIdentity.mncString?.let { it1 ->
                                BsInfoModel(
                                        "4G",
                                        it,
                                        it1,
                                        cell.cellIdentity.ci,
                                        band,
                                        activity.HexToDec(eNBHex),
                                        cell.cellIdentity.earfcn,
                                        cell.cellIdentity.pci,
                                        cell.cellIdentity.tac,
                                        cell.cellIdentity.operatorAlphaLong,
                                    )
                                }
                            }
                        bsInfoModel?.let { activity.sendBSData(it) }

                    }
                }
                if (cell is CellInfoWcdma){
                    if (cell.isRegistered){
                        isNetworksType = "3G"

                        val CellSignalStrengthArr: List<String> =cell.cellSignalStrength.toString().split(" ")
                        var ss = 0
                        if (CellSignalStrengthArr.size > 1) {
                            val elem = CellSignalStrengthArr[1].split("=".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            if (elem[0].contains("ss")) {
                                ss = elem[1].toInt()
                            }
                        }
                       var ecno = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            cell.cellSignalStrength.ecNo
                        }else{
                            0
                        }
                        val signalModel=SignalModel(
                            "3G",
                            ss,
                            cell.cellSignalStrength.dbm,
                            0,
                            ecno

                        )
                        activity.sendSignData(signalModel)

                        var RNCID = cell.cellIdentity.cid / 65536;
                        val bsInfoModel = cell.cellIdentity.mccString?.let {
                            cell.cellIdentity.mncString?.let { it1 ->
                                BsInfoModel(
                                    "3G",
                                    it,
                                    it1,
                                    cell.cellIdentity.cid,
                                    0,
                                    RNCID,
                                    cell.cellIdentity.uarfcn,
                                    cell.cellIdentity.psc,
                                    cell.cellIdentity.lac,
                                    cell.cellIdentity.operatorAlphaLong
                                )
                            }
                        }
                        bsInfoModel?.let { activity.sendBSData(it) }
                    }
                }
                if (cell is CellInfoGsm){
                    if (cell.isRegistered){
                        isNetworksType = "2G"

                        var rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            cell.cellSignalStrength.rssi
                        } else {
                            0
                        }
                        val signalModel=SignalModel(
                            "2G",
                            rssi,
                            cell.cellSignalStrength.dbm,
                            0,
                            cell.cellSignalStrength.bitErrorRate,
                            0,
                            cell.cellSignalStrength.timingAdvance
                        )
                        activity.sendSignData(signalModel)
                        val bsInfoModel = cell.cellIdentity.mccString?.let {
                            cell.cellIdentity.mncString?.let { it1 ->
                                BsInfoModel(
                                    "2G",
                                    it,
                                    it1,
                                    cell.cellIdentity.cid,
                                    0,
                                    0,
                                    cell.cellIdentity.arfcn,
                                    cell.cellIdentity.bsic,
                                    cell.cellIdentity.lac,
                                    cell.cellIdentity.operatorAlphaLong
                                )
                            }
                        }
                        bsInfoModel?.let { activity.sendBSData(it) }

                    }
                }
            }
            super.onCellInfoChanged(cellInfo)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    class MyTelephonyCallbackNeiborhood(val activity: ServiceBack) : TelephonyCallback(), TelephonyCallback.CellInfoListener{
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
            var neighbours = ArrayList<InfoNeiborhood>()
            cellInfo.forEach { cell ->
                if (cell is CellInfoLte) {
                    if (!cell.isRegistered) {

                        var bands = cell.cellIdentity.bands

                        var band = if (bands.isNotEmpty()){
                            bands[0]
                        } else { 0 }

                        val infoNeiborhood=InfoNeiborhood(
                            "4G",
                            cell.cellIdentity.pci,
                            cell.cellIdentity.earfcn,
                            band,
                            cell.cellSignalStrength.rssi,
                            cell.cellSignalStrength.rsrp,
                            cell.cellSignalStrength.rsrq,
                            cell.cellSignalStrength.timingAdvance
                        )
                        neighbours.add(infoNeiborhood)
                    }
                }
                if (cell is CellInfoWcdma){
                    if (!cell.isRegistered) {
                        val CellSignalStrengthArr: List<String> =cell.cellSignalStrength.toString().split(" ")
                        var ss = 0
                        if (CellSignalStrengthArr.size > 1) {
                            val elem = CellSignalStrengthArr[1].split("=".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            if (elem[0].contains("ss")) {
                                ss = elem[1].toInt()
                            }
                        }
                        val infoNeiborhood=InfoNeiborhood(
                            "3G",
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            cell.cellIdentity.psc,
                            cell.cellIdentity.uarfcn,
                            ss

                        )
                        neighbours.add(infoNeiborhood)
                    }
                }
                if (cell is CellInfoGsm) {
                    if (!cell.isRegistered) {
                        val infoNeiborhood = InfoNeiborhood(
                            "2G",
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            cell.cellIdentity.lac,
                            cell.cellIdentity.cid,
                            cell.cellIdentity.arfcn,
                            cell.cellIdentity.bsic,
                            cell.cellSignalStrength.rssi
                        )
                        neighbours.add(infoNeiborhood)
                    }
                }
            }
            activity.sendInfoNeighbours(neighbours)
        }
    }
    class MyPhoneStateListenerNeiborhood (val activity: ServiceBack): PhoneStateListener(){
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED || androidx.core.app.ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.READ_PHONE_STATE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            var neighbours = ArrayList<InfoNeiborhood>()
            if (cellInfo != null) {
                cellInfo.forEach { cell ->
                    if (cell is CellInfoLte) {
                        if (!cell.isRegistered) {

                            var bands = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                cell.cellIdentity.bands
                            } else {
                                null
                            }
                            var band = if (bands?.isNotEmpty() == true){
                                bands[0]
                            } else { 0 }

                            val infoNeiborhood=InfoNeiborhood(
                                "4G",
                                cell.cellIdentity.pci,
                                cell.cellIdentity.earfcn,
                                band,
                                cell.cellSignalStrength.rssi,
                                cell.cellSignalStrength.rsrp,
                                cell.cellSignalStrength.rsrq,
                                cell.cellSignalStrength.timingAdvance
                            )
                            neighbours.add(infoNeiborhood)
                        }
                    }
                    if (cell is CellInfoWcdma){
                        if (!cell.isRegistered) {
                            val CellSignalStrengthArr: List<String> =cell.cellSignalStrength.toString().split(" ")
                            var ss = 0
                            if (CellSignalStrengthArr.size > 1) {
                                val elem = CellSignalStrengthArr[1].split("=".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                                if (elem[0].contains("ss")) {
                                    ss = elem[1].toInt()
                                }
                            }
                            val infoNeiborhood=InfoNeiborhood(
                                "3G",
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                cell.cellIdentity.psc,
                                cell.cellIdentity.uarfcn,
                                ss

                            )
                            neighbours.add(infoNeiborhood)
                        }
                    }
                    if (cell is CellInfoGsm) {
                        if (!cell.isRegistered) {
                            var rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                cell.cellSignalStrength.rssi
                            } else {
                               0
                            }
                            val infoNeiborhood = InfoNeiborhood(
                                "2G",
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                cell.cellIdentity.lac,
                                cell.cellIdentity.cid,
                                cell.cellIdentity.arfcn,
                                cell.cellIdentity.bsic,
                                rssi
                            )
                            neighbours.add(infoNeiborhood)
                        }
                    }
                }
            }
            activity.sendInfoNeighbours(neighbours)
            super.onCellInfoChanged(cellInfo)
        }

    }




    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChannel)
        }

        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            10,
            nIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.drawable.noc_logo)
            .setContentTitle("Сервис запущен!")
            .setContentIntent(pIntent).build()
        startForeground(99, notification)
    }
    private fun initLocation(){
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)
        locRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000)
            .setMinUpdateDistanceMeters(7F)
            .build()

    }
    private val locCallBack = object : LocationCallback(){
        override fun onLocationResult(locRes: LocationResult) {
            super.onLocationResult(locRes)
            val locModel = LocationModel(
                locRes.lastLocation!!.latitude,
                locRes.lastLocation!!.longitude,
            )
            sendLocData(locModel)

        }
    }
    private fun sendLocData(locModel: LocationModel){
        val i = Intent(LOC_MODLE_INTENT)
        i.putExtra(LOC_MODLE_INTENT, locModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
    }
    private fun sendSpeedData(speedModel:SpeedModel){
        val s = Intent(Speed_MODLE_INTENT)
        s.putExtra(Speed_MODLE_INTENT, speedModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(s)
    }
    private fun sendSignData(signModel: SignalModel){
        val q = Intent(SIGNAL_MODLE_INTENT)
        q.putExtra(SIGNAL_MODLE_INTENT, signModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(q)

    }
    private fun sendBSData(bsInfoModel: BsInfoModel){
        val w = Intent(BS_MODLE_INTENT)
        w.putExtra(BS_MODLE_INTENT, bsInfoModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(w)

    }
private fun BwSandData (bwModel: BWModel){
    val bw = Intent(BW_MODLE_INTENT)
    bw.putExtra(BW_MODLE_INTENT,bwModel)
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(bw)
}
    private fun sendCallData(callInfoData: CallInfoModel){
      val c = Intent(Call_MODLE_INTENT)
       c.putExtra(Call_MODLE_INTENT, callInfoData)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(c)
   }

    private fun sendInfoNeighbours (infoNeighbours: ArrayList<InfoNeiborhood>){
        val neighbours = Intent(INFO_NEIGHBOURS)
        neighbours.putExtra(INFO_NEIGHBOURS, infoNeighbours)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(neighbours)
    }
    private fun startLocUp(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        locProvider.requestLocationUpdates(
            locRequest,
            locCallBack,
            Looper.myLooper()
        )
    }


    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.unregisterTelephonyCallback(myTelephonyCallbackSignalStrength as TelephonyCallback)
            tm.unregisterTelephonyCallback(myTelephonyCallback as TelephonyCallback)
            tm.unregisterTelephonyCallback(myTelephonyCallbackCall as TelephonyCallback)
            tm.unregisterTelephonyCallback(myTelephonyCallbackNeiborhood as TelephonyCallback)
            tm.unregisterTelephonyCallback(myTelephonyCallbackState as TelephonyCallback)
        }else{
            tm.listen(myTelephonyCallbackSignalStrength as PhoneStateListener?, PhoneStateListener.LISTEN_NONE)
            tm.listen(myTelephonyCallback as PhoneStateListener?, PhoneStateListener.LISTEN_NONE)
            tm.listen(myTelephonyCallbackCall as PhoneStateListener?, PhoneStateListener.LISTEN_NONE)
            tm.listen(myTelephonyCallbackNeiborhood as PhoneStateListener?, PhoneStateListener.LISTEN_NONE)
            tm.listen(myTelephonyCallbackState as PhoneStateListener?, PhoneStateListener.LISTEN_NONE)
        }

        mTrafficSpeedMeasurer.stopMeasuring()
        locProvider.removeLocationUpdates(locCallBack)
    }

    private val mStreamSpeedListener = object : ITrafficSpeedListener {
        override fun onTrafficSpeedMeasured(upStream: Double, downStream: Double) {
            val speedModel = SpeedModel (

                Utils.parseSpeed(upStream, SHOW_SPEED_IN_BITS),
                Utils.parseSpeed(downStream, SHOW_SPEED_IN_BITS)
            )
                sendSpeedData(speedModel)
        }
    }



    companion object{
        const val Speed_MODLE_INTENT = "Speed_intent"
        const val CHANNEL_ID = "channel_1"
        const val LOC_MODLE_INTENT = "loc_intent"
        const val SIGNAL_MODLE_INTENT = "signal_intent"
        const val BS_MODLE_INTENT = "BS_intent"
        const val Call_MODLE_INTENT = "Call_intent"
        const val BW_MODLE_INTENT = "BW_intent"
        const val INFO_NEIGHBOURS = "INFO_NEIGHBOURS"
        var WriterIsWorking = false
        var isNetworksType = ""


    }
}