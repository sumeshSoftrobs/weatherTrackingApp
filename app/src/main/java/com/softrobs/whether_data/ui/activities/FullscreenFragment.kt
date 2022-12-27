package com.softrobs.whether_data.ui.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.softrobs.whether_data.R
import com.softrobs.whether_data.data.remote.network.api_state_management.ApiState
import com.softrobs.whether_data.data.remote.response.ResponseFromForecastModel
import com.softrobs.whether_data.data.remote.response.WeatherData
import com.softrobs.whether_data.databinding.FragmentFullscreenBinding
import com.softrobs.whether_data.ui.adapters.WeatherForecastRecyclerAdapter
import com.softrobs.whether_data.utils.API_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class FullscreenFragment : Fragment(R.layout.fragment_fullscreen) {
    private val hideHandler = Handler(Looper.myLooper()!!)

    private var lat:Double? = null
    private var lon:Double? = null
    private lateinit var adapter:WeatherForecastRecyclerAdapter

    private val viewModel by viewModels<MainActivityViewModel>()
    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var dummyButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentFullscreenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var currentDate:String? = null
    private var calendarDate:String? = null
    private val myCalendar = Calendar.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFullscreenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        val date =
            OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                val myFormat = "yyyy-MM-dd"
                val dateFormat = SimpleDateFormat(myFormat, Locale.US)
                currentDate = dateFormat.format(myCalendar.time)
                calendarDate = dateFormat.format(myCalendar.time)
                wetherForcastRequest()
                observeWetherResponse()
            }
        _binding?.selectDateImg?.setOnClickListener(View.OnClickListener {
            DatePickerDialog(
                requireContext(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        })

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (lat == null || lon == null){
            getLocation()
        }else{
//            Toast.makeText(requireContext(),"Wehtjer",Toast.LENGTH_LONG).show()
            wetherForcastRequest()

            observeWetherResponse()
        }


    }
    private fun observeWetherResponse(){
        lifecycleScope.launchWhenStarted {
            with(viewModel){
                observeWeatherData.collect{ res ->

//                    Log.d("EXCEPTION_FROM_LOGIN_API" , res.toString())
                    when(res){
                        is ApiState.SUCCESS ->{
                            val it = res.getResponse.list

                            updateUiwithNewData(res.getResponse)
                            Log.i("WetherResponse : ","${res.getResponse.toString()}")
                        }
                        is ApiState.LOADING -> {
                            Toast.makeText(requireContext(),"Loading...",
                                Toast.LENGTH_LONG).show()
                        }
                        is ApiState.ERROR -> {
                            Toast.makeText(requireContext(),"Something went wrong...${res.message}",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUiwithNewData(response: ResponseFromForecastModel) {

        if (calendarDate == null){
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val newdate = Date()
            currentDate = formatter.format(newdate)
        }

        val newResp = response.list
//        Toast.makeText(requireContext(),newResp.toString(),Toast.LENGTH_LONG).show()

        val tempListToday = ArrayList<WeatherData>()
        val dtData = newResp[0].dt_txt.split(" ")
        newResp.forEach{
            val dateTime = it.dt_txt.split(" ")
            if (dateTime[0].toString() == currentDate.toString()){
                tempListToday.add(it)
            }
        }
        _binding?.tempDataRecycler?.setHasFixedSize(true)
        _binding?.tempDataRecycler?.layoutManager = LinearLayoutManager(requireContext())
        _binding?.tempDataRecycler?.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        adapter = WeatherForecastRecyclerAdapter(tempListToday,requireContext())
        _binding?.tempDataRecycler?.adapter = adapter


        _binding?.temperatureTxt?.text = "${tempListToday[0].main.temp.toString()}°"
        _binding?.wetherTxt?.text = tempListToday[0].weather[0].main

        _binding?.humTxt?.text = tempListToday[0].main.humidity.toString()
        _binding?.percievedTempTxt?.text = tempListToday[0].main.temp.toString()
        _binding?.visibilityTxt?.text = tempListToday[0].visibility.toString()
        _binding?.windSpeedTxt?.text = tempListToday[0].wind.speed.toString()

        val lineGraphView = _binding?.idGraphView

        // on below line we are adding data to our graph view.
        val series: LineGraphSeries<DataPoint> = LineGraphSeries(
            arrayOf(
                // on below line we are adding
                // each point on our x and y axis.
                DataPoint(0.0, 1.0),
                DataPoint(1.0, 3.0),
                DataPoint(2.0, 4.0),
                DataPoint(3.0, 9.0),
                DataPoint(4.0, 6.0),
                DataPoint(5.0, 3.0),
                DataPoint(6.0, 6.0),
                DataPoint(7.0, 1.0),
                DataPoint(8.0, 2.0)
            )
        )

        // on below line adding animation
        lineGraphView?.animate()

        // on below line we are setting scrollable
        // for point graph view
        lineGraphView?.viewport?.isScrollable = true

        // on below line we are setting scalable.
        lineGraphView?.viewport?.isScalable = true

        // on below line we are setting scalable y
        lineGraphView?.viewport?.setScalableY(true)

        // on below line we are setting scrollable y
        lineGraphView?.viewport?.setScrollableY(true)

        // on below line we are setting color for series.
        series.color = R.color.purple_200

        // on below line we are adding
        // data series to our graph view.
        lineGraphView?.addSeries(series)
        Log.d("Filtered List :","${tempListToday.toString()}")

    }


    private fun wetherForcastRequest() {
        viewModel.invokeWetherForecastCall(23.5,25.3, API_KEY, "json","metric")
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        lat = location.latitude
                        lon = location.longitude
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        _binding?.apply {

                            locationCity.text = "${list[0].locality}"
                            locationStateCountry.text = "${list[0].subLocality} , ${list[0].countryName}"
//                            tvLocality.text = "Locality\n"
//                            tvAddress.text = "Address\n${list[0].getAddressLine(0)}"
//                            wetherForcastRequest()
//                            observeWetherResponse()
                        }
                        wetherForcastRequest()
                        observeWetherResponse()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun checkPermissions():Boolean{
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            100
        )
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }
    override fun onResume() {
        super.onResume()
//        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dummyButton = null
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}