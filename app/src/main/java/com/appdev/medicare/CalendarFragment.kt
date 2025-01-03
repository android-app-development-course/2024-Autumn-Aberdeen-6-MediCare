package com.appdev.medicare

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
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.DateItem
import com.appdev.medicare.model.MedicationData
import com.appdev.medicare.databinding.FragmentCalendarBinding
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.receiver.NotificationReceiver
import com.appdev.medicare.room.AppDatabase
import com.appdev.medicare.room.DatabaseBuilder
import com.appdev.medicare.utils.buildAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private lateinit var buttonPreviousMonth: ImageButton
    private lateinit var buttonNextMonth: ImageButton
    private lateinit var buttonAddMedication: ImageButton

    private lateinit var dateItems: MutableList<DateItem> // 只是当前月份的，更换月份后重新初始化

    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var recyclerViewMedication: RecyclerView

    private lateinit var addMedicationActivityLauncher: ActivityResultLauncher<Intent>

    private val cachedDateItems = mutableMapOf<String, MutableList<DateItem>>() // 缓存 dateItems

    private lateinit var dataBase: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        val root: View = binding.root
        dataBase = DatabaseBuilder.getInstance(requireContext()) // 实例化本地数据库

        textMonthYear = binding.textMonthYear
        recyclerViewCalendar = binding.recyclerViewCalendar
        recyclerViewMedication = binding.recyclerViewMedication
        switchToggleMode = binding.switchToggleMode
        buttonNextMonth = binding.buttonNextMonth
        buttonPreviousMonth = binding.buttonPreviousMonth
        buttonAddMedication = binding.buttonAddMedication

        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val daysOfMonth = getDaysOfMonth(currentDate)

        textMonthYear.text = dateFormat.format(currentDate.time)
        dateItems = generateDateItemsForMonth(currentDate)

        lifecycleScope.launch {
            val previousMonth = Calendar.getInstance().apply {
                time = currentDate.time
                add(Calendar.MONTH, -1)
            }
            val nextMonth = Calendar.getInstance().apply {
                time = currentDate.time
                add(Calendar.MONTH, 1)
            }
            init_medicationInfo(dateItems)

            val previousMonthItems = generateDateItemsForMonth(previousMonth)
            val nextMonthItems = generateDateItemsForMonth(nextMonth)
            val deferredPrevious = async {
                init_medicationInfo(previousMonthItems)
            }
            val deferredNext = async {
                init_medicationInfo(nextMonthItems)
            }
            deferredPrevious.await()
            deferredNext.await()
        }


        recyclerViewMedication.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = CalendarAdapter(daysOfMonth, false, dateItems)
        recyclerViewCalendar.adapter = calendarAdapter

        switchToggleMode.setOnCheckedChangeListener { _, isChecked ->
            recyclerViewMedication.adapter = null
            calendarAdapter.setMultiSelectMode = isChecked
            calendarAdapter.clearStates()
            if (isChecked) {
                // 显示启用多选模式的提示
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.multipleSelectEnabled),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // 显示禁用多选模式的提示
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.multipleSelectDisabled),
                    Toast.LENGTH_SHORT
                ).show()
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
                            lifecycleScope.launch {
                                val checkFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                                if (calendarAdapter.setMultiSelectMode) {
                                    val selectedDateItems = calendarAdapter.getSelectedDateItems()
                                    withContext(Dispatchers.IO) {
                                        selectedDateItems.forEach { dateItem ->
                                            dateItem.medicationData?.add(medicationData)
                                            val medicationList = dateItem.medicationData
                                            medicationList?.lastOrNull()?.let { lastMedicationData ->
                                                setRemindersForDailyIntake(
                                                    this@CalendarFragment.requireActivity(),
                                                    dateItem.date,
                                                    lastMedicationData.medicationName,
                                                    lastMedicationData.patientName,
                                                    lastMedicationData.dailyIntakeTimes,
                                                    lastMedicationData.weekMode,
                                                    lastMedicationData.reminderMode
                                                )
                                            }
                                            val yearMonth = checkFormat.format(dateItem.date)
                                            cachedDateItems[yearMonth]?.find { it.date == dateItem.date }?.medicationData =
                                                dateItem.medicationData
                                        }
                                        calendarAdapter.clearStates()
                                    }
                                } else {
                                    val selectedDateItem = calendarAdapter.getSelectedDateItem()
                                    if (selectedDateItem != null) {
                                        selectedDateItem.medicationData?.add(medicationData)
                                        val medicationList = selectedDateItem.medicationData
                                        medicationList?.lastOrNull()?.let { lastMedicationData ->
                                            setRemindersForDailyIntake(
                                                this@CalendarFragment.requireActivity(),
                                                selectedDateItem.date,
                                                lastMedicationData.medicationName,
                                                lastMedicationData.patientName,
                                                lastMedicationData.dailyIntakeTimes,
                                                lastMedicationData.weekMode,
                                                lastMedicationData.reminderMode
                                            )
                                        }
                                        val yearMonth = checkFormat.format(selectedDateItem.date)
                                        cachedDateItems[yearMonth]?.find { it.date == selectedDateItem.date }?.medicationData =
                                            selectedDateItem.medicationData
                                        medicationAdapter = MedicationAdapter(selectedDateItem) { newList, item, deleteMedic ->
                                            selectedDateItem.medicationData = newList
                                            deleteOne(item, deleteMedic)
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
            val selectedDateItems = calendarAdapter.getSelectedDateItems()
            Log.d("CalendarFragment", "Selected Date Item: $selectedDateItem")
            if (selectedDateItem == null && selectedDateItems.isEmpty()) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    buildAlertDialog(
                        requireContext(),
                        requireContext().getString(R.string.selectDate),
                        requireContext().getString(R.string.selectDateDes)
                    )
                        .show()
                }
            } else {
                val intent = Intent(requireContext(), AddMedActivity::class.java)
                intent.putExtra("mode", calendarAdapter.setMultiSelectMode)
                if (!calendarAdapter.setMultiSelectMode) {
                    intent.putExtra("selectedDate", calendarAdapter.getSelectedDateItem())
                } else {
                    intent.putExtra("selectedDates", ArrayList(calendarAdapter.getSelectedDateItems()))
                }
                addMedicationActivityLauncher.launch(intent)
            }
        }
        // 设置日期选择监听器
        calendarAdapter.setOnDateSelectedListener(object : CalendarAdapter.OnDateSelectedListener {
            override fun onDateSelected(dateItem: DateItem, flag: Boolean) {
                if (flag) {
                    val medicationList = dateItem.medicationData
                    if (medicationList != null) {
                        medicationAdapter = MedicationAdapter(dateItem) {newList, item, deleteMedic ->
                            dateItem.medicationData = newList
                            deleteOne(item, deleteMedic)
                        }
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

        val calendar = Calendar.getInstance()
        calendar.time = date

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
        medicationName: String,
        patientName: String,
        intakeTimes: MutableList<String>,
        weekMode: String,
        reminderMode: String
    ) {
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
            }
        }

        lifecycleScope.launch{
            for (index in intakeTimes.indices) {
                // 获取当前的提醒时间
                val entireTime = combineDateAndTime(date, intakeTimes[index])
                (if (isAlarmEnabled) {
                    async(Dispatchers.IO) {
                        setAlarmReminder(
                            context,
                            entireTime,
                            index,
                            selectedWeekDay,
                            medicationName,
                            patientName
                        )
                    }
                } else {
                    null
                })?.await()

                (if (isNotificationEnabled) {
                    async(Dispatchers.IO) {
                        setNotificationReminder(
                            context,
                            entireTime,
                            index,
                            medicationName,
                            patientName
                        )
                    }
                } else {
                    null
                })?.await()

                if (index < intakeTimes.size - 1) {
                    delay(5_000L)
                }
            }
        }
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

    private fun setNotificationReminder(context: Context, remindTime: Date, requestCode: Int, medicationName: String, patientName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.appdev.medicare.NOTIFICATION_ACTION"
            putExtra("title", requireContext().getString(R.string.notificationTitle, patientName, medicationName))
            putExtra("message", requireContext().getString(R.string.notificationDetail))
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

    private fun setAlarmReminder(context: Context, remindTime: Date, requestCode: Int, weekDay: MutableList<Int>, medicationName: String, patientName: String) {
        val calendar = Calendar.getInstance().apply {
            time = remindTime
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, requireContext().getString(R.string.reminderIndex, requestCode + 1) + " $medicationName $patientName") // 闹钟标签
            if (weekDay.size > 1)
                putExtra(AlarmClock.EXTRA_DAYS, weekDay as ArrayList<Int>?)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true) // 如果设为 true，不显示系统闹钟 UI
        }

        // Important: Android 11及以上出现 Intent.resolveActivity(context.getPackageManager()) == null的处理
        try {
            // 尝试启动闹钟 Intent
            context.startActivity(alarmIntent)
            Log.d("CalendarFragment", "Alarm: Start Alarm setting")
            // 设置完系统闹钟，用户可以选择此次之后暂停但无法实现直接删除，单次闹钟考虑使用别的方法
        } catch (e: ActivityNotFoundException) {
            // 如果没有任何应用可以处理这个 Intent
            Toast.makeText(context, requireContext().getString(R.string.alarmNotAvailable), Toast.LENGTH_SHORT).show()
        }
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
        val checkFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val yearMonth = checkFormat.format(currentDate.time)
        if (cachedDateItems.containsKey(yearMonth)) {
            dateItems = cachedDateItems[yearMonth]!!
        } else {
            dateItems = generateDateItemsForMonth(currentDate)
        }
        val daysOfMonth = getDaysOfMonth(currentDate)

        calendarAdapter.updateData(dateItems, daysOfMonth)
//        calendarAdapter = CalendarAdapter(daysOfMonth, switchToggleMode.isChecked, dateItems)
        recyclerViewCalendar.adapter = calendarAdapter

        recyclerViewMedication.adapter = null // 清空记录栏

        lifecycleScope.launch {
            val previousMonth = Calendar.getInstance().apply {
                time = currentDate.time
                add(Calendar.MONTH, -1)
            }
            val nextMonth = Calendar.getInstance().apply {
                time = currentDate.time
                add(Calendar.MONTH, 1)
            }
            init_medicationInfo(dateItems)
            init_medicationInfo(generateDateItemsForMonth(previousMonth))
            init_medicationInfo(generateDateItemsForMonth(nextMonth))
        }
    }

    private fun convertMedicationData(data: JsonValue.JsonObject): MedicationData {
        val map = data.value
        return MedicationData(
            (map["medication_id"] as? JsonValue.JsonNumber)?.value!!.toInt(),
            (map["medication_name"] as? JsonValue.JsonString)?.value!!,
            (map["patient_name"] as? JsonValue.JsonString)?.value!!,
            (map["dosage"] as? JsonValue.JsonString)?.value!!,
            (map["remaining_amount"] as? JsonValue.JsonString)?.value!!,
            (map["frequency"] as? JsonValue.JsonString)?.value!!,
            (map["times"] as? JsonValue.JsonList)?.value?.mapNotNull {
                (it as? JsonValue.JsonString)?.value
            }?.toMutableList() ?: mutableListOf(),
            (map["week_mode"] as? JsonValue.JsonString)?.value!!,
            (map["reminder_mode"] as? JsonValue.JsonString)?.value!!,
            (map["expiry_date"] as? JsonValue.JsonString)?.value!!
        )
    }

    private fun update_medicationInfo(dateItems: MutableList<DateItem>) {
        // 云同步这月份的内容
        val checkFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val yearMonth = checkFormat.format(dateItems[0].date)
        lifecycleScope.launch {
            val deferredList = dateItems.map { dateItem ->
                async {
                    dealEach(dateItem)
                }
            }
            deferredList.awaitAll()
            cachedDateItems[yearMonth] = dateItems
        }
    }

    private fun init_medicationInfo(dateItems: MutableList<DateItem>) {
        val checkFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val yearMonth = checkFormat.format(dateItems[0].date)

        if (!cachedDateItems.containsKey(yearMonth)) {
            lifecycleScope.launch {
                val deferredList = dateItems.map { dateItem ->
                    async {
                        dealEach(dateItem)
                    }
                }
                deferredList.awaitAll()
                cachedDateItems[yearMonth] = dateItems
            }
        }
    }

    private fun dealEach(dateItem: DateItem) {
        lifecycleScope.launch {
            val listInfo = mutableListOf<MedicationData>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.format(dateItem.date)

            withContext(Dispatchers.IO) {
                val medicationIdList = dataBase.calendarMedicationDao().findMedicationIdByDate(date)
                if (medicationIdList.isNotEmpty()) {
                    for (id in medicationIdList) {
                        val dateId = dataBase.calendarMedicationDao().findId(id, date)
                        val medicationInfo = dataBase.medicationDao().findById(id)
                        val medicationTimes = dataBase.medicationTimeDao().findByMedicationAndDateId(id, dateId) // 这里获取到了 status, time
                        val statusList = medicationTimes.map { it.status } // 未使用
                        val timeList = medicationTimes.map { it.time }

                        val result = MedicationData(
                            medicationInfo.id,
                            medicationInfo.medicationName,
                            medicationInfo.patientName,
                            medicationInfo.dosage,
                            medicationInfo.remainingAmount,
                            medicationInfo.frequency,
                            timeList.toMutableList(),
                            medicationInfo.weekMode,
                            medicationInfo.reminderType,
                            medicationInfo.expirationDate,
                        )
                        listInfo.add(result)
                    }
                }
            }
            dateItem.medicationData = listInfo
        }
    }

    private fun deleteOne(dateItem: DateItem, deleteMedic: MedicationData) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val checkFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val date = dateFormat.format(dateItem.date)
                    val yearMonth = checkFormat.format(dateItem.date)
                    val medicationId = deleteMedic.medicationID
                    dataBase.calendarMedicationDao().softDeleteByMedicationIdAndDate(medicationId, date)
                    cachedDateItems[yearMonth]?.find { it.date == dateItem.date }?.medicationData = dateItem.medicationData
                } catch(e: Exception) {
                    Log.w("CalendarFragment", "Fail to delete local database, reason: $e")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up the binding
    }
}