package com.example.autosmsapp

import android.Manifest
import android.R.attr
import android.R.id.message
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_SEND_SMS = 100
    val RESULT_PICK_CONTACT = 101
    var phoneNo: String? = null
    var name: String? = null
    var hourOfDay = 0
    var minute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestForPermission()
        //scheduleAlarm()
    }

    fun onSelectContact(v: View) {
        val contactPickerIntent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        )
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT)
    }

    fun onSelectTime(v: View) {
        TimePickerFragment().show(supportFragmentManager, "timePicker")
    }

    fun onTimeSet(hourOfDay: Int, minute: Int) {
        this.hourOfDay = hourOfDay
        this.minute = minute
        txt_selected_time.text = "$hourOfDay : $minute"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_PICK_CONTACT -> {
                    var cursor: Cursor? = null
                    try {
                        val uri: Uri? = data!!.data
                        cursor = contentResolver.query(uri!!, null, null, null, null)
                        cursor!!.moveToFirst()
                        val phoneIndex: Int =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val nameIndex: Int =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        phoneNo = cursor.getString(phoneIndex)
                        name = cursor.getString(nameIndex)
                        txt_selected_contact.text = "$name\n$phoneNo"
                        Log.e("Name and Contact", "$name,$phoneNo")
                    } catch (e: Exception) {
                        cursor!!.close()
                        e.printStackTrace()
                    }
                    cursor!!.close()
                }
            }
        } else {
            Log.e("Failed", "Not able to pick contact")
        }
    }

    fun requestForPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.SEND_SMS
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS
                );
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        applicationContext,
                        "permission granted", Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "SMS faild, please try again.", Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }
        }
    }


    // Setup a recurring alarm every half hour
    fun scheduleAlarm(v: View) {

        if (txt_selected_contact.text === getString(R.string.no_contact)) {
            Toast.makeText(this, "Please select contact", Toast.LENGTH_SHORT).show()
            return
        }

        if (txt_selected_time.text === getString(R.string.no_time)) {
            Toast.makeText(this, "Please select time", Toast.LENGTH_SHORT).show()
            return
        }

        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(applicationContext, MyAlarmReceiver::class.java)
        intent.putExtra("phone", phoneNo)
        intent.putExtra("msg", et_msg.text.toString())
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(
            this, MyAlarmReceiver.REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calender = Calendar.getInstance()
        calender.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calender.set(Calendar.MINUTE, minute)

        // Setup periodic alarm every every half hour from this point onwards
        val firstMillis =
            calender.timeInMillis // alarm is set right away
        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
// Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY

        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            firstMillis,
            AlarmManager.INTERVAL_DAY,
            pIntent
        )
    }

    fun cancel(v:View){
        val intent = Intent(applicationContext, MyAlarmReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT);
        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarm.cancel(pIntent);
    }
}
