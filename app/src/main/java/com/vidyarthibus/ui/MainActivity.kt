package com.vidyarthibus.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.vidyarthibus.R
import com.vidyarthibus.databinding.ActivityMainBinding
import com.vidyarthibus.ui.alternatives.AlternativesActivity
import com.vidyarthibus.ui.detail.DetailActivity
import com.vidyarthibus.ui.home.HomeViewModel
import com.vidyarthibus.ui.home.RouteAdapter
import com.vidyarthibus.utils.LocationUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: HomeViewModel by viewModels()
    private lateinit var adapter: RouteAdapter
    private var userLocation: Location? = null

    private val locationPermRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchLocation()
        } else {
            binding.tvLocationName.text = "Location unavailable"
            binding.tvLocationSub.text  = "Enable GPS to report crowd"
            binding.tvLocationBadge.text = "No GPS"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RouteAdapter { item ->
            startActivity(Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_ROUTE_ID, item.route.id)
                userLocation?.let {
                    putExtra(DetailActivity.EXTRA_USER_LAT, it.latitude)
                    putExtra(DetailActivity.EXTRA_USER_LNG, it.longitude)
                }
            })
        }
        binding.rvRoutes.layoutManager = LinearLayoutManager(this)
        binding.rvRoutes.adapter = adapter

        lifecycleScope.launch {
            vm.items.collectLatest { adapter.submitList(it) }
        }

        binding.navAlt.setOnClickListener {
            startActivity(Intent(this, AlternativesActivity::class.java))
        }

        checkAndRequestLocation()
    }

    private fun checkAndRequestLocation() {
        val fine   = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(this, fine) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, coarse) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            locationPermRequest.launch(arrayOf(fine, coarse))
        }
    }

    private fun fetchLocation() {
        lifecycleScope.launch {
            try {
                val loc = LocationUtils.getCurrentLocation(this@MainActivity)
                userLocation = loc
                binding.tvLocationName.text = "Location detected"
                binding.tvLocationSub.text  = "GPS verified · ready to report"
                binding.tvLocationBadge.text = "✓ Verified"
                binding.tvLocationBadge.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_pill_green)
                binding.tvLocationBadge.setTextColor(
                    ContextCompat.getColor(this@MainActivity, R.color.crowd_green_text))
            } catch (e: Exception) {
                binding.tvLocationName.text = "Location error"
                binding.tvLocationSub.text  = e.message ?: "Try again"
                binding.tvLocationBadge.text = "Error"
            }
        }
    }
}
