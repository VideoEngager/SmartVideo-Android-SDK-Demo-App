//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class GC_Activity : AppCompatActivity() {
    lateinit var sett:Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_c)
        Globals.params?.genesys_cloud_params_init?.let {
            findViewById<EditText>(R.id.orgid).setText(it.OrganizationId)
            findViewById<EditText>(R.id.depid).setText(it.DeploymentId)
            findViewById<EditText>(R.id.videourl).setText(it.VideoengagerUrl)
            findViewById<EditText>(R.id.tenid).setText(it.TennathId)
            findViewById<EditText>(R.id.env).setText(it.Environment)
            findViewById<EditText>(R.id.queue).setText(it.Queue)
            findViewById<EditText>(R.id.name).setText(it.MyNickname)

        }

        findViewById<Button>(R.id.buttonaudio).setOnClickListener {
            // audio mode only
            readSettings()
            val video = VideoEngager(this, sett, VideoEngager.Engine.genesys)
            if (video.Connect(VideoEngager.CallType.audio)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.buttonvideo).setOnClickListener {
            readSettings()
            val video = VideoEngager(this, sett, VideoEngager.Engine.genesys)
            if (video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.buttonchat).setOnClickListener {
            readSettings()
            Globals.chat = VideoEngager(this, sett, VideoEngager.Engine.genesys).apply {
                if (Connect(VideoEngager.CallType.chat)) {
                    onEventListener = object : VideoEngager.EventListener() {
                        override fun onChatAccepted() {
                            val intent = Intent(this@GC_Activity, WebChat::class.java)
                            startActivity(intent)
                            finish()
                        }

                        override fun onDisconnected() {
                            finish()
                        }
                    }
                } else Toast.makeText(
                    this@GC_Activity,
                    "Error from connection",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    fun readSettings(){
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
                findViewById<EditText>(R.id.name).text.toString(), "",
                "myMail@aa.aa", "",
                Language = MainActivity.Lang?: VideoEngager.Language.ENGLISH
            )
        }
    }

    val listener = object : VideoEngager.EventListener(){
        override fun onDisconnected() {
            finish()
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@GC_Activity, "Error:$message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang!!.value?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}