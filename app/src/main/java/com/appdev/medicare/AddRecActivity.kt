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
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.utils.buildAlertDialog
import com.appdev.medicare.databinding.ActivityAddRecordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text

class AddRecActivity: AppCompatActivity() {
    private var _binding: ActivityAddRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var textBoxName: EditText
    private lateinit var textPatientName: EditText
    private lateinit var textRecordTime: EditText
    private lateinit var textAdvice: EditText

    private lateinit var buttonAddPicture: ImageButton
    private lateinit var buttonBackMain: ImageButton
    private lateinit var buttonSaveRecord: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddRecordBinding.inflate(layoutInflater)
        val root: View = binding.root
        setContentView(root)

        textBoxName = binding.editTextRecordName
        textPatientName = binding.editTextPatientName
        textRecordTime = binding.editTextRecordTime
        textAdvice= binding.editTextAdvice

        buttonAddPicture = binding.buttonAddPicture
        buttonBackMain = binding.buttonBackMain
        buttonSaveRecord = binding.buttonSaveRecord


        buttonBackMain.setOnClickListener {
            finish()
        }

        buttonAddPicture.setOnClickListener {
            // 定义接口
        }

        buttonSaveRecord.setOnClickListener {
            // 定义函数
            finish()
        }
    }
}