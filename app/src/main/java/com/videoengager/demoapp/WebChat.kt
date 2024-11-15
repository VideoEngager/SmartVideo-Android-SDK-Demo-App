//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp


import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.VideoEngager
import java.util.concurrent.Executors


class WebChat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_chat)
        val msgBox = findViewById<EditText>(R.id.msgBox)
        val chatBox = findViewById<TextView>(R.id.chatBox)
        val scrollView = findViewById<ScrollView>(R.id.msgScroll)
        chatBox.text=""
        findViewById<ImageButton>(R.id.sendButt).setOnClickListener {
            SmartVideo.SendMessage(msgBox.text.toString())
            chatBox.append("ME->${msgBox.text}\n")
            scrollView.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
            msgBox.text.clear()
        }
        SmartVideo.onEventListener=object : VideoEngager.EventListener(){

            override fun onCallFinished() {
                Toast.makeText(this@WebChat,"Chat Ended!",Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onMessageAndTimeStampReceived(timestamp: String, message: String) {
                chatBox.append("${Globals.agentName}->${message}\n")
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
                if(message.startsWith("https") && message.contains(".com/ve/")){
                    SmartVideo.VeVisitorVideoCall(message)
                }
            }
        }

        findViewById<ImageButton>(R.id.exit_button).setOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        Executors.newSingleThreadExecutor().execute {
            SmartVideo.Dispose()
        }
        super.onBackPressed()
    }
}