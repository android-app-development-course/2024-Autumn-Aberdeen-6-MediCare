package com.appdev.medicare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.DateItem
import com.appdev.medicare.model.MedicationData
import com.appdev.medicare.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.MutableList

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var textMonthYear: TextView
    private lateinit var recyclerViewCalendar: RecyclerView
    private lateinit var switchToggleMode: SwitchCompat
    private lateinit var calendarAdapter: CalendarAdapter
    private var allTextViews: MutableList<TextView> = mutableListOf()

    private lateinit var buttonPreviousMonth: ImageButton
    private lateinit var buttonNextMonth: ImageButton
    private lateinit var buttonAddMedication: ImageButton

    private lateinit var dateItems: MutableList<DateItem> // 只是当前月份的，更换月份后重新初始化

    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var recyclerViewMedication: RecyclerView

    private lateinit var addMedicationActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        val root: View = binding.root

        textMonthYear = binding.textMonthYear
        recyclerViewCalendar = binding.recyclerViewCalendar
        recyclerViewMedication = binding.recyclerViewMedication
        switchToggleMode = binding.switchToggleMode
        buttonNextMonth = binding.buttonNextMonth
        buttonPreviousMonth = binding.buttonPreviousMonth
        buttonAddMedication = binding.buttonAddMedication

        // Set the current month and year in the header
        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        textMonthYear.text = dateFormat.format(currentDate.time)

        // Prepare the days of the month
        val daysOfMonth = getDaysOfMonth(currentDate)

        // Get the DateItems in this month
        dateItems = generateDateItemsForMonth(currentDate)

        recyclerViewMedication.layoutManager = LinearLayoutManager(requireContext())
        // Setup RecyclerView with a GridLayoutManager
        recyclerViewCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = CalendarAdapter(daysOfMonth, false, dateItems)
        recyclerViewCalendar.adapter = calendarAdapter

        switchToggleMode.setOnCheckedChangeListener { _, isChecked ->
            recyclerViewMedication.adapter = null
            calendarAdapter.setMultiSelectMode = isChecked
            allTextViews = calendarAdapter.getAllTextViews()
            allTextViews.forEach { view ->
                view.setBackgroundResource(R.drawable.default_shape)
            }
            if (isChecked) {
                // 显示启用多选模式的提示
                Toast.makeText(requireContext(), "多选模式已启用", Toast.LENGTH_SHORT).show()
            } else {
                // 显示禁用多选模式的提示
                Toast.makeText(requireContext(), "多选模式已禁用", Toast.LENGTH_SHORT).show()
            }
        }

        buttonPreviousMonth.setOnClickListener {
            val currentDate = Calendar.getInstance()
            currentDate.add(Calendar.MONTH, -1)
            updateUI(currentDate)
        }

        buttonNextMonth.setOnClickListener {
            val currentDate = Calendar.getInstance()
            currentDate.add(Calendar.MONTH, 1)
            updateUI(currentDate)
        }

        addMedicationActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data!= null) {
                    val medicationData = data.getParcelableExtra("MEDICATION_DATA") as MedicationData?
                    if (medicationData!= null) {
                        if (calendarAdapter.setMultiSelectMode) {
                            val selectedDateItems = calendarAdapter.getSelectedDateItems()
                            selectedDateItems.forEach { dateItem:DateItem ->
                                dateItem.medicationData?.add(medicationData)
//                                val medicationList = dateItem.medicationData
//                                medicationList?.let {
//                                    medicationList.forEach {medicationData ->
//                                        setRemindersForDailyIntake(requireContext(), dateItem.date, medicationData.dailyIntakeTimes, medicationData.reminderMode)
//                                    }
//                                }
                            }
                            calendarAdapter.clearStates()
                        } else {
                            val selectedDateItem = calendarAdapter.getSelectedDateItem()
                            if (selectedDateItem!= null) {
                                selectedDateItem.medicationData?.add(medicationData)
                                val medicationList = selectedDateItem.medicationData
                                medicationList?.let {
//                                    medicationList.forEach {medicationData ->
//                                        setRemindersForDailyIntake(requireContext(), selectedDateItem.date, medicationData.dailyIntakeTimes, medicationData.reminderMode)
//                                    }
                                    medicationAdapter = MedicationAdapter(medicationList) {}
                                    recyclerViewMedication.adapter = medicationAdapter
                                }
                            }
                        }
                    }
                }
            }
        }

        buttonAddMedication.setOnClickListener {
            // 保证在点击加号之前，用户已经选定了要做更改的日期
            val intent = Intent(requireContext(), AddMedActivity::class.java)
            addMedicationActivityLauncher.launch(intent)
        }

        // 设置日期选择监听器
        calendarAdapter.setOnDateSelectedListener(object : CalendarAdapter.OnDateSelectedListener {
            override fun onDateSelected(dateItem: DateItem, flag: Boolean) {
                if (flag) {
                    val medicationList = dateItem.medicationData
                    if (medicationList!= null) {
                        medicationAdapter = MedicationAdapter(medicationList) {}
                        recyclerViewMedication.adapter = medicationAdapter
                    }
                } else {
                    recyclerViewMedication.adapter = null
                }
            }
        })

        return root
    }

    private fun combineDateAndTime(date: Date, time: String): Date {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeDate = timeFormat.parse(time)

        // 使用 Calendar 将日期和时间合并
        val calendar = Calendar.getInstance()
        calendar.time = date

        // 获取时间部分的小时和分钟
        val timeCalendar = Calendar.getInstance()
        timeCalendar.time = timeDate!!

        // 设置日期的小时和分钟
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    private fun generateDateItemsForMonth(calendar: Calendar): MutableList<DateItem> {
        val dateItems = mutableListOf<DateItem>()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateIdentifier = generateDateIdentifier(calendar)
            dateItems.add(
                DateItem(
                    dateIdentifier = dateIdentifier,
                    date = calendar.time,
                    medicationData = mutableListOf()
                )
            )
        }
        return dateItems
    }

    private fun generateDateIdentifier(calendar: Calendar): Int {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1  // Calendar.MONTH 从 0 开始
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return year * 10000 + month * 100 + day
    }

    private fun getDaysOfMonth(calendar: Calendar): List<Int> {
        val daysOfMonth = mutableListOf<Int>()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 星期日返回7
        val adjustedDayOfWeek = when (val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 7
            else -> firstDayOfWeek - 1
        }
        // Add empty days for alignment
        for (i in 1 until adjustedDayOfWeek) {
            daysOfMonth.add(0) // Add empty slots
        }

        // Add the actual days of the month
        for (day in 1..daysInMonth) {
            daysOfMonth.add(day)
        }

        return daysOfMonth
    }

    private fun updateUI(currentDate: Calendar) {
        textMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDate.time)
        val daysOfMonth = getDaysOfMonth(currentDate)
        calendarAdapter = CalendarAdapter(daysOfMonth, switchToggleMode.isChecked, generateDateItemsForMonth(currentDate))
        recyclerViewCalendar.adapter = calendarAdapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up the binding
    }
}