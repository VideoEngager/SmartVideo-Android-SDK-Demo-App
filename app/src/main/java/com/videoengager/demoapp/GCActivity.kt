//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.enums.CallType
import com.videoengager.sdk.enums.Engine
import com.videoengager.sdk.generic.model.schedule.Answer
import com.videoengager.sdk.generic.model.schedule.Availability
import com.videoengager.sdk.generic.model.schedule.AvailabilityCalendarSettings
import com.videoengager.sdk.generic.model.schedule.Result
import com.videoengager.sdk.model.AgentInfo
import com.videoengager.sdk.model.Error
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import org.acra.ACRA
import java.util.*
import java.util.concurrent.Executors

class GCActivity : AppCompatActivity() {
    lateinit var sett:Settings
    private lateinit var preferences : SharedPreferences
    lateinit var additionalSettings : SharedPreferences
    lateinit var scheduleSettings : SharedPreferences
    lateinit var waitView : AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_c)
        window.applyCustomColors(getSharedPreferences("additional", MODE_PRIVATE))
        preferences = getSharedPreferences("genesys_cloud", MODE_PRIVATE)
        additionalSettings = getSharedPreferences("additional", MODE_PRIVATE)
        scheduleSettings = getSharedPreferences("schedule", MODE_PRIVATE)

        waitView = AlertDialog.Builder(this)
            .setTitle("Loading schedule meeting ...")
            .setView(ProgressBar(this,null,android.R.attr.progressBarStyleLarge).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            .create()

       if(!preferences.contains("VideoengagerUrl")) {//load defaults from params.json
           Globals.params?.genesys_cloud_params_init?.let {
               findViewById<EditText>(R.id.orgid).setText(it.OrganizationId)
               findViewById<EditText>(R.id.depid).setText(it.DeploymentId)
               findViewById<EditText>(R.id.videourl).setText(it.VideoengagerUrl)
               findViewById<EditText>(R.id.tenid).setText(it.TennathId)
               findViewById<EditText>(R.id.env).setText(it.Environment)
               findViewById<EditText>(R.id.queue).setText(it.Queue)
               findViewById<EditText>(R.id.name).setText(it.MyNickname)
           }
       }else
       {
           //load modified
           findViewById<EditText>(R.id.orgid).setText(preferences.getString("OrganizationId",""))
           findViewById<EditText>(R.id.depid).setText(preferences.getString("DeploymentId",""))
           findViewById<EditText>(R.id.videourl).setText(preferences.getString("VideoengagerUrl",""))
           findViewById<EditText>(R.id.tenid).setText(preferences.getString("TennathId",""))
           findViewById<EditText>(R.id.env).setText(preferences.getString("Environment",""))
           findViewById<EditText>(R.id.queue).setText(preferences.getString("Queue",""))
           findViewById<EditText>(R.id.name).setText(preferences.getString("MyNickname",""))

       }

        findViewById<Button>(R.id.buttonaudio).setOnClickListener {
            // audio mode only
            readSettings()
            if(SmartVideo.IsInCall){
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            }else {
                SmartVideo.Initialize(this, sett, Engine.genesys)
                if (SmartVideo.Connect(CallType.audio) == true) {
                    SmartVideo.onEventListener = listener
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.buttonvideo).setOnClickListener {
            readSettings()
            // add custom fields
            val customFields = mutableMapOf<String,Any>()
            val urlClient = "https://my_url_client"
            val videocallFlag = true
            val audioonlycallFlag = false
            val chatonlyFlag  = false
            customFields["urlclient"]=urlClient
            customFields["videocall"]=videocallFlag
            customFields["audioonlycall"]=audioonlycallFlag
            customFields["chatonly"]=chatonlyFlag
            sett.CustomFields=customFields
            if(SmartVideo.IsInCall){
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            }else {
                SmartVideo.Initialize(this, sett, Engine.genesys)
                if (SmartVideo.Connect(CallType.video) == true) {
                    SmartVideo.onEventListener = listener
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.buttonchat).setOnClickListener {
            readSettings()
            if(SmartVideo.IsInCall){
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            }else {
                SmartVideo.Initialize(this, sett, Engine.genesys)

                if (SmartVideo.Connect(CallType.chat)) {
                    SmartVideo.onEventListener = object : VideoEngager.EventListener() {
                        override fun onChatAccepted() {
                            val intent = Intent(this@GCActivity, WebChat::class.java)
                            startActivity(intent)
                            finish()
                        }

                        override fun onAgentOnline(agentInfo: AgentInfo?) {
                            agentInfo?.let {
                                Globals.agentName = it.firstName ?: "Agent"
                            }
                        }

                        override fun onCallFinished() {
                            // finish()
                        }
                    }
                } else Toast.makeText(
                    this@GCActivity,
                    "Error from connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.buttonAdditionalSettings).setOnClickListener {
            startActivity(Intent(this@GCActivity,AdditionalSettingsActivity::class.java))
        }

        findViewById<Button>(R.id.buttonschedule).setOnClickListener {
            readSettings()
            sett.MyPhone="+12345678"
            AlertDialog.Builder(this@GCActivity)
                .setTitle(R.string.choose_time)
                .setNegativeButton(R.string.set_time){ _, _ ->
                    Calendar.getInstance().let { c->
                        DatePickerDialog(this@GCActivity, { _, y, m, d ->
                            TimePickerDialog(this@GCActivity, { _, hh, mm ->
                                val pickedDateTime = Calendar.getInstance()
                                pickedDateTime.set(y, m, d, hh, mm)
                                SmartVideo.SDK_DEBUG=true
                                if(SmartVideo.IsInCall){
                                    Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
                                }else {
                                    SmartVideo.Initialize(this, sett, Engine.genesys)
                                    SmartVideo.onEventListener = listener
                                    SmartVideo.VeVisitorCreateScheduleMeeting(pickedDateTime.time, true, scheduleCallbackAnswer)
                                    SmartVideo.Dispose()
                                    waitView.setTitle("Loading schedule meeting ...")
                                    try{ waitView.show() }catch (_:Exception){}
                                }
                            },c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), true).show()
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                    }
                }
                .setNeutralButton(R.string.soon_as_possible){ dlg, _ ->
                    dlg.dismiss()
                    SmartVideo.SDK_DEBUG=true
                    if(SmartVideo.IsInCall){
                        Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
                    }else {
                        SmartVideo.Initialize(this, sett, Engine.genesys)
                        SmartVideo.onEventListener = listener
                        SmartVideo.VeVisitorCreateScheduleMeeting(null, false, scheduleCallbackAnswer)
                        SmartVideo.Dispose()
                        waitView.setTitle("Loading schedule meeting ...")
                        try{ waitView.show() }catch (_:Exception){}
                    }
                }
                .create()
                .apply {
                    if(scheduleSettings.contains("callid")){
                        setButton(AlertDialog.BUTTON_POSITIVE,"Open last requested") { dlg, _ ->
                            dlg?.dismiss()
                            openSavedScheduleMeeting()
                        }
                    }
                }
                .show()
        }

        findViewById<Button>(R.id.buttonavailability).setOnClickListener {
            readSettings()

            if (scheduleSettings.contains("callid")) {
                AlertDialog.Builder(this@GCActivity)
                    .setTitle("Message")
                    .setMessage("You have requested video meeting.\nCancel it if you want to schedule new one!")
                    .setPositiveButton("Open") { dlg, _ ->
                        dlg?.dismiss()
                        openSavedScheduleMeeting()
                    }.show()
                return@setOnClickListener
            }

            SmartVideo.SDK_DEBUG = true
            if (SmartVideo.IsInCall) {
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            } else{
                SmartVideo.Initialize(this, sett, Engine.genesys)
                SmartVideo.onEventListener = listener
                SmartVideo.VeChackAgentAvailability(object : Availability() {
                    override fun onCalendarSettingsResult(calendarSettings: AvailabilityCalendarSettings) {
                        SmartVideo.Dispose()
                        runOnUiThread { try { waitView.hide() } catch (_: Exception) { } }
                        if (calendarSettings.showAvailability) {
                            startActivity(Intent(this@GCActivity, AvailabilityActivity::class.java).apply { putExtra("settings", sett); putExtra("calendarSettings", calendarSettings) })
                        } else {
                            Toast.makeText(this@GCActivity, "Agent do not support Availability", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
            waitView.setTitle("Checking availability ...")
            try{ waitView.show() }catch (_:Exception){}
        }

        findViewById<Button>(R.id.button_pause_screen_share).setOnClickListener {
            SmartVideo.VeForcePauseScreenShare(this@GCActivity)
        }

        findViewById<Button>(R.id.button_resume_screen_share).setOnClickListener {
            SmartVideo.VeForceResumeScreenShare(this@GCActivity)
        }

        findViewById<Button>(R.id.button_pin).setOnClickListener {
            readSettings()
            val pin = findViewById<EditText>(R.id.pin).text.toString()
            if(!pin.isNullOrEmpty()){
                try {
                    SmartVideo.Initialize(this, sett, Engine.generic)
                    if (SmartVideo.Connect(CallType.video)) {
                        SmartVideo.onEventListener = listener
                        SmartVideo.VeVisitorVideoCall(pin)
                    } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
                }catch (ex:Exception){
                    Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this@GCActivity,"Enter PIN",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val scheduleCallbackAnswer = object : Answer() {
        override fun onSuccessResult(result: Result) {
            runOnUiThread {
                waitView.hide()
                scheduleSettings.edit().putString("callid",result.callId).apply()
                startActivityForResult(Intent(this@GCActivity,ScheduleResultActivity::class.java).putExtra("schedule_info",result),SCHEDULE_MEETING)
            }
        }
    }

    private fun openSavedScheduleMeeting(){
        SmartVideo.SDK_DEBUG=true
        if (SmartVideo.IsInCall) {
            Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
        } else {
            SmartVideo.Initialize(this@GCActivity, sett, Engine.genesys)
            SmartVideo.onEventListener = listener
            waitView.setTitle("Loading schedule meeting ...")
            try {
                waitView.show()
            } catch (_: Exception) {
            }
            scheduleSettings.getString("callid", null)?.let {
                SmartVideo.VeVisitorGetScheduleMeeting(it, scheduleCallbackAnswer)
                SmartVideo.Dispose()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SCHEDULE_MEETING && resultCode==ScheduleResultActivity.DELETE_ACTION){
            data?.getStringExtra("callId")?.let {
                SmartVideo.SDK_DEBUG = true
                if (SmartVideo.IsInCall) {
                    Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
                } else {
                    SmartVideo.Initialize(this@GCActivity, sett, Engine.genesys)
                    waitView.setTitle("Deleting schedule meeting ...")
                    waitView.show()
                    SmartVideo.VeVisitorDeleteScheduleMeeting(it, object : Answer() {
                        override fun onSuccessResult(result: Result) {
                            runOnUiThread {
                                scheduleSettings.edit().remove("callid").apply()
                                waitView.hide()
                                Toast.makeText(this@GCActivity, "Deleted", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            SmartVideo.Dispose()
                        }
                    })
                }
            }
        }
    }

    private fun readSettings(){
        Globals.params?.genesys_cloud_params_init?.let {
            sett = Settings(
                findViewById<EditText>(R.id.orgid).text.toString(),
                findViewById<EditText>(R.id.depid).text.toString(),
                findViewById<EditText>(R.id.videourl).text.toString(),
                findViewById<EditText>(R.id.tenid).text.toString(),
                findViewById<EditText>(R.id.env).text.toString(),
                findViewById<EditText>(R.id.queue).text.toString(),
                it.AgentShortURL,
                findViewById<EditText>(R.id.name).text.toString(),
                findViewById<EditText>(R.id.name).text.toString(), ".",
                "myMail@aa.aa", "",
                Language = MainActivity.Lang?: VideoEngager.Language.ENGLISH
            )
            //save settings for later usage
            preferences.edit {
                putString("OrganizationId",findViewById<EditText>(R.id.orgid).text.toString())
                putString("DeploymentId",findViewById<EditText>(R.id.depid).text.toString())
                putString("VideoengagerUrl",findViewById<EditText>(R.id.videourl).text.toString())
                putString("TennathId",findViewById<EditText>(R.id.tenid).text.toString())
                putString("Environment",findViewById<EditText>(R.id.env).text.toString())
                putString("Queue",findViewById<EditText>(R.id.queue).text.toString())
                putString("MyNickname",findViewById<EditText>(R.id.name).text.toString())
                apply()
            }
            //load additional settings
            sett.AvatarImageUrl = additionalSettings.getString("avatarImageUrl",null)
            sett.informationLabelText = additionalSettings.getString("informationLabelText",null)
            sett.backgroundImageURL = additionalSettings.getString("backgroundImageURL",null)
            sett.toolBarHideTimeout = additionalSettings.getString("toolBarHideTimeout","10")!!.toInt()
            sett.customerLabel = additionalSettings.getString("customerLabel",null)
            sett.agentWaitingTimeout = additionalSettings.getString("agentWaitingTimeout","120")!!.toInt()
            sett.allowVisitorToSwitchAudioCallToVideoCall = additionalSettings.getBoolean("allowVisitorToSwitchAudioCallToVideoCall",false)
            sett.startCallWithPictureInPictureMode = additionalSettings.getBoolean("startCallWithPictureInPictureMode",false)
            sett.startCallWithSpeakerPhone = additionalSettings.getBoolean("startCallWithSpeakerPhone",false)
            sett.outgoingCallVC = Settings.OutgoingCallVC(additionalSettings.getBoolean("hideAvatar",false), additionalSettings.getBoolean("hideName",false))
        }
    }

    private val listener = object : VideoEngager.EventListener(){
        override fun onCallFinished() {
           // finish()
        }

        override fun onError(error: Error): Boolean {
            runOnUiThread { waitView.hide() }
            if(error.severity==Error.Severity.FATAL){
                ACRA.log.e("GC_Activity",error.toString())
                Toast.makeText(this@GCActivity, "Error:${error.message}", Toast.LENGTH_SHORT).show()
            }
            if(error.source.contains("schedule",true)){
                scheduleSettings.edit().remove("callid").apply()
                Toast.makeText(this@GCActivity, "Schedule meeting expired!", Toast.LENGTH_SHORT).show()
            }
            return super.onError(error)
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@GCActivity, message, Toast.LENGTH_SHORT).show()
        }

        override fun onAgentTimeout(): Boolean {
            additionalSettings.let {
                return it.getBoolean("showAgentBusyDialog",true)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang!!.value
        )
        )
        super.attachBaseContext(localeUpdatedContext)
    }

    companion object{
        const val SCHEDULE_MEETING = 9999
    }
}