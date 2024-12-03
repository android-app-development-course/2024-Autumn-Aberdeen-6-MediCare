package com.appdev.medicare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

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
                "Please fill in all fields.",
                Toast.LENGTH_SHORT
            ).show()
        } else if (password.toString() === confirmPassword.toString()) {
            Toast.makeText(
                this,
                "Not the same password",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Trying to register with username '${username}'!",
                Toast.LENGTH_SHORT
            ).show()
            // to be implemented
        }
    }
}