package com.softrobs.whether_data.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.softrobs.whether_data.R
import com.softrobs.whether_data.data.remote.network.api_state_management.ApiState
import com.softrobs.whether_data.databinding.ActivityMainBinding
import com.softrobs.whether_data.databinding.FragmentWetherBinding
import com.softrobs.whether_data.ui.activities.MainActivityViewModel
import com.softrobs.whether_data.utils.API_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class WetherFragment : Fragment() {

    private var lat:Double? = null
    private var lon:Double? = null

    private val viewModel by viewModels<MainActivityViewModel> ()

    private lateinit var binding: FragmentWetherBinding
    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    companion object {
        fun newInstance() = WetherFragment()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWetherBinding.bind(view)

//        binding.selectDateImg.setOnClickListener(View.OnClickListener {
//
//        })
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//
//        if (lat == null || lon == null){
//            getLocation()
//        }else{
//            wetherForcastRequest()
//        }
//
//        observeWetherResponse()

    }
    private fun observeWetherResponse(){
        lifecycleScope.launchWhenStarted {
            with(viewModel){
                observeWeatherData.collect{ res ->
                    when(res){
                        is ApiState.SUCCESS ->{
                            Toast.makeText(requireContext(),res.getResponse.message.toString(),
                            Toast.LENGTH_LONG).show()
                            Log.i("WetherResponse : ","${res.getResponse.list.toString()}")
                        }
                        is ApiState.LOADING -> {
                            Toast.makeText(requireContext(),"Loading...",
                                Toast.LENGTH_LONG).show()
                        }
                        is ApiState.ERROR -> {
                            Toast.makeText(requireContext(),"Something went wrong...",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }


//    private fun wetherForcastRequest() {
//        viewModel.invokeWetherForecastCall(lat!!,lon!!, API_KEY)
//    }
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
                        binding.apply {

                            locationCity.text = "${list[0].locality}"
                            locationStateCountry.text = "${list[0].subLocality} ${list[0].countryName}"
//                            tvLocality.text = "Locality\n"
//                            tvAddress.text = "Address\n${list[0].getAddressLine(0)}"
                        }
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
}