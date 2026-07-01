package com.example.simplewebview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.simplewebview.attribution.HomeAttributionCoordinator
import com.example.simplewebview.attribution.LaunchType
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var attributionCoordinator: HomeAttributionCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        bottomNav = findViewById(R.id.bottom_nav)
        attributionCoordinator = AppAttribution.createCoordinator(this)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(EcommerceFragment())
                    true
                }

                R.id.navigation_search -> {
                    loadFragment(BookingFragment())
                    true
                }

                R.id.navigation_activity -> {
                    loadFragment(ActivityFragment())
                    true
                }

                else -> {
                    loadFragment(ActivityFragment())
                    true
                }
            }
        }

        applyTargetTab(intent.getStringExtra(AppAttribution.targetTabExtra()))
        attributionCoordinator.onCreate(LaunchType.COLD_START)
        attributionCoordinator.onFirstInstallEligible(LaunchType.COLD_START)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        attributionCoordinator.resetForNextLaunchCycle()
        applyTargetTab(intent?.getStringExtra(AppAttribution.targetTabExtra()))
        attributionCoordinator.onNewIntent(
            intent = intent,
            launchType = LaunchType.BACKGROUND_RETURN,
            sdkResolver = AppAttribution::resolveSdk,
        )
    }

    fun openAttributionTarget(screen: String, contentId: String?) {
        when (screen) {
            "ecommerce" -> applyTargetTab("ecommerce")
            "booking" -> applyTargetTab("booking")
            "activity" -> applyTargetTab("activity")
            "debug_attribution" -> {
                startActivity(Intent(this, AttributionDebugActivity::class.java))
            }
            "yearly_plan" -> {
                startActivity(
                    Intent(this, YearlyPlanActivity::class.java).apply {
                        putExtra("content_id", contentId)
                    }
                )
            }
        }
    }

    private fun applyTargetTab(target: String?) {
        when (target) {
            "ecommerce" -> selectTab(R.id.navigation_home, EcommerceFragment())
            "booking" -> selectTab(R.id.navigation_search, BookingFragment())
            "activity", null, "" -> selectTab(R.id.navigation_activity, ActivityFragment())
        }
    }

    private fun selectTab(itemId: Int, fragment: Fragment) {
        if (bottomNav.selectedItemId == itemId) {
            loadFragment(fragment)
        } else {
            bottomNav.selectedItemId = itemId
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
