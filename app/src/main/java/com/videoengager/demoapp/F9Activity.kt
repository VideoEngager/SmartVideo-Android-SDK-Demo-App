//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.enums.CallType
import com.videoengager.sdk.enums.Engine
import com.videoengager.sdk.model.Error
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import org.acra.ACRA
import java.util.*

class F9Activity : AppCompatActivity() {
    private lateinit var sett:Settings
    private lateinit var preferences : SharedPreferences
    lateinit var additionalSettings : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_f9)
        window.applyCustomColors(getSharedPreferences("additional", MODE_PRIVATE))
        preferences = getSharedPreferences("five9", MODE_PRIVATE)
        additionalSettings = getSharedPreferences("additional", MODE_PRIVATE)

        findViewById<Button>(R.id.button_video).setOnClickListener {
            readSettings()
            SmartVideo.SDK_DEBUG = true
            if (SmartVideo.IsInCall) {
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            } else {
                SmartVideo.Initialize(this, sett, Engine.five9)
                if (SmartVideo.Connect(CallType.video)) {
                    SmartVideo.onEventListener = listener
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }

        if(preferences.contains("orgid")) {
            //load settings previous settings
            findViewById<EditText>(R.id.orgid).setText(preferences.getString("orgid", ""))
            findViewById<EditText>(R.id.queue).setText(preferences.getString("queue", ""))
            findViewById<EditText>(R.id.env).setText(preferences.getString("env", ""))
            findViewById<EditText>(R.id.name).setText(preferences.getString("name", ""))
            findViewById<EditText>(R.id.tenid).setText(preferences.getString("tenid", ""))
            findViewById<EditText>(R.id.videourl).setText(preferences.getString("videourl", ""))
        }else{
            //load defaults
            Globals.params?.five9_params_init?.let {
                findViewById<EditText>(R.id.orgid).setText(it.OrganizationId)
                findViewById<EditText>(R.id.queue).setText(it.Queue)
                findViewById<EditText>(R.id.env).setText(it.Environment)
                findViewById<EditText>(R.id.name).setText(it.MyNickname)
                findViewById<EditText>(R.id.tenid).setText(it.TennathId)
                findViewById<EditText>(R.id.videourl).setText(it.VideoengagerUrl)
            }
        }

        additionalSettings.edit{
            putString("last_used_tenant",findViewById<EditText>(R.id.tenid).text.toString())
            apply()
        }
    }

    private fun readSettings(){
       // Globals.params?.genesys_cloud_params_init?.let {
            sett = Settings(
                OrganizationId = findViewById<EditText>(R.id.orgid).text.toString(),
                Queue = findViewById<EditText>(R.id.queue).text.toString(),
                Environment = findViewById<EditText>(R.id.env).text.toString(),
                VideoengagerUrl = findViewById<EditText>(R.id.videourl).text.toString(),
                TennathId = findViewById<EditText>(R.id.tenid).text.toString(),
                AgentShortURL = "agent",
                MyFirstName = "",
                MyLastName = "",
                MyNickname = findViewById<EditText>(R.id.name).text.toString(),
                MyEmail =  "",
                Language = MainActivity.Lang?: VideoEngager.Language.ENGLISH,
                DeploymentId = null,
                MyPhone = null,
                ServiceName = null,
                Subject = null
            )
        //save settings for later usage
        preferences.edit {
            putString("orgid",findViewById<EditText>(R.id.orgid).text.toString())
            putString("queue",findViewById<EditText>(R.id.queue).text.toString())
            putString("env",findViewById<EditText>(R.id.env).text.toString())
            putString("name",findViewById<EditText>(R.id.name).text.toString())
            putString("tenid",findViewById<EditText>(R.id.tenid).text.toString())
            putString("videourl",findViewById<EditText>(R.id.videourl).text.toString())
            apply()
        }

        additionalSettings.edit{
            putString("last_used_tenant",findViewById<EditText>(R.id.tenid).text.toString())
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

    private val listener = object : VideoEngager.EventListener(){
        override fun onCallFinished() {
            finish()
        }

        override fun onError(error: Error): Boolean {
            ACRA.log.e("F9_Activity",error.toString())
            Toast.makeText(this@F9Activity, "Error:${error.message}", Toast.LENGTH_SHORT).show()
            return super.onError(error)
        }

        override fun onMessageReceived(message: String) {
            Toast.makeText(this@F9Activity, message.trim(), Toast.LENGTH_LONG).show()
        }

        override fun onMessageAndTimeStampReceived(timestamp: String, message: String) {
            Toast.makeText(this@F9Activity, message.trim(), Toast.LENGTH_LONG).show()
        }

        override fun onAgentTimeout(): Boolean {
            additionalSettings.let {
                return it.getBoolean("showAgentBusyDialog",true)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang!!.value)
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}