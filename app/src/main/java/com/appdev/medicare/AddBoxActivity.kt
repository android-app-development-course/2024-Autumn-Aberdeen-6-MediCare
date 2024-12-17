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
import org.w3c.dom.Text

class AddBoxActivity : AppCompatActivity() {
    private var _binding: ActivityAddBoxBinding? = null
    private val binding get() = _binding!!

    private lateinit var textBoxName: EditText
    private lateinit var textBoxType: EditText
    private lateinit var textApplicablePeople: EditText
    private lateinit var textRemark: EditText

    private lateinit var buttonAddPicture: ImageButton
    private lateinit var buttonBackMain: ImageButton
    private lateinit var buttonSaveBox: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddBoxBinding.inflate(layoutInflater)
        val root: View = binding.root
        setContentView(root)

        textBoxName = binding.editTextBoxName
        textBoxType = binding.editTextBoxType
        textApplicablePeople = binding.editTextApplicablePeople
        textRemark = binding.editTextRemark

        buttonAddPicture = binding.buttonAddPicture
        buttonBackMain = binding.buttonBackMain
        buttonSaveBox = binding.buttonSaveBox


        buttonBackMain.setOnClickListener {
            finish()
        }

        buttonAddPicture.setOnClickListener {
            // 定义接口
        }

        buttonSaveBox.setOnClickListener {
            // 定义函数
            finish()
        }
    }
}