package ir.yarhamrah.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class SmsNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName.lowercase()
        if (!(pkg.contains("messaging") || pkg.contains("messages") || pkg.contains("sms"))) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.bigText")?.toString()
            ?: extras.getCharSequence("android.text")?.toString().orEmpty()
        if (text.isBlank()) return
        getSharedPreferences("yar", MODE_PRIVATE).edit()
            .putString("last_sms", "فرستنده: $title\nمتن اعلان: $text\n\nاین فقط پیش‌نمایش اعلان جدید است، نه تاریخچه کامل پیامک‌ها.")
            .apply()
    }
}
