package com.appdev.medicare

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.appdev.medicare.databinding.ActivityAddRecordBinding

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