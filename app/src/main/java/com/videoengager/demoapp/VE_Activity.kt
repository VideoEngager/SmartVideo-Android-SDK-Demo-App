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
import androidx.core.widget.addTextChangedListener
import com.google.gson.Gson
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.model.Error
import com.videoengager.sdk.tools.LangUtils
import org.acra.ACRA
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
        if(Globals.params==null){
            Globals.params= Gson().fromJson(assets.open("params.json").reader(Charsets.UTF_8),Params::class.java)
        }
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

        //handle deep links
        if(intent.action== Intent.ACTION_VIEW && intent.data!=null){
            val video = VideoEngager(this,sett, VideoEngager.Engine.generic)
            if(video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = listener
                video.VeVisitorVideoCall(intent.dataString?:"")
            }else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }
    }

    val listener = object : VideoEngager.EventListener(){
        override fun onCallFinished() {
            finish()
        }

        override fun onError(error: Error): Boolean {
            ACRA?.log?.e("VE_Activity",error.toString())
            Toast.makeText(this@VE_Activity, "Error:${error.message}", Toast.LENGTH_SHORT).show()
            return super.onError(error)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang?.value ?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}