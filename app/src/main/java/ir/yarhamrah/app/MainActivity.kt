package ir.yarhamrah.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.view.Gravity
import android.view.View
import android.graphics.Typeface
import android.content.Context

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var smsText: TextView
    private val pickFile = 301
    private val requestContacts = 302

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 32, 28, 24); layoutDirection = View.LAYOUT_DIRECTION_RTL }
        val title = TextView(this).apply { text = "یار همراه"; textSize = 28f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0,0,0,20) }
        status = TextView(this).apply { text = "اطلاعات فقط با انتخاب خودت به اشتراک گذاشته می‌شود."; textSize = 15f; setPadding(0,8,0,16) }
        smsText = TextView(this).apply { text = latestSms(this@MainActivity); textSize = 14f; setPadding(12,12,12,12) }
        root.addView(title); root.addView(status)
        root.addView(action("انتخاب مخاطب") { chooseContact() })
        root.addView(action("باز کردن تنظیم دسترسی اعلان پیامک") { startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) })
        root.addView(action("تازه‌سازی پیش‌نمایش پیامک‌ها") { smsText.text = latestSms(this) })
        root.addView(smsText)
        root.addView(action("انتخاب فایل") { val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE) }; startActivityForResult(i, pickFile) })
        root.addView(action("اشتراک‌گذاری متن پیامک‌ها") { share(smsText.text.toString()) })
        root.addView(action("حریم خصوصی") { AlertDialog.Builder(this).setTitle("حریم خصوصی").setMessage("این نمونه چیزی را خودکار به اینترنت ارسال نمی‌کند. مخاطب و فایل فقط پس از انتخاب تو و با پنجرهٔ اشتراک‌گذاری اندروید ارسال می‌شوند.").setPositiveButton("باشه", null).show() })
        val scroll = ScrollView(this); scroll.addView(root); setContentView(scroll)
    }

    private fun action(label: String, run: () -> Unit) = Button(this).apply { text = label; isAllCaps = false; setOnClickListener { run() } }

    private fun chooseContact() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), requestContacts); return
        }
        val names = mutableListOf<String>(); val numbers = mutableListOf<String>()
        contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")?.use { c ->
            while (c.moveToNext() && names.size < 150) { names.add(c.getString(0) ?: "بدون نام"); numbers.add(c.getString(1) ?: "") }
        }
        if (names.isEmpty()) { status.text = "مخاطبی پیدا نشد."; return }
        AlertDialog.Builder(this).setTitle("یک مخاطب را انتخاب کن").setItems(names.toTypedArray()) { _, which ->
            val selected = names[which] + "\n" + numbers[which]
            AlertDialog.Builder(this).setTitle("اشتراک‌گذاری مخاطب").setMessage(selected).setNegativeButton("لغو", null).setPositiveButton("اشتراک‌گذاری") { _, _ -> share(selected) }.show()
        }.show()
    }

    private fun share(value: String) {
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, value) }
        startActivity(Intent.createChooser(intent, "اشتراک‌گذاری با..."))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickFile && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            val send = Intent(Intent.ACTION_SEND).apply { type = contentResolver.getType(uri) ?: "*/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            startActivity(Intent.createChooser(send, "اشتراک‌گذاری فایل با..."))
        }
    }

    private fun latestSms(context: Context): String = context.getSharedPreferences("yar", Context.MODE_PRIVATE).getString("last_sms", "هنوز اعلان پیامکی دریافت نشده است.\nبرای دیدن پیامک‌های جدید، دسترسی اعلان را فعال کن.") ?: ""
}