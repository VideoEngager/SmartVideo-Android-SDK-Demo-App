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
import android.widget.Toast
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class VE_Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_v_e)
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
            }else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.button_video).setOnClickListener {
            val video = VideoEngager(this,sett, VideoEngager.Engine.generic)
            if(video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = listener
            }else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }
    }

    val listener = object : VideoEngager.EventListener(){
        override fun onDisconnected() {
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