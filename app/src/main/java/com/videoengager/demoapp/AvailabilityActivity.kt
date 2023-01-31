package com.videoengager.demoapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.generic.model.schedule.*
import com.videoengager.sdk.model.Error
import com.videoengager.sdk.model.Settings
import org.acra.ACRA
import java.text.SimpleDateFormat
import java.util.*

class AvailabilityActivity : AppCompatActivity() {

    var timeSlots = mutableListOf<AvailabilityTimeSlots>()
    private lateinit var settings:Settings
    private lateinit var calendarSettings:AvailabilityCalendarSettings
    lateinit var timeSlotsAdapter:TimeSlotsAdapter
    lateinit var waitView : ProgressBar
    lateinit var scheduleSettings : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_availability)
        scheduleSettings = getSharedPreferences("schedule", MODE_PRIVATE)
        settings = intent.getSerializableExtra("settings") as Settings
        calendarSettings = intent.getSerializableExtra("calendarSettings") as AvailabilityCalendarSettings
        waitView = findViewById(R.id.progressBar)
        waitView.isVisible = false

        val timeSlotsView = findViewById<RecyclerView>(R.id.timeslotsView)
        timeSlotsAdapter = TimeSlotsAdapter(timeSlots){
            settings.MyPhone="+12345678"
            AlertDialog.Builder(this@AvailabilityActivity)
                .setMessage("Do you want to schedule video meeting at $it ?")
                .setPositiveButton("OK"){ _, _ ->
                        waitView.isVisible = true
                        VideoEngager.SDK_DEBUG=true
                        val video = VideoEngager(this, settings, VideoEngager.Engine.genesys)
                        video.onEventListener = listener
                        video.VeVisitorCreateScheduleMeeting(it,true, object : Answer() {
                            override fun onSuccessResult(result: Result) {
                                video.Disconnect()
                                runOnUiThread {
                                    waitView.isVisible = false
                                    scheduleSettings.edit().putString("callid",result.callId).apply()
                                    startActivityForResult(Intent(this@AvailabilityActivity,ScheduleResultActivity::class.java).putExtra("schedule_info",result),1001)
                                }
                            }
                        })
                        video.Disconnect()
                    }
                .setNegativeButton("Cancel", null)
                .show()
        }
        timeSlotsView.adapter = timeSlotsAdapter
        timeSlotsView.layoutManager = GridLayoutManager(this,3,GridLayoutManager.VERTICAL,false)


        val calendar = Calendar.getInstance()
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        loadSlots(calendar.time)
        calendarView.setMinimumDate(calendar.apply { add(Calendar.DATE, -1) })
        calendarView.setMaximumDate(Calendar.getInstance().apply { add(Calendar.DATE, calendarSettings.numberOfDays-1) })
        calendarView.setOnDayClickListener {
            if(it.isEnabled){
                calendarView.setDate(it.calendar)
                loadSlots(it.calendar.time)
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadSlots(forDate:Date) {
        timeSlots.clear()
        timeSlotsAdapter.notifyDataSetChanged()
        waitView.isVisible = true
        VideoEngager.SDK_DEBUG = true
        val video = VideoEngager(this@AvailabilityActivity, settings, VideoEngager.Engine.genesys)
        video.onEventListener = listener
        video.VeGetAgentAvailabilityTimeSlots(forDate, 1, object : Availability() {
            override fun onTimeSlotsResult(timeSlots: List<AvailabilityTimeSlots>) {
                val currentTime = Calendar.getInstance()
                val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                val currentMinute = currentTime.get(Calendar.MINUTE)
                val startTime = Calendar.getInstance().apply { time=forDate }
                    .apply {
                        if(calendarSettings.calendarHours.allDay.openTime.isNullOrEmpty()) {
                            set(Calendar.HOUR_OF_DAY, currentHour)
                            set(Calendar.MINUTE, currentMinute)
                            set(Calendar.SECOND, 0)
                        }else{
                            val t = calendarSettings.calendarHours.allDay.openTime.split(":")
                            set(Calendar.HOUR_OF_DAY, t[0].toInt())
                            set(Calendar.MINUTE, t[1].toInt())
                            set(Calendar.SECOND, 0)
                        }
                    }.time
                val endTime = Calendar.getInstance().apply { time=forDate }
                    .apply {
                           if(calendarSettings.calendarHours.allDay.closeTime.isNullOrEmpty()) {
                               set(Calendar.HOUR_OF_DAY, 23)
                               set(Calendar.MINUTE, 59)
                               set(Calendar.SECOND, 59)
                           }else{
                               val t = calendarSettings.calendarHours.allDay.closeTime.split(":")
                               set(Calendar.HOUR_OF_DAY, t[0].toInt())
                               set(Calendar.MINUTE, t[1].toInt())
                               set(Calendar.SECOND, 0)
                           }
                        }.time

                this@AvailabilityActivity.timeSlots.addAll(timeSlots.filter { it.interval>0
                        && it.date.after(if(currentTime.time.after(startTime)) currentTime.time else startTime)
                        && it.date.before(endTime) })
                runOnUiThread {
                    timeSlotsAdapter.notifyDataSetChanged()
                    waitView.isVisible = false
                }
            }
        })
    }

    private val listener = object : VideoEngager.EventListener(){

        override fun onError(error: Error): Boolean {
            runOnUiThread { waitView.isVisible = false }
            if(error.severity==Error.Severity.FATAL){
                ACRA.log.e("AvailableActivity",error.toString())
                Toast.makeText(this@AvailabilityActivity, "Error:${error.message}", Toast.LENGTH_SHORT).show()
            }
            return super.onError(error)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1001){
            if(resultCode==ScheduleResultActivity.DELETE_ACTION) {
                data?.getStringExtra("callId")?.let {
                    VideoEngager.SDK_DEBUG = true
                    val video = VideoEngager(this@AvailabilityActivity, settings, VideoEngager.Engine.genesys)
                    waitView.isVisible = true
                    video.VeVisitorDeleteScheduleMeeting(it, object : Answer() {
                        override fun onSuccessResult(result: Result) {
                            runOnUiThread {
                                scheduleSettings.edit().remove("callid").apply()
                                waitView.isVisible = false
                                Toast.makeText(this@AvailabilityActivity, "Deleted schedule video meeting", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            video.Disconnect()
                        }
                    })
                }
            }else{
                finish()
            }
        }
    }

    class TimeSlotsAdapter(private val timeslots : MutableList<AvailabilityTimeSlots>, private val TimeSlotSelectCallback:(date:Date)->Unit) : RecyclerView.Adapter<TimeSlotsAdapter.ViewHolder>(){

        private val dateFormat = SimpleDateFormat("HH:mm",Locale.getDefault())

        class ViewHolder(view: View, private val SelectCallback:(date:Date)->Unit) : RecyclerView.ViewHolder(view),View.OnClickListener {
            val textView: TextView
            var date:Date?=null

            init {
                textView = view.findViewById(android.R.id.text1)
                view.setOnClickListener(this)
                view.setBackgroundResource(android.R.drawable.list_selector_background)
                textView.gravity= Gravity.CENTER
            }

            override fun onClick(view: View?) {
                SelectCallback.invoke(date!!)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)

            return ViewHolder(view,TimeSlotSelectCallback)
        }

        override fun getItemCount(): Int {
            return timeslots.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val timeSlot = timeslots[position]
            holder.date = timeSlot.date
            holder.textView.text = holder.date?.let { dateFormat.format(it) }
        }
    }
}