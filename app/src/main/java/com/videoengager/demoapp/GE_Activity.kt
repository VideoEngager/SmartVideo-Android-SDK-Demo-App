//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class GE_Activity : AppCompatActivity() {
    lateinit var sett:Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_e)


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


    }

    fun readSettings(){
       // Globals.params?.genesys_cloud_params_init?.let {
            sett = Settings(
                VideoengagerUrl =  "https://videome.leadsecure.com",
                Environment = findViewById<EditText>(R.id.url).text.toString()+"/"+findViewById<EditText>(R.id.service).text.toString(),
                AgentShortURL = findViewById<EditText>(R.id.agenturl).text.toString(),
                MyFirstName = findViewById<EditText>(R.id.fname).text.toString(),
                MyLastName = findViewById<EditText>(R.id.lname).text.toString(),
                MyNickname = findViewById<EditText>(R.id.subject).text.toString(),
                MyEmail =  findViewById<EditText>(R.id.mail).text.toString(),
                Language = MainActivity.Lang?: VideoEngager.Language.ENGLISH,
                AuthorizationHeader = findViewById<EditText>(R.id.auth).text.toString(),
                DeploymentId = null,OrganizationId = null,TennathId = null,Queue = null,MyPhone = null
            )

    }

    val listener = object : VideoEngager.EventListener(){
        override fun onDisconnected() {
            finish()
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@GE_Activity, "Error:$message", Toast.LENGTH_SHORT).show()
        }

        override fun onMessageReceived(message: String) {
            runOnUiThread {
                Toast.makeText(this@GE_Activity, message.trim(), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang!!.value?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}