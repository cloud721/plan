package com.example.simplewebview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private lateinit var etAccount: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvErrorMsg: TextView
    private lateinit var tvAppName: TextView
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        etAccount = findViewById(R.id.et_account)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvErrorMsg = findViewById(R.id.tv_error_msg)
        tvAppName = findViewById(R.id.tv_app_name)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInput()
            }
        }
        etAccount.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        validateInput()

        tvAppName.setOnLongClickListener {
            startActivity(Intent(this, AttributionDebugActivity::class.java))
            true
        }

        btnLogin.setOnClickListener {
            val account = etAccount.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (account.isEmpty() || password.isEmpty()) {
                tvErrorMsg.text = "Please fill in both fields"
                tvErrorMsg.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvErrorMsg.visibility = View.GONE
            performLoginRequest(account, password)
        }
    }

    private fun validateInput() {
        val account = etAccount.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (account.isEmpty() && password.isEmpty()) {
            btnLogin.isEnabled = false
            tvErrorMsg.visibility = View.GONE
        } else if (account.length < 6 || password.length < 6) {
            btnLogin.isEnabled = false
            tvErrorMsg.text = "Account or password is too short"
            tvErrorMsg.visibility = View.VISIBLE
        } else {
            btnLogin.isEnabled = true
            tvErrorMsg.visibility = View.GONE
        }
    }

    private fun performLoginRequest(account: String, pass: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Requesting..."

        Thread {
            var displayMessage = ""
            var success = false
            try {
                val url = URL("http://192.168.21.35:3000/login")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val jsonPayload = "{\"username\": \"$account\", \"password\": \"$pass\"}"
                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(jsonPayload)
                writer.flush()
                writer.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val reader = conn.inputStream.bufferedReader()
                    val responseString = reader.readText()
                    reader.close()

                    val jsonObj = JSONObject(responseString)
                    val code = jsonObj.optInt("code", -1)
                    val msg = jsonObj.optString("message", "")

                    if (code == 0) {
                        success = true
                        val dataObj = jsonObj.optJSONObject("data")
                        val token = dataObj?.optString("token", "")
                        displayMessage = "Login success! Token: $token"
                    } else {
                        displayMessage = "Login failed: $msg"
                    }
                } else {
                    val errorReader = conn.errorStream?.bufferedReader()
                    displayMessage = errorReader?.readText() ?: "Unexpected HTTP status: $responseCode"
                    errorReader?.close()
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                displayMessage = e.message ?: "Unknown network error"
            }

            mainHandler.post {
                btnLogin.isEnabled = true
                btnLogin.text = "Continue"
                if (success) {
                    Toast.makeText(this@LoginActivity, displayMessage, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    tvErrorMsg.text = displayMessage
                    tvErrorMsg.visibility = View.VISIBLE
                }
            }
        }.start()
    }
}
