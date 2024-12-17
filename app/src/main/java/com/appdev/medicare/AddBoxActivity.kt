package com.appdev.medicare

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appdev.medicare.model.DateItem
import com.appdev.medicare.model.MedicationData
import java.text.SimpleDateFormat
import java.util.*
import com.appdev.medicare.api.RetrofitClient
import com.appdev.medicare.model.AddMedicationRequest
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.utils.buildAlertDialog
import com.appdev.medicare.databinding.ActivityAddBoxBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddBoxActivity : AppCompatActivity() {

}