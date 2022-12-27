package com.softrobs.whether_data.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Insets.add
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.softrobs.whether_data.R
import com.softrobs.whether_data.databinding.ActivityMainBinding
import com.softrobs.whether_data.ui.BaseActivity
import com.softrobs.whether_data.ui.fragments.WetherFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.jar.Manifest
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<FullscreenFragment>(R.id.fragment_container_view)
            }
        }

    }

    override fun getLayoutResourceId(): Int {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return R.layout.activity_main
    }

    override fun initUI() {



//        isLocationEnabled()

    }

}