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
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.enums.CallType
import com.videoengager.sdk.enums.Engine
import com.videoengager.sdk.model.Error
import com.videoengager.sdk.tools.LangUtils
import io.ktor.http.Url
import org.acra.ACRA
import java.util.*

class VEActivity : AppCompatActivity() {
    private lateinit var veVisitorUrl : EditText
    private lateinit var preferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_v_e)
        val prefs = getSharedPreferences("additional", MODE_PRIVATE)
        window.applyCustomColors(prefs)
        SmartVideo.SDK_DEBUG = true
        preferences = getSharedPreferences("ve_preferences", MODE_PRIVATE)
        val gc_preferences = getSharedPreferences("genesys_cloud", MODE_PRIVATE)
        veVisitorUrl = findViewById(R.id.ve_url)
        veVisitorUrl.setText(preferences.getString("veUrl",""))
        if(Globals.params==null){
            Globals.params= Gson().fromJson(assets.open("params.json").reader(Charsets.UTF_8),Params::class.java)
        }

        val sett=Globals.params?.genesys_cloud_params_init!!
        sett.Language = MainActivity.Lang?:VideoEngager.Language.ENGLISH
        if(gc_preferences.contains("VideoengagerUrl")){
            sett.VideoengagerUrl = gc_preferences.getString("VideoengagerUrl","").toString()
        }
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
            if (SmartVideo.IsInCall) {
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            } else {
                SmartVideo.Initialize(this, sett, Engine.generic)
                if (SmartVideo.Connect(CallType.video)) {
                    SmartVideo.onEventListener = listener
                    SmartVideo.VeVisitorVideoCall(veVisitorUrl.text.toString())
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.button_pin).setOnClickListener {
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
                Toast.makeText(this@VEActivity,"Enter PIN",Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.button_cb).setOnClickListener {
            if(!SmartVideo.IsInCall) {
                SmartVideo.Initialize(this, sett, Engine.generic)
            }
            SmartVideo.VeStartCoBrowse()
        }

        veVisitorUrl.addTextChangedListener {
            preferences.edit().putString("veUrl",veVisitorUrl.text.toString()).apply()
        }

        //handle deep links
        if(intent.action== Intent.ACTION_VIEW && intent.data!=null){
            if (SmartVideo.IsInCall) {
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            } else {
                Url(intent.dataString!!).let {
                    sett.VideoengagerUrl = it.protocol.name.plus("://").plus(it.host)
                    sett.TennathId = prefs.getString("last_used_tenant", sett.TennathId)
                }
                SmartVideo.Initialize(this, sett, Engine.generic)
                if (SmartVideo.Connect(CallType.video)) {
                    SmartVideo.onEventListener = listener
                    SmartVideo.VeVisitorVideoCall(intent.dataString ?: "")
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val listener = object : VideoEngager.EventListener(){
        override fun onCallFinished() {
            finish()
            SmartVideo.Dispose()
        }

        override fun onError(error: Error): Boolean {
            ACRA.log.e("VE_Activity",error.toString())
            Toast.makeText(this@VEActivity, "Error:${error.message}", Toast.LENGTH_SHORT).show()
            return super.onError(error)
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@VEActivity,message,Toast.LENGTH_LONG).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang?.value ?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}