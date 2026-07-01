package com.example.simplewebview

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class YearlyPlanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make it full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        setContentView(R.layout.activity_yearly_plan)

        val btnBack = findViewById<TextView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }
}
