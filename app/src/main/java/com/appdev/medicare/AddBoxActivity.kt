package com.appdev.medicare

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.media.MediaCodecInfo
import android.os.Bundle
import android.util.Log
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
import com.appdev.medicare.databinding.ActivityAddBoxBinding
import com.appdev.medicare.model.BoxData
import com.appdev.medicare.room.DatabaseBuilder
import com.appdev.medicare.room.dao.MedicineBoxDao
import com.appdev.medicare.room.entity.MedicineBox
import com.google.android.material.datepicker.DateValidatorPointBackward
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

    private lateinit var boxData: BoxData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddBoxBinding.inflate(layoutInflater)
        val root: View = binding.root
        val dataBase = DatabaseBuilder.getInstance(this@AddBoxActivity)
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
            val boxName = textBoxName.text.toString()
            val boxType = textBoxType.text.toString()
            val applicablePeople = textApplicablePeople.text.toString()
            val remark = textRemark.text.toString()

            var insertSuccessful = true
            var boxId = 0

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        boxId = dataBase.medicineBoxDao().insertOne(MedicineBox(
                            boxName = boxName,
                            boxType = boxType,
                            applicablePeople = applicablePeople,
                            medicationId = null,
                            remark = remark,
                        )).toInt()
                      } catch(e: Exception) {
                        Log.w("Insert Box", "Insert Box Database Failed")
                        insertSuccessful = false
                      }
                }
                if (insertSuccessful) {
                    boxData = BoxData(boxId, boxName, boxType, applicablePeople,null, remark,"")
                    val intent = Intent()
                    intent.putExtra("BOX_DATA", boxData)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }
}