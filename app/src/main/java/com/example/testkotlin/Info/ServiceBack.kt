package com.example.testkotlin.Info

import android.Manifest
import android.annotation.SuppressLint
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
import android.telephony.CellInfoLte
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

class ServiceBack: Service() {
    private lateinit var tm:TelephonyManager
    val myTelephonyCallback = MyTelephonyCallback(this)
//    val myTelephonyCallbackSignalStrength = MyTelephonyCallbackSignalStrength(this)

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

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        Log.d("life service", "Create")
        startNotification()
        initLocation()
        tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        tm.registerTelephonyCallback(mainExecutor, myTelephonyCallback)
        mTrafficSpeedMeasurer = TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.MOBILE)
        mTrafficSpeedMeasurer.startMeasuring()
        mTrafficSpeedMeasurer.registerListener(mStreamSpeedListener);
//        tm.registerTelephonyCallback(mainExecutor, myTelephonyCallbackSignalStrength)
    }


    fun DecToHex(dec: Int): String {
        return dec.toString(16)
    }

    fun HexToDec(hex: String): Int {
        return hex.toInt(16)
    }
    @SuppressLint("NewApi")
    class MyTelephonyCallback(val activity: ServiceBack) : TelephonyCallback(), TelephonyCallback.CellInfoListener{
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
            cellInfo.forEach { cell ->
                if (cell is CellInfoLte){
                    if (cell.isRegistered){
                        Log.d("Signal1", cell.cellSignalStrength.toString())
                        val signalModel=SignalModel(
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
                        } else {

                        }
                        val CID = cell.cellIdentity.ci
                        val cellidHex = activity.DecToHex(CID)
                        val eNBHex = cellidHex.substring(0, cellidHex.length - 2)

                        val bsInfoModel= cell.cellIdentity.mccString?.let {
                            cell.cellIdentity.mncString?.let { it1 ->
                                BsInfoModel(
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
            }
        }
    }



//    @SuppressLint("NewApi")
//    class MyTelephonyCallbackSignalStrength(val activity: ServiceBack) : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener{
//        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
//            val strengthAmplitude = signalStrength.cellSignalStrengths
//            for (cellSignalStrength in strengthAmplitude) {
//                if (cellSignalStrength is CellSignalStrengthLte) {
//                    Log.d("Signal2", cellSignalStrength.toString())
//                    val signalModel=SignalModel(
//                    cellSignalStrength.rssi,
//                    cellSignalStrength.rsrp,
//                    cellSignalStrength.rsrq,
//                    cellSignalStrength.rssnr,
//                    cellSignalStrength.cqi,
//                    )
//                    activity.sendSignData(signalModel)
//                }
//            }
//        }
//    }


//    private val cc = MyTelephonyCallbackSignalStrength(this)
    private val cc2 = MyTelephonyCallback(this)
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
            .setMinUpdateDistanceMeters(5F)
            .build()

    }

    private val locCallBack = object : LocationCallback(){
        override fun onLocationResult(locRes: LocationResult) {
            super.onLocationResult(locRes)
            Log.d("GPS",locRes.lastLocation.toString())
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

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onDestroy() {
        super.onDestroy()
        Log.d("life service", "Destroy")
//               tm.unregisterTelephonyCallback(myTelephonyCallbackSignalStrength)
                tm.unregisterTelephonyCallback(myTelephonyCallback)
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

    private fun sendSpeedData(speedModel:SpeedModel){
        val s = Intent(Speed_MODLE_INTENT)
        s.putExtra(Speed_MODLE_INTENT, speedModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(s)
    }


    companion object{
        const val Speed_MODLE_INTENT = "Speed_intent"
        const val CHANNEL_ID = "channel_1"
        const val LOC_MODLE_INTENT = "loc_intent"
        const val SIGNAL_MODLE_INTENT = "signal_intent"
        const val BS_MODLE_INTENT = "BS_intent"

    }
}