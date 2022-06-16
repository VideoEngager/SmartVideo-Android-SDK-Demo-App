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
import androidx.core.widget.addTextChangedListener
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class VE_Activity : AppCompatActivity() {
    lateinit var veVisitorUrl : EditText
    lateinit var preferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_v_e)
        preferences = getSharedPreferences("ve_preferences", MODE_PRIVATE)
        veVisitorUrl = findViewById(R.id.ve_url)
        veVisitorUrl.setText(preferences.getString("veUrl",""))

        val sett=Globals.params?.generic_params_init!!
        sett.Language = MainActivity.Lang?:VideoEngager.Language.ENGLISH
//        val sett = Settings(
//            "",
//            "",
//            "videome.videoengager.com",
//            "",
//            "",
//            "",
//            "Johna","Demo","Demo","Demo",
//            "demo@demo.dd","123456",
//            MainActivity.Lang!!
//        )
        findViewById<Button>(R.id.button_audio).setOnClickListener {

            val video = VideoEngager(this,sett, VideoEngager.Engine.generic)
            if(video.Connect(VideoEngager.CallType.audio)) {
                video.onEventListener = listener
                video.VeVisitorVideoCall(veVisitorUrl.text.toString())
            }else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.button_video).setOnClickListener {
            val video = VideoEngager(this,sett, VideoEngager.Engine.generic)
            if(video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = listener
                video.VeVisitorVideoCall(veVisitorUrl.text.toString())
            }else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        veVisitorUrl.addTextChangedListener {
            preferences.edit().putString("veUrl",veVisitorUrl.text.toString()).apply()
        }
    }

    val listener = object : VideoEngager.EventListener(){
        override fun onCallFinished() {
            finish()
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@VE_Activity, "Error:$message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang?.value ?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}