package com.videoengager.demoapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.CalendarContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.model.Error

class ScheduleResultActivity : AppCompatActivity() {
    private val CREATE_FILE = 5544
    lateinit var schInfo : com.videoengager.sdk.generic.model.schedule.Result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_result)
        val meetingUrlBox = findViewById<EditText>(R.id.sch_meeting_url)
        val meetingTimeBox = findViewById<EditText>(R.id.sch_meeting_time)
        meetingTimeBox.keyListener = null
        meetingUrlBox.keyListener = null
        schInfo =intent.getSerializableExtra("schedule_info") as com.videoengager.sdk.generic.model.schedule.Result
        meetingUrlBox.setText(schInfo.veVisitorUrl)
        meetingTimeBox.setText((schInfo.meetingDate.toString()))

        findViewById<Button>(R.id.button_sch_openmeeting).setOnClickListener {
            val sett=Globals.params?.generic_params_init!!
            sett.Language = MainActivity.Lang?:VideoEngager.Language.ENGLISH
            val video = VideoEngager(this,sett, VideoEngager.Engine.generic)
            if(video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = object : VideoEngager.EventListener(){
                    override fun onError(error: Error): Boolean {
                        Toast.makeText(this@ScheduleResultActivity, "Error:${error.message}", Toast.LENGTH_SHORT).show()
                        return super.onError(error)
                    }
                }
                video.VeVisitorVideoCall(schInfo.veVisitorUrl)
            }else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.button_sch_addtocalendar).setOnClickListener {
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, schInfo.meetingEpochTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, schInfo.meetingEpochTime+60*60*1000)//1 hour
                .putExtra(CalendarContract.Events.TITLE, "Video Meeting")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Start your video meeting by opening URL in your browser")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, schInfo.veVisitorUrl)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_sch_saveics).setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="*/*"
                putExtra(Intent.EXTRA_TITLE, "videomeeting.ics")
            }
            startActivityForResult(intent, CREATE_FILE)
        }

        findViewById<Button>(R.id.button_sch_copy).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("video meeting", meetingUrlBox.text.toString())
            clipboard.setPrimaryClip(clip)
        }

        findViewById<Button>(R.id.button_sch_delete).setOnClickListener {
            setResult(DELETE_ACTION,Intent().putExtra("callId",schInfo.callId))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CREATE_FILE && resultCode==RESULT_OK){
            data?.data?.also { uri ->
               contentResolver.openFileDescriptor(uri,"w").use {
                   ParcelFileDescriptor.AutoCloseOutputStream(it).write(schInfo.icsFileData.encodeToByteArray())
                   runOnUiThread { Toast.makeText(this@ScheduleResultActivity,"Saved videomeeting.ics}",Toast.LENGTH_LONG).show() }
               }
            }
        }
    }

    companion object {
        val DELETE_ACTION = 9898
    }
}