package com.appdev.medicare

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.appdev.medicare.model.MedicationData
import java.text.SimpleDateFormat
import java.util.*

class AddMedActivity : AppCompatActivity() {
    private lateinit var editTextMedicationName: EditText
    private lateinit var editTextPatientName: EditText
    private lateinit var editTextMedicalRecord: EditText
    private lateinit var editTextDosage: EditText
    private lateinit var editTextRemainingAmount: EditText
    private lateinit var editTextDailyIntakeFrequency: EditText
    private lateinit var editTextIntakeIntervalDays: EditText

    private var selectedTimes: MutableMap<Int, String> = mutableMapOf()

    private lateinit var checkboxCalendar: CheckBox
    private lateinit var checkboxAlarm: CheckBox
    private lateinit var checkboxAppNotification: CheckBox
    private lateinit var checkboxNone: CheckBox

    private lateinit var editTextExpiryDate: EditText
    private lateinit var buttonSaveMedication: Button
    private lateinit var buttonBackMain: ImageButton

    private lateinit var medicationData : MedicationData
    private lateinit var formattedExpiryDate : Date


    private fun getSelectedTimes(): MutableMap<Int, String> {
        return selectedTimes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        // Initialize UI elements
        editTextMedicationName = findViewById(R.id.editTextMedicationName)
        editTextPatientName = findViewById(R.id.editTextPatientName)
        editTextMedicalRecord = findViewById(R.id.editTextMedicalRecord)
        editTextDosage = findViewById(R.id.editTextDosage)
        editTextRemainingAmount = findViewById(R.id.editTextRemainingAmount)
        editTextDailyIntakeFrequency = findViewById(R.id.editTextDailyIntakeFrequency)
        editTextIntakeIntervalDays = findViewById(R.id.editTextIntakeIntervalDays)

        // 提醒方式
        checkboxCalendar = findViewById(R.id.checkboxCalendar)
        checkboxAlarm = findViewById(R.id.checkboxAlarm)
        checkboxAppNotification = findViewById(R.id.checkboxAppNotification)
        checkboxNone = findViewById(R.id.checkboxNone)

        editTextExpiryDate = findViewById(R.id.editTextExpiryDate)
        buttonSaveMedication = findViewById(R.id.buttonSaveMedication)
        buttonBackMain = findViewById(R.id.buttonBackMain)

        // Set up the checkboxes
        // 支持多选，选择无取消选中其他的
        checkboxNone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkboxCalendar.isChecked = false
                checkboxAlarm.isChecked = false
                checkboxAppNotification.isChecked = false
            }
        }
        val reminderCheckboxes = listOf(checkboxCalendar, checkboxAlarm, checkboxAppNotification)
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
            saveMedicationData()
        }
    }

    fun showDailyIntakeList(view: View) {
        val items = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("选择每日服用次数")
        builder.setItems(items) { dialog, which ->
            val selectedNumber = items[which].toInt()
            val timeSelectionLayout = findViewById<LinearLayout>(R.id.timeSelectionLayout)

            timeSelectionLayout.removeAllViews()
            selectedTimes.clear()

            for (i in 1..selectedNumber) {
                val rowLayout = LinearLayout(this)
                rowLayout.orientation = LinearLayout.HORIZONTAL
                val rowLayoutParams = LinearLayout.LayoutParams(
                    (resources.displayMetrics.widthPixels * 4 / 5), // 设置宽度为屏幕的 4/5
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                rowLayout.layoutParams = rowLayoutParams
                for (j in 0 until 3) {
                    val index = (i - 1) * 3 + j
                    if (index < selectedNumber) {
                        val timeButton = android.widget.Button(this)
                        timeButton.text = "选择时间 $index"

                        val fixedWidth =
                            ((resources.displayMetrics.widthPixels * 4 / 5 - 80) / 3).toInt()
                        val buttonParams = LinearLayout.LayoutParams(
                            fixedWidth,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        buttonParams.setMargins(20, 0, 20, 2)
                        timeButton.layoutParams = buttonParams

                        timeButton.setOnClickListener {
                            showTimePicker(timeButton, index)
                        }
                        rowLayout.addView(timeButton)
                    }
                }
                timeSelectionLayout.addView(rowLayout)
            }
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
                formattedExpiryDate = calendar.time
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
        var checkboxes: MutableList<CheckBox> = mutableListOf(checkboxCalendar, checkboxAlarm, checkboxAppNotification)
        checkboxes.forEach { checkbox ->
            if (checkbox.isChecked) {
                modeCode += "0"
            } else {
                modeCode += "1"
            }
        }
        return modeCode
    }

    private fun saveMedicationData() {
        val medicationName = editTextMedicationName.text.toString()
        val patientName = editTextPatientName.text.toString()
        // 诊籍的唯一标识符。待定
        val medicalRecord = editTextMedicalRecord.text.toString()
        val dosage = editTextDosage.text.toString()
        val remainingAmount = editTextRemainingAmount.text.toString()
        val dailyIntakeFrequency = editTextDailyIntakeFrequency.text.toString()
        var dailyIntakeTimes: MutableList<String> = mutableListOf()
        val intakeIntervalDays = editTextIntakeIntervalDays.text.toString()
        var reminderMode: String? = null
        val expiryDate = editTextExpiryDate.text.toString()

        // Validate data input
        if (medicationName.isBlank() || patientName.isBlank() || dosage.isBlank() ||
            remainingAmount.isBlank() || dailyIntakeFrequency.isBlank() || selectedTimes.isEmpty() ||
            intakeIntervalDays.isBlank() || expiryDate.isBlank()) {
            Toast.makeText(this, "请填写所有药品信息", Toast.LENGTH_SHORT).show()
            return
        }

        dailyIntakeTimes = sortTimes(selectedTimes.values.toMutableList())
        reminderMode = getRemindMode()

        medicationData = MedicationData(medicationName, patientName, dosage, remainingAmount.toInt(), dailyIntakeFrequency.toInt(), dailyIntakeTimes, intakeIntervalDays.toInt(), reminderMode, formattedExpiryDate)

        val intent = Intent()
        intent.putExtra("MEDICATION_DATA", medicationData)
        setResult(RESULT_OK, intent)

        // Data saved successfully message
        Toast.makeText(this, "药品信息已保存", Toast.LENGTH_SHORT).show()

        // Return to the previous screen or handle saved data as needed
        finish()
    }
}
