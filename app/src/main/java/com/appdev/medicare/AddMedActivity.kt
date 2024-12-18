package com.appdev.medicare

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.AttributeSet
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
import com.appdev.medicare.model.AddMedicationRequest
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.room.AppDatabase
import com.appdev.medicare.room.DatabaseBuilder
import com.appdev.medicare.room.entity.CalendarMedication
import com.appdev.medicare.room.entity.Medication
import com.appdev.medicare.room.entity.MedicationTime
import com.appdev.medicare.utils.buildAlertDialog
import com.appdev.medicare.utils.parseRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMedActivity : AppCompatActivity() {
    private lateinit var editTextMedicationName: EditText
    private lateinit var editTextPatientName: EditText
    private lateinit var editTextMedicalRecord: EditText
    private lateinit var editTextDosage: EditText
    private lateinit var editTextRemainingAmount: EditText

    private var dailyIntakeFrequency: Int = 3
    private var selectedTimes: MutableMap<Int, String> = mutableMapOf()

    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private lateinit var checkBox3: CheckBox
    private lateinit var checkBox4: CheckBox
    private lateinit var checkBox5: CheckBox
    private lateinit var checkBox6: CheckBox
    private lateinit var checkBox7: CheckBox

    private lateinit var checkboxCalendar: CheckBox
    private lateinit var checkboxAlarm: CheckBox
    private lateinit var checkboxAppNotification: CheckBox
    private lateinit var checkboxNone: CheckBox

    private lateinit var editTextExpiryDate: EditText
    private lateinit var buttonSaveMedication: Button
    private lateinit var buttonBackMain: ImageButton

    private lateinit var medicationData : MedicationData
    private lateinit var formattedExpiryDate : String

    private lateinit var dateList: List<String>

    private lateinit var dataBase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)
        dataBase = DatabaseBuilder.getInstance(this@AddMedActivity)

        // Initialize UI elements
        editTextMedicationName = findViewById(R.id.editTextMedicationName)
        editTextPatientName = findViewById(R.id.editTextPatientName)
        editTextMedicalRecord = findViewById(R.id.editTextMedicalRecord)
        editTextDosage = findViewById(R.id.editTextDosage)
        editTextRemainingAmount = findViewById(R.id.editTextRemainingAmount)

        checkBox1 = findViewById(R.id.checkBoxMonday)
        checkBox2 = findViewById(R.id.checkBoxTuesday)
        checkBox3 = findViewById(R.id.checkBoxWednesday)
        checkBox4 = findViewById(R.id.checkBoxThursday)
        checkBox5 = findViewById(R.id.checkBoxFriday)
        checkBox6 = findViewById(R.id.checkBoxSaturday)
        checkBox7 = findViewById(R.id.checkBoxSunday)
        val checkBoxWeeks = listOf(checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6,checkBox7)


        // 提醒方式
        checkboxCalendar = findViewById(R.id.checkboxCalendar)
        checkboxAlarm = findViewById(R.id.checkboxAlarm)
        checkboxAppNotification = findViewById(R.id.checkboxAppNotification)
        checkboxNone = findViewById(R.id.checkboxNone)

        editTextExpiryDate = findViewById(R.id.editTextExpiryDate)
        buttonSaveMedication = findViewById(R.id.buttonSaveMedication)
        buttonBackMain = findViewById(R.id.buttonBackMain)

        showIntakeTimes(3, true)

        val flag = intent.getBooleanExtra("flag", true)
        // 锁定当天的星期，全部星期栏禁用
        val mode = intent.getBooleanExtra("mode", false)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (!mode and flag) {
            val selectedDate = intent.getParcelableExtra<DateItem>("selectedDate")
            dateList = listOf(dateFormat.format(selectedDate!!.date))
            val calendar = Calendar.getInstance().apply {
                time = selectedDate.date
            }
            val week = when (val w = calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> 6
                else -> w - 2
            }
            checkBoxWeeks[week].isChecked = true
            checkBoxWeeks.forEach { box ->
                if (box != checkBoxWeeks[week]) box.isChecked = false
            }
            checkBoxWeeks.forEach { box ->
                box.isEnabled = false
            }
        } else if (mode and flag) {
            val selectedDates: ArrayList<DateItem>? = intent.getParcelableArrayListExtra("selectedDates")
            dateList = selectedDates?.map { dateFormat.format(it.date) } ?: emptyList()
        }


        // Set up the checkboxes
        // 支持多选，选择无取消选中其他的
        checkboxNone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkboxCalendar.isChecked = false
                checkboxAlarm.isChecked = false
                checkboxAppNotification.isChecked = false
            }
        }

        checkboxCalendar.setOnClickListener {
            Toast.makeText(this, "日历提醒功能已被弃用！！", Toast.LENGTH_SHORT).show()
        }

        val reminderCheckboxes = listOf(checkboxAlarm, checkboxAppNotification)
        reminderCheckboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkboxNone.isChecked = false
                } else {
                    val allUnchecked = reminderCheckboxes.all {!it.isChecked }
                    if (allUnchecked) {
                        // 如果所有提醒方式都未选中，选中"无"
                        checkboxNone.isChecked = true
                    }
                }
            }
        }
        // Set up the expiry date picker
        editTextExpiryDate.setOnClickListener {
            showDatePicker()
        }

        // Set up back button
        buttonBackMain.setOnClickListener {
            finish()
        }

        // Save medication data on button click
        buttonSaveMedication.setOnClickListener {
            val medicationName = editTextMedicationName.text.toString()
            val patientName = editTextPatientName.text.toString()
            // 诊籍的唯一标识符。待定
            val medicalRecord = editTextMedicalRecord.text.toString()
            val dosage = editTextDosage.text.toString()
            val remainingAmount = editTextRemainingAmount.text.toString()

            var dailyIntakeTimes: MutableList<String> = mutableListOf()
            var reminderMode: String? = null
            var weekMode: String? = null
            val expiryDate = editTextExpiryDate.text.toString()

            // Validate data input
            if (medicationName.isBlank() || patientName.isBlank() || dosage.isBlank() ||
                remainingAmount.isBlank() || selectedTimes.size != dailyIntakeFrequency || expiryDate.isBlank()) {
                Toast.makeText(
                    this,
                    "请填写所有药品信息",
                    Toast.LENGTH_SHORT).show()
            } else {
                dailyIntakeTimes = sortTimes(selectedTimes.values.toMutableList())
                reminderMode = getRemindMode()
                weekMode = getWeekMode()

                var addSuccessful = true
                var medicationId = 0

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val localMedicationUpdate = Medication(
                                medicationName = medicationName,
                                patientName = patientName,
                                dosage = dosage,
                                remainingAmount = remainingAmount,
                                frequency = dailyIntakeFrequency.toString(),
                                weekMode = weekMode,
                                reminderType = reminderMode,
                                expirationDate = formattedExpiryDate,
                            )
                            medicationId =
                                dataBase.medicationDao().insertOne(localMedicationUpdate).toInt() // 更新本地Medication数据库
                            for (date in dateList) {
                                val dateCalendarUpdate = CalendarMedication(
                                    medicationId = medicationId,
                                    date = date
                                )
                                val dateId = dataBase.calendarMedicationDao().insertOne(dateCalendarUpdate).toInt()
                                for (time in dailyIntakeTimes) {
                                    val localTimeUpdate = MedicationTime(
                                        medicationId = medicationId,
                                        dateId =  dateId,
                                        time = time,
                                        status =  -1,
                                    )
                                    dataBase.medicationTimeDao().insertOne(localTimeUpdate)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("插入出错","请检查各数据插入")
                            addSuccessful = false
                        }
                    }

                }
                if (addSuccessful) {
                    medicationData = MedicationData(medicationId, medicationName, patientName, dosage, remainingAmount, dailyIntakeFrequency.toString(), dailyIntakeTimes, weekMode, reminderMode, formattedExpiryDate)
                    val intent = Intent()
                    intent.putExtra("MEDICATION_DATA", medicationData)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    runOnUiThread {
                        buildAlertDialog(
                            this@AddMedActivity,
                            "药品信息保存失败",
                            "原因1"
                        )
                            .show()
                    }
                }
            }
        }
    }
    fun showIntakeTimes(selectedNumber: Int, defaultFlag: Boolean) {
        val timeSelectionLayout = findViewById<LinearLayout>(R.id.timeSelectionLayout)
        dailyIntakeFrequency = selectedNumber
        timeSelectionLayout.removeAllViews()
        selectedTimes.clear()

        if(defaultFlag) {
            selectedTimes[0] = "08:30"
            selectedTimes[1] = "12:30"
            selectedTimes[2] = "19:30"
        }

        for (i in 1..selectedNumber) {
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            val rowLayoutParams = LinearLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 4 / 5), // 设置宽度为屏幕的 4/5
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = rowLayoutParams
            for (j in 1..3) {
                val index = (i - 1) * 3 + j
                if (index <= selectedNumber) {
                    val timeButton = android.widget.Button(this)
                    timeButton.apply {
                        val defaultTimeText = when (index) {
                            1 -> "08:30"
                            2 -> "12:30"
                            3 -> "19:30"
                            else -> ""
                        }
                        if (defaultFlag)
                            text = defaultTimeText
                        else
                            text =  ("选择时间 ${index}")
                        setTextColor(Color.parseColor("#000000")) // 按钮文字颜色
                        setBackgroundResource(R.drawable.rounded_blue_background) // 使用自定义背景
                        textSize = 16f
                        gravity = Gravity.CENTER
                    }

                    val fixedWidth =
                        ((resources.displayMetrics.widthPixels * 4 / 5 - 80) / 3) - 30.toInt()
                    val buttonParams = LinearLayout.LayoutParams(
                        fixedWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
//                        buttonParams.setMargins(20, 0, 20, 2)
                    buttonParams.setMargins(20, 0, 20, 2) // 更均匀的间距
                    timeButton.layoutParams = buttonParams

                    val rowLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, // 父布局宽度匹配屏幕
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    rowLayoutParams.setMargins(0, 16, 0, 16) // 每一行的上下间距
                    rowLayout.layoutParams = rowLayoutParams

                    timeButton.setOnClickListener {
                        showTimePicker(timeButton, index)
                    }
                    rowLayout.addView(timeButton)
                }
            }
            timeSelectionLayout.addView(rowLayout)
        }
    }

    fun showDailyIntakeList(view: View) {
        val items = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("选择每日服用次数")
        builder.setItems(items) { dialog, which ->
            val selectedNumber = items[which].toInt()
            showIntakeTimes(selectedNumber, false)
        }
        builder.show()
    }

    private fun showTimePicker(button: Button, index: Int) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            this,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val timeText = "%02d:%02d".format(selectedHour, selectedMinute)
                button.text = timeText
                selectedTimes[index] = timeText
            },
            hour,
            minute,
            true // 是否使用 24 小时制
        ).show()
    }

    private fun showDatePicker() {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Open date picker dialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Set selected date to EditText
                val formattedDate = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth + 1, selectedDay)
                formattedExpiryDate = formattedDate
                editTextExpiryDate.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    fun sortTimes(selectedTimes: MutableList<String>): MutableList<String> {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return selectedTimes.sortedWith(compareBy {
            timeFormat.parse(it) // 将字符串解析为 Date 以便正确排序
        }).toMutableList()
    }

    fun getRemindMode(): String {
        var modeCode = ""
        var checkboxes: MutableList<CheckBox> = mutableListOf(checkboxAlarm, checkboxAppNotification)
        checkboxes.forEach { checkbox ->
            if (checkbox.isChecked) {
                modeCode += "1"
            } else {
                modeCode += "0"
            }
        }
        return modeCode
    }

    // 对星期的选择编码，便于数据库储存
    fun getWeekMode(): String {
        var modeCode = ""
        var checkboxes: List<CheckBox> = listOf(checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6, checkBox7)
        checkboxes.forEach { checkbox ->
            if (checkbox.isChecked) {
                modeCode += "1"
            } else {
                modeCode += "0"
            }
        }
        return modeCode
    }
}