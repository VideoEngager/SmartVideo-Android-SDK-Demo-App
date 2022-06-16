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
import com.videoengager.sdk.VideoEngager


class WebChat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_chat)
        val msgBox = findViewById<EditText>(R.id.msgBox)
        val chatBox = findViewById<TextView>(R.id.chatBox)
        val scrollView = findViewById<ScrollView>(R.id.msgScroll)
        chatBox.text=""
        findViewById<ImageButton>(R.id.sendButt).setOnClickListener {
            Globals.chat?.SendMessage(msgBox.text.toString())
            chatBox.append("ME->${msgBox.text.toString()}\n")
            scrollView.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
            msgBox.text.clear()
        }
        Globals.chat?.onEventListener=object : VideoEngager.EventListener(){

            override fun onCallFinished() {
                Toast.makeText(this@WebChat,"Chat Ended!",Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onMessageReceived(message: String) {
                chatBox.append("${Globals.agentName}->${message}\n")
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
                if(message.startsWith("https") && message.contains(".com/ve/")){
                    Globals.chat?.VeVisitorVideoCall(message)
                }
            }


        }
    }

    override fun onBackPressed() {
        Globals.chat?.Disconnect()
        //super.onBackPressed()
    }
}