package com.videoengager.demoapp

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.videoengager.demoapp.databinding.ActivityAdditionalSettingsBinding

class AdditionalSettingsActivity : AppCompatActivity() {
    private lateinit var prefs : SharedPreferences
    private lateinit var ui : ActivityAdditionalSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAdditionalSettingsBinding.inflate(layoutInflater)
        setContentView(ui.root)
        prefs = getSharedPreferences("additional", MODE_PRIVATE)
        loadUI()
        ui.restrictPhone.setOnCheckedChangeListener { _, isChecked ->
            AlertDialog.Builder(this@AdditionalSettingsActivity)
                .setTitle("Notification")
                .setMessage("This option will be available when the App restarts.\nDo you want to restart app now?")
                .setPositiveButton("Yes") { dialog, which ->
                    dialog.dismiss()
                    save()
                    val intent = Intent(this@AdditionalSettingsActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    Runtime.getRuntime().exit(0)
                 }
                .setNegativeButton("Cancel"){ d, w -> d.dismiss() }
                .show()
        }
    }

    private fun loadUI(){
        ui.avatarImageUrl.setText(prefs.getString("avatarImageUrl",""))
        ui.informationLabelText.setText(prefs.getString("informationLabelText",""))
        ui.backgroundImageURL.setText(prefs.getString("backgroundImageURL",""))
        ui.toolBarHideTimeout.setText(prefs.getString("toolBarHideTimeout","10"))
        ui.customerLabel.setText(prefs.getString("customerLabel",""))
        ui.agentWaitingTimeout.setText(prefs.getString("agentWaitingTimeout","120"))
        ui.allowVisitorToSwitchAudioCallToVideoCall.isChecked=prefs.getBoolean("allowVisitorToSwitchAudioCallToVideoCall",false)
        ui.startCallWithPictureInPictureMode.isChecked=prefs.getBoolean("startCallWithPictureInPictureMode",false)
        ui.startCallWithSpeakerPhone.isChecked=prefs.getBoolean("startCallWithSpeakerPhone",false)
        ui.hideAvatar.isChecked=prefs.getBoolean("hideAvatar",false)
        ui.hideName.isChecked=prefs.getBoolean("hideName",false)
        ui.showAgentBusyDialog.isChecked=prefs.getBoolean("showAgentBusyDialog",true)
        ui.restrictPhone.isChecked = prefs.getBoolean("restrictPhone",false)
    }

    private fun save(){
        prefs.edit {
                putString("avatarImageUrl",ui.avatarImageUrl.text.toString())
                putString("informationLabelText",ui.informationLabelText.text.toString())
                putString("backgroundImageURL",ui.backgroundImageURL.text.toString())
                putString("toolBarHideTimeout",ui.toolBarHideTimeout.text.toString())
                putString("customerLabel",ui.customerLabel.text.toString())
                putString("agentWaitingTimeout",ui.agentWaitingTimeout.text.toString())
                putBoolean("allowVisitorToSwitchAudioCallToVideoCall",ui.allowVisitorToSwitchAudioCallToVideoCall.isChecked)
                putBoolean("startCallWithPictureInPictureMode",ui.startCallWithPictureInPictureMode.isChecked)
                putBoolean("startCallWithSpeakerPhone",ui.startCallWithSpeakerPhone.isChecked)
                putBoolean("hideAvatar",ui.hideAvatar.isChecked)
                putBoolean("hideName",ui.hideName.isChecked)
                putBoolean("showAgentBusyDialog",ui.showAgentBusyDialog.isChecked)
                putBoolean("restrictPhone",ui.restrictPhone.isChecked)
                commit()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        save()
    }
}