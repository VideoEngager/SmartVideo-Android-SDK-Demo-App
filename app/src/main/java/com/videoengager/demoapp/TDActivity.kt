//
// DemoTalkDesk
//
// Copyright © 2023 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.enums.CallType
import com.videoengager.sdk.enums.Engine
import com.videoengager.sdk.model.Error
import com.videoengager.sdk.tools.LangUtils
import io.cobrowse.CobrowseIO
import java.util.Locale

class TDActivity : AppCompatActivity(),CobrowseIO.Redacted {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_td)
        window.applyCustomColors(getSharedPreferences("additional", MODE_PRIVATE))
        val tdPrefs = getSharedPreferences("talkdesk",MODE_PRIVATE)
        val sett=Globals.params?.genesys_cloud_params_init!!
        sett.Language = MainActivity.Lang?: VideoEngager.Language.ENGLISH
        val tenantBox = findViewById<EditText>(R.id.tenid)
        val flowIdBox = findViewById<EditText>(R.id.flowid)
        SmartVideo.SDK_DEBUG = true
        tenantBox.addTextChangedListener {
            tdPrefs.edit().putString("tenant",it.toString()).apply()
            Globals.params?.genesys_cloud_params_init?.TennathId = it.toString()
        }
        flowIdBox.addTextChangedListener {
            tdPrefs.edit().putString("flow",it.toString()).apply()
        }
        tenantBox.setText(tdPrefs.getString("tenant","Xh6at3QenNopCTcP"))
        flowIdBox.setText(tdPrefs.getString("flow","c2a25a3a17fa43ba9c28aab62c9862fd"))
       // flowIdBox.setText(tdPrefs.getString("flow","7e95b243344f46808743bc6ee366bd2f"))
        findViewById<Button>(R.id.button_video).setOnClickListener {
            SmartVideo.SDK_DEBUG=true

            // sett.TennathId = "Xh6at3QenNopCTcP"
            sett.flowId=flowIdBox.text.toString()
            sett.TennathId = tenantBox.text.toString()
            sett.VideoengagerUrl="https://videome.leadsecure.com"

            if (SmartVideo.IsInCall) {
                Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
            } else {
                SmartVideo.Initialize(this, sett, Engine.talkdesk)
                if (SmartVideo.Connect(CallType.video)) {
                    SmartVideo.onEventListener = listener
                } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.button_cb).setOnClickListener {
            if(!SmartVideo.IsInCall) {
                SmartVideo.Initialize(this, sett, Engine.generic)
                SmartVideo.onEventListener = listener
            }
            SmartVideo.VeStartCoBrowse()
        }

        findViewById<Button>(R.id.button_pin).setOnClickListener {
            val pin = findViewById<EditText>(R.id.pin).text.toString()
            if(!pin.isNullOrEmpty()){
                try {
                    sett.TennathId = tenantBox.text.toString()
                    sett.VideoengagerUrl="https://videome.leadsecure.com"
                    SmartVideo.Initialize(this, sett, Engine.generic)
                    if (SmartVideo.Connect(CallType.video)) {
                        SmartVideo.onEventListener = listener
                        SmartVideo.VeVisitorVideoCall(pin)
                    } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
                }catch (ex:Exception){
                    Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this@TDActivity,"Enter PIN",Toast.LENGTH_SHORT).show()
            }
        }
    }



    val listener = object : VideoEngager.EventListener(){
        override fun onCallFinished() {
           // finish()
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@TDActivity,message,Toast.LENGTH_LONG).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang?.value ?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun redactedViews(): MutableList<View> {
        return  mutableListOf(findViewById<EditText>(R.id.flowid))
    }
}