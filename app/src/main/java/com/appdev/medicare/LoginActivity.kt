package com.appdev.medicare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

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
                "Please fill in all fields.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Trying to login with username '${username}'!",
                Toast.LENGTH_SHORT
            ).show()
            // to be implemented
        }
    }
}