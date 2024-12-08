package com.appdev.medicare

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.DateItem
import com.appdev.medicare.model.MedicationData
import com.appdev.medicare.databinding.FragmentCalendarBinding
import com.appdev.medicare.receiver.NotificationReceiver
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
    ): View {
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
            currentDate.add(Calendar.MONTH, -1)
            updateUI(currentDate)
        }

        buttonNextMonth.setOnClickListener {
            currentDate.add(Calendar.MONTH, 1)
            updateUI(currentDate)
        }

        addMedicationActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val medicationData =
                            data.getParcelableExtra("MEDICATION_DATA") as MedicationData?
                        if (medicationData != null) {
                            if (calendarAdapter.setMultiSelectMode) {
                                val selectedDateItems = calendarAdapter.getSelectedDateItems()
                                selectedDateItems.forEach { dateItem ->
                                    dateItem.medicationData?.add(medicationData)
                                    val medicationList = dateItem.medicationData
                                    medicationList?.let {
                                        medicationList.forEach { medicationData ->
                                            setRemindersForDailyIntake(
                                                requireContext(),
                                                dateItem.date,
                                                medicationData.dailyIntakeTimes,
                                                medicationData.weekMode,
                                                medicationData.reminderMode
                                            )
                                        }
                                    }
                                }
                                calendarAdapter.clearStates()
                            } else {
                                val selectedDateItem = calendarAdapter.getSelectedDateItem()
                                if (selectedDateItem != null) {
                                    selectedDateItem.medicationData?.add(medicationData)
                                    val medicationList = selectedDateItem.medicationData
                                    medicationList?.let {
                                        medicationList.forEach { medicationData ->
                                            setRemindersForDailyIntake(
                                                requireContext(),
                                                selectedDateItem.date,
                                                medicationData.dailyIntakeTimes,
                                                medicationData.weekMode,
                                                medicationData.reminderMode
                                            )
                                        }
                                        medicationAdapter = MedicationAdapter(medicationList) { newList ->
                                            selectedDateItem.medicationData = newList
                                        }
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
            val selectedDateItem = calendarAdapter.getSelectedDateItem()
            Log.d("CalendarFragment", "Selected Date Item: $selectedDateItem")
            if (selectedDateItem == null) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    val alertDialog = AlertDialog.Builder(requireContext())
                        .setTitle("选择日期")
                        .setMessage("请先选择日期，以便添加药物。")
                        .setPositiveButton("确定") { dialog, _ ->
                            dialog.dismiss()  // Dismiss the dialog
                        }
                        .create()
                    alertDialog.show()
                }
            } else {
                val intent = Intent(requireContext(), AddMedActivity::class.java)
                intent.putExtra("mode", calendarAdapter.setMultiSelectMode)
                if (!calendarAdapter.setMultiSelectMode) {
                    intent.putExtra("selectedDate", calendarAdapter.getSelectedDateItem())
                }
                addMedicationActivityLauncher.launch(intent)
            }
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

    private fun setRemindersForDailyIntake(
        context: Context,
        date: Date,
        intakeTimes: MutableList<String>,
        weekMode: String,
        reminderMode: String
    ) {
        //  val isCalendarEnabled = reminderMode.getOrNull(0) == '1'
        val isAlarmEnabled = reminderMode.getOrNull(0) == '1'
        val isNotificationEnabled = reminderMode.getOrNull(1) == '1'

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.areNotificationsEnabled()) {
            startNotificationSetting()
        }

        val selectedWeekDay = mutableListOf<Int>()
        val weeks = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)

        for (i in 0 until 7) {
            val code = weekMode.getOrNull(i) == '1'
            if (code) {
                selectedWeekDay.add(weeks[i])
                //  val selected = weeks[i]
                //  Log.d("selectedDay$i", "$selected")
            }
        }
        val handler = Handler(Looper.getMainLooper())
        var index = 0

        val reminderTask = object : Runnable {
            override fun run() {
                if (index >= intakeTimes.size) return // 如果超出范围，停止任务
                // 获取当前的提醒时间
                val entireTime = combineDateAndTime(date, intakeTimes[index])
                // 设置提醒
                if (isNotificationEnabled) {
                    setNotificationReminder(context, entireTime, index)
                } else if (isAlarmEnabled) {
                    setAlarmReminder(context, entireTime, index, selectedWeekDay)
                }
                // 增加索引，等待 10 秒后处理下一个时间
                index++
                handler.postDelayed(this, 10_000L) // 延迟 10 秒再执行下一次任务
            }
        }
        // 开始执行第一个任务
        handler.post(reminderTask)
    }

    private fun startNotificationSetting() {
        val applicationInfo = requireContext().applicationInfo
        try {
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", applicationInfo.packageName)
                putExtra("android.provider.extra.APP_PACKAGE", applicationInfo.packageName)
                putExtra("app_uid", applicationInfo.uid)
            }
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                data = Uri.fromParts("package", applicationInfo.packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun setAlarmReminder(context: Context, remindTime: Date, requestCode: Int, weekDay: MutableList<Int>) {
        val calendar = Calendar.getInstance().apply {
            time = remindTime
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // 创建系统闹钟的 Intent
        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Medication Reminder"+"$requestCode") // 闹钟标签
            if (weekDay.size > 1)
                putExtra(AlarmClock.EXTRA_DAYS, weekDay as ArrayList<Int>?)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true) // 如果设为 true，不显示系统闹钟 UI
        }

        // Important: Android 11及以上出现 Intent.resolveActivity(context.getPackageManager()) == null的处理
        try {
            // 尝试启动闹钟 Intent
            context.startActivity(alarmIntent)
            // 设置完系统闹钟，用户可以选择此次之后暂停但无法实现直接删除，单次闹钟考虑使用别的方法
        } catch (e: ActivityNotFoundException) {
            // 如果没有任何应用可以处理这个 Intent
            Toast.makeText(context, "Alarm clock app not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun setNotificationReminder(context: Context, remindTime: Date, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.calendar.NOTIFICATION_ACTION"
            putExtra("title", "Medication Reminder")
            putExtra("message", "It's time to take your medication.")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!hasRequirePermission(context)) {
            val uri = Uri.parse("package:${context.packageName}")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime.time, pendingIntent)
    }

    private fun hasRequirePermission(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            false
        }
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