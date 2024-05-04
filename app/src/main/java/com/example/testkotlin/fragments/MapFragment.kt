package com.example.testkotlin.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.testkotlin.MainViewModel
import com.example.testkotlin.R
import com.example.testkotlin.databinding.FragmentMapBinding
import com.example.testkotlin.fragmentsimport.SignalStrengthServer
import com.example.testkotlin.utils.checkPermission
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment.LEFT
import com.yandex.mapkit.logo.VerticalAlignment.BOTTOM
import com.yandex.runtime.image.ImageProvider.fromResource

import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch

class MapFragment : Fragment(), UserLocationObjectListener, CameraListener {



    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMapBinding
    private lateinit var locationmapkit: UserLocationLayer
    private var routeStartLocation = Point(0.0, 0.0)
    private var permissionLocation = false
    private var followUserLocation = false
    private val model : MainViewModel by activityViewModels()
    var lat: Double  = 0.0
    var lon: Double  = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



//        settOsm()
        setYandex()
        binding = FragmentMapBinding.inflate(inflater, container, false)
        var mapKit = MapKitFactory.getInstance()
        locationmapkit = mapKit.createUserLocationLayer(binding.yandex.mapWindow)
        userInterface()



        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissions()
        locationUpdate()
    }

    private fun locationUpdate() =with(binding){
        model.locationUpdates.observe(viewLifecycleOwner){
            lat = it.lat
            lon = it.lon
            lifecycleScope.launch {
                SignalStrengthServer()
            }

//            val points = listOf(
//                Point(59.935493, 30.327392),
//                Point(59.938185, 30.32808),
//                Point(59.937376, 30.33621),
//                Point(59.934517, 30.335059),
//            )
//            val polyline = Polyline(points)
//
//
//
//            val polylineObject = binding.yandex.map.mapObjects.addPolyline(polyline)
//
//            polylineObject.apply {
//                strokeWidth = 5f
//                setStrokeColor(ContextCompat.getColor(requireActivity(), R.color.Grey))
//                outlineWidth = 1f
//                outlineColor = ContextCompat.getColor(requireActivity(), R.color.black)
//            }


        }
    }


    suspend fun SignalStrengthServer() {
        val baseURL = "http://ss.sut.dchudinov.ru/api/v1"
        val login = "roma"
        val password = "romaromaroman"
        val server =
            com.example.testkotlin.fragmentsimport.SignalStrengthServer(baseURL, login, password)
//    http://ss.sut.dchudinov.ru/api/v1/cells?lat=59.903119&long=30.488665&radius=0.001
        val lat: Double = lat
        val long: Double = lon
        val radius: Double = 0.0015
        var points = ArrayList<Point>();
        try {
            var cells = server.getCells(lat, long, radius)

            Log.d("Cells", cells.cells.toString())
            for (cell in cells.cells) {
                points.add(Point(cell.lat, cell.long))
            }
        }catch (ex: Exception) {
            Log.d("Cells", ex.toString())
        }

        val pinsCollection = binding.yandex.map.mapObjects.addCollection()

        val imageProvider = fromResource(activity, R.mipmap.bs_for_map_foreground)

        points.forEach { point ->
            pinsCollection.addPlacemark().apply {
                geometry = point
                setIcon(imageProvider)
            }
        }

//    lat=59.90358805&long=30.48996893&radius=0.02&
////        mnc=2&network=4G&dateStart=2023-08-31&dateEnd=2024-04-23
    }

    private fun setYandex(){
        MapKitFactory.initialize(activity)
        binding = FragmentMapBinding.inflate(layoutInflater)

    }



    private fun initYandex(){
        locationmapkit.isVisible = true
        locationmapkit.isHeadingEnabled = true
        locationmapkit.setObjectListener(this)

        binding.yandex.map.addCameraListener(this)

        cameraUserPosition()
        permissionLocation = true
    }
    private fun cameraUserPosition() {
        if (locationmapkit.cameraPosition() != null) {
            routeStartLocation = locationmapkit.cameraPosition()!!.target
            binding.yandex.map.move(
                CameraPosition(routeStartLocation, 16f, 0f, 0f), Animation(Animation.Type.SMOOTH, 1f), null
            )
        } else {
            binding.yandex.map.move(CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f))
        }
    }



    private fun userInterface() {
        val mapLogoAlignment = Alignment(LEFT, BOTTOM)
        binding.yandex.map.logo.setAlignment(mapLogoAlignment)

        binding.floatingActionButton7.setOnClickListener {
            if (permissionLocation) {
                cameraUserPosition()

                followUserLocation = true
            } else {
                checkLocPermission()
            }
        }
    }
    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateReason, finish: Boolean
    ) {

        if (finish) {
            if (followUserLocation) {
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }
    private fun setAnchor() {
        locationmapkit.setAnchor(
            PointF(
                (binding.yandex.width * 0.5).toFloat(), (binding.yandex.height * 0.5).toFloat()
            ),
            PointF(
                (binding.yandex.width * 0.5).toFloat(), (binding.yandex.height * 0.83).toFloat()
            )
        )

        binding.floatingActionButton7.setImageResource(R.drawable.free_icon_gps_1161225)

        followUserLocation = false
    }

    private fun noAnchor() {
        locationmapkit.resetAnchor()

        binding.floatingActionButton7.setImageResource(R.drawable.ic_location_searching_black_24dp)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        userLocationView.pin.setIcon(fromResource(context, R.mipmap.man_phone_foreground))
        userLocationView.arrow.setIcon(fromResource(context, R.mipmap.man_phone_foreground))
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}

    override fun onObjectRemoved(p0: UserLocationView) {}

// НАСТРОЙКА КАРТ OSM НАЧАЛО

//    private fun settOsm(){
//        Configuration.getInstance().load(activity as AppCompatActivity, activity?.getSharedPreferences("osm_pref",
//            Context.MODE_PRIVATE))
//        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
//    }
//
//    private fun initOsm() = with(binding){
//        OSMmap.controller.setZoom(20.0)
//        val mLocProvider = GpsMyLocationProvider(activity)
//        val mLocOverlay = MyLocationNewOverlay(mLocProvider,OSMmap)
//        mLocOverlay.enableMyLocation()
//        mLocOverlay.enableFollowLocation()
//        mLocOverlay.runOnFirstFix {
//            OSMmap.overlays.clear()
//            OSMmap.overlays.add(mLocOverlay)
//        }
//    }

    // НАСТРОЙКА КАРТ OSM КОНЕЦ

    // РАЗРЕШЕНИЯ НАЧАЛО

    private fun registerPermissions(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            if(it[Manifest.permission.ACCESS_FINE_LOCATION] == true){
//                initOsm()
                initYandex()
                checkLocationEnabled()
            } else {
                Toast.makeText(activity, "Вы не дали разрешения на использование местоположения!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        ) {
//            initOsm()
            initYandex()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun checkPermissionBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//            initOsm()
            initYandex()
            checkLocationEnabled()
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun checkLocationEnabled(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!isEnabled){
            Toast.makeText(activity, "GPS выключен!", Toast.LENGTH_LONG).show()
        }
    }

    // РАЗРЕШЕНИЯ КОНЕЦ
    override fun onStart() {
        binding.yandex.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        checkLocPermission()
    }

    override fun onStop() {
        binding.yandex.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }




    companion object {
        @JvmStatic
        fun newInstance() = MapFragment()
        const val MAPKIT_API_KEY = "42b5d92d-c3e0-4099-889b-6957e67f57a2"
    }




}


