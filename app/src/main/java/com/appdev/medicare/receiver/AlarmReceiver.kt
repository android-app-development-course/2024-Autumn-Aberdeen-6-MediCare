package com.appdev.medicare.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context!= null && intent!= null) {
            val message = intent.getStringExtra("message")?: "Reminder"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            // 在这里可以添加更多的提醒逻辑，例如启动通知或触发声音提醒
            // 以下是一个简单的通知示例（假设已经有一个`NotificationUtils`类来处理通知创建和显示）
            // NotificationUtils.showNotification(context, message)
        }
    }
}
