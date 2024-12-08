package com.appdev.medicare

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appdev.medicare.api.RetrofitClient
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.model.LoginRequest
import com.appdev.medicare.utils.buildAlertDialog
import com.appdev.medicare.utils.hashString
import com.appdev.medicare.utils.parseRequestBody
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var registerText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        buttonLogin = findViewById(R.id.buttonLogin)
        registerText = findViewById(R.id.registerText)

        buttonLogin.setOnClickListener {
            login()
        }

        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity().javaClass)
            startActivity(intent)
        }
    }

    private fun login() {
        val username = usernameInput.text
        val password = passwordInput.text

        if (username!!.isEmpty() || password!!.isEmpty()) {
            Toast.makeText(
                this,
                this.getString(R.string.fillInAllFields),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            lifecycleScope.launch {
                val loginRequest =
                    LoginRequest(username.toString(), hashString(password.toString()))
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.login(loginRequest).execute()
                }

                if (response.isSuccessful) {
                    val loginToken =
                        ((response.body()!!.data as JsonValue.JsonObject).value["token"] as JsonValue.JsonString).value
                    Log.d("LoginActivity", "Login success, token: $loginToken")
                    val prefs: SharedPreferences =
                        this@LoginActivity.getSharedPreferences("MediCare", MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString("login_token", loginToken) // 存储 token
                    editor.apply()
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            this@LoginActivity.getString(R.string.loginSuccess),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    val failReason = parseRequestBody(response.errorBody()).error!!
                    if (failReason.code == "USERNAME_ALREADY_EXIST") {
                        Log.w(
                            "LoginActivity",
                            "Login Failed, reason: invalid username or password"
                        )
                        runOnUiThread {
                            buildAlertDialog(
                                this@LoginActivity,
                                this@LoginActivity.getString(R.string.loginFailed),
                                this@LoginActivity.getString(R.string.invalidUsernameOrPassword)
                            )
                                .show()
                        }
                    } else {
                        Log.w(
                            "LoginActivity",
                            "Login failed, reason: ${failReason.code}, ${failReason.description}"
                        )
                        runOnUiThread {
                            buildAlertDialog(
                                this@LoginActivity,
                                this@LoginActivity.getString(R.string.loginFailed),
                                "${failReason.code} - ${failReason.description}"
                            )
                        }
                    }
                }
            }
        }
    }
}