//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class GE_Activity : AppCompatActivity() {
    lateinit var sett:Settings
    lateinit var preferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_e)
        preferences = getSharedPreferences("genesys_engage", MODE_PRIVATE)

        findViewById<Button>(R.id.buttonaudio).setOnClickListener {
            // audio mode only
            readSettings()
            val video = VideoEngager(this, sett, VideoEngager.Engine.engage)
            if (video.Connect(VideoEngager.CallType.audio)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.buttonvideo).setOnClickListener {
            readSettings()
            val video = VideoEngager(this, sett, VideoEngager.Engine.engage)
            if (video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
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



    }

    fun readSettings(){
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
                ServiceName = findViewById<EditText>(R.id.service).text.toString()
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
    }

    val listener = object : VideoEngager.EventListener(){
        override fun onDisconnected() {
            finish()
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@GE_Activity, "Error:$message", Toast.LENGTH_SHORT).show()
        }

        override fun onMessageReceived(message: String) {
            Toast.makeText(this@GE_Activity, message.trim(), Toast.LENGTH_LONG).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang!!.value?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}