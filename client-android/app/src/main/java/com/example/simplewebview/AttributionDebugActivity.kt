package com.example.simplewebview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simplewebview.attribution.AttributionDebugStore

class AttributionDebugActivity : AppCompatActivity() {
    private lateinit var tvLogs: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attribution_debug)
        supportActionBar?.hide()

        tvLogs = findViewById(R.id.tv_logs)

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.btn_refresh).setOnClickListener {
            renderLogs()
        }
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            AttributionDebugStore.clear()
            renderLogs()
        }
        findViewById<Button>(R.id.btn_copy).setOnClickListener {
            copyLogs()
        }
    }

    override fun onResume() {
        super.onResume()
        renderLogs()
    }

    private fun renderLogs() {
        tvLogs.text = AttributionDebugStore.render()
    }

    private fun copyLogs() {
        val logs = AttributionDebugStore.render()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("attribution-logs", logs))
        Toast.makeText(this, "Logs copied", Toast.LENGTH_SHORT).show()
    }
}
