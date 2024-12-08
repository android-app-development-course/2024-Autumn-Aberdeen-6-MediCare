package com.appdev.medicare

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appdev.medicare.api.RetrofitClient
import com.appdev.medicare.model.RegisterRequest
import com.appdev.medicare.utils.buildAlertDialog
import com.appdev.medicare.utils.hashString
import com.appdev.medicare.utils.parseRequestBody
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var loginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        buttonRegister = findViewById(R.id.buttonRegister)
        loginText = findViewById(R.id.loginText)

        buttonRegister.setOnClickListener() {
            register()
        }

        loginText.setOnClickListener {
            finish()
        }
    }

    private fun register() {
        val username = usernameInput.text
        val password = passwordInput.text
        val confirmPassword = confirmPasswordInput.text

        if (username!!.isEmpty() || password!!.isEmpty() || confirmPassword!!.isEmpty()) {
            Toast.makeText(
                this,
                this.getString(R.string.fillInAllFields),
                Toast.LENGTH_SHORT
            ).show()
        } else if (password.toString() != confirmPassword.toString()) {
            Toast.makeText(
                this,
                this.getString(R.string.passwordsNotMatch),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            lifecycleScope.launch {
                val registerRequest =
                    RegisterRequest(username.toString(), hashString(password.toString()))
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.register(registerRequest).execute()
                }

                if (response.isSuccessful) {
                    Log.d("RegisterActivity", "Register success")
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterActivity,
                            this@RegisterActivity.getString(R.string.registerSuccess),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    val failReason = parseRequestBody(response.errorBody()).error!!
                    if (failReason.code == "USERNAME_ALREADY_EXIST") {
                        Log.w(
                            "RegisterActivity",
                            "Register Failed, reason: username $username already exist."
                        )
                        runOnUiThread {
                            buildAlertDialog(
                                this@RegisterActivity,
                                this@RegisterActivity.getString(R.string.registerFailed),
                                this@RegisterActivity.getString(R.string.usernameAlreadyExist)
                            )
                                .show()
                        }
                    } else {
                        Log.w(
                            "RegisterActivity",
                            "Register failed, reason: ${failReason.code}, ${failReason.description}"
                        )
                        runOnUiThread {
                            buildAlertDialog(
                                this@RegisterActivity,
                                this@RegisterActivity.getString(R.string.registerFailed),
                                "${failReason.code} - ${failReason.description}"
                            )
                        }
                    }
                }
            }
        }
    }
}