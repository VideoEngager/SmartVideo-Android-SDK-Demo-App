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

class GEActivity : AppCompatActivity() {
    private lateinit var sett:Settings
    private lateinit var preferences : SharedPreferences
    lateinit var additionalSettings : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_e)
        window.applyCustomColors(getSharedPreferences("additional", MODE_PRIVATE))
        preferences = getSharedPreferences("genesys_engage", MODE_PRIVATE)
        additionalSettings = getSharedPreferences("additional", MODE_PRIVATE)
        findViewById<Button>(R.id.buttonaudio).setOnClickListener {
            // audio mode only
            readSettings()
            if (SmartVideo.IsInCall) {
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            } else {
                SmartVideo.Initialize(this, sett, Engine.engage)
                if (SmartVideo.Connect(CallType.audio)) {
                    SmartVideo.onEventListener = listener
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.buttonvideo).setOnClickListener {
            readSettings()
            if (SmartVideo.IsInCall) {
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            } else {
                SmartVideo.Initialize(this, sett, Engine.engage)
                if (SmartVideo.Connect(CallType.video)) {
                    SmartVideo.onEventListener = listener
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }

        //load settings previous settings
        findViewById<EditText>(R.id.url).setText(preferences.getString("url",""))
        findViewById<EditText>(R.id.service).setText(preferences.getString("service",""))
        findViewById<EditText>(R.id.agenturl).setText(preferences.getString("agenturl",""))
        findViewById<EditText>(R.id.fname).setText(preferences.getString("fname",""))
        findViewById<EditText>(R.id.lname).setText(preferences.getString("lname",""))
        findViewById<EditText>(R.id.subject).setText(preferences.getString("subject",""))
        findViewById<EditText>(R.id.mail).setText(preferences.getString("mail",""))
        findViewById<EditText>(R.id.auth).setText(preferences.getString("auth",""))

        findViewById<Button>(R.id.buttonAdditionalSettings).setOnClickListener {
            startActivity(Intent(this@GEActivity,AdditionalSettingsActivity::class.java))
        }

    }

    private fun readSettings(){
       // Globals.params?.genesys_cloud_params_init?.let {
            sett = Settings(
                VideoengagerUrl =  "https://videome.leadsecure.com",
                Environment = findViewById<EditText>(R.id.url).text.toString(),
                AgentShortURL = findViewById<EditText>(R.id.agenturl).text.toString(),
                MyFirstName = findViewById<EditText>(R.id.fname).text.toString(),
                MyLastName = findViewById<EditText>(R.id.lname).text.toString(),
                MyNickname = findViewById<EditText>(R.id.subject).text.toString(),
                MyEmail =  findViewById<EditText>(R.id.mail).text.toString(),
                Language = MainActivity.Lang?: VideoEngager.Language.ENGLISH,
                AuthorizationHeader = findViewById<EditText>(R.id.auth).text.toString(),
                DeploymentId = null,OrganizationId = null,TennathId = null,Queue = null,MyPhone = null,
                ServiceName = findViewById<EditText>(R.id.service).text.toString(),
                Subject = findViewById<EditText>(R.id.subject).text.toString()
            )
        //save settings for later usage
        preferences.edit {
            putString("url",findViewById<EditText>(R.id.url).text.toString())
            putString("service",findViewById<EditText>(R.id.service).text.toString())
            putString("agenturl",findViewById<EditText>(R.id.agenturl).text.toString())
            putString("fname",findViewById<EditText>(R.id.fname).text.toString())
            putString("lname",findViewById<EditText>(R.id.lname).text.toString())
            putString("subject",findViewById<EditText>(R.id.subject).text.toString())
            putString("mail",findViewById<EditText>(R.id.mail).text.toString())
            putString("auth",findViewById<EditText>(R.id.auth).text.toString())
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
            ACRA.log.e("GE_Activity",error.toString())
            Toast.makeText(this@GEActivity, "Error:${error.message}", Toast.LENGTH_SHORT).show()
            return super.onError(error)
        }

        override fun onMessageReceived(message: String) {
            Toast.makeText(this@GEActivity, message.trim(), Toast.LENGTH_LONG).show()
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