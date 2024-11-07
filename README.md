# MediCare 呵药  
- **Medi**cine + **Care** → **MediCare**  
- 智能药品管理 & 提醒 App  
- 2024 年秋季阿伯丁学院 第 6 组  
### 项目目录  
- [/doc](/doc/) - 项目文档  
    - [/01 商业计划书](/doc/01%20商业计划书.md)  
    - [/02 MediCare 设计方案汇报](/doc/02%20MediCare%20设计方案汇报.pptx)


### CalendarPage开发问题与解决

1. 在使用系统闹钟设置闹钟提醒时，不能连续设置闹钟。因为是加载Intent，会只有第一个时间被设置  
   使用`handler.post()`设置延迟后解决
   ```kotlin
   val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Medication Reminder"+"$requestCode") // 闹钟标签
            putExtra(AlarmClock.EXTRA_SKIP_UI, true) // 如果设为 true，不显示系统闹钟 UI
        }
   ```
   ```kotlin
   val reminderTask = object : Runnable {
    override fun run() {
        if (index >= intakeTimes.size) return // 如果超出范围，停止任务
        // 获取当前的提醒时间
        val entireTime = combineDateAndTime(date, intakeTimes[index])
        // 设置提醒
        if (isNotificationEnabled) {
            setNotificationReminder(context, entireTime, index)
        } else if (isAlarmEnabled) {
            setAlarmReminder(context, entireTime, index)
        }
        // 增加索引，等待 10 秒后处理下一个时间
        index++
        handler.postDelayed(this, 10_000L) // 延迟 10 秒再执行下一次任务
        }    
    }
    // 开始执行第一个任务
    handler.post(reminderTask)
   ```
2. 同样的在设置通知提醒时，连续设置时只有最后一个时间被设置。因为忘记设置requestCode区分各pendingIntent
   在函数中添加requestCode并将其传入pendingIntent解决
   ```kotlin
   val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
   ```
