package com.videoengager.demoapp

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.VideoEngager
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class ErrorReportingApplication : Application() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        SmartVideo.SDK_DEBUG = true
        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.KEY_VALUE_LIST
            logcatArguments = arrayOf("-t","5000","-v","long").asList()
            deleteUnapprovedReportsOnApplicationStart = true
            //deleteUnapprovedReportsOnApplicationStart = false
            //each plugin you chose above can be configured in a block like this:
            mailSender {
                //required
                mailTo = "engineering@videoengager.com"
                //defaults to true
                reportAsFile = true
                //defaults to ACRA-report.stacktrace
                reportFileName = "CrashReport.txt"
                //defaults to "<applicationId> Crash Report"
                subject = "Android SDK demoApp crash report"
                //defaults to empty
               // body = "Hello"

            }

            dialog {
                //required
                text = "Will open email client and compose e-mail with logs as attachment."
                //optional, enables the dialog title
                title = "Submit error report"
                //defaults to android.R.string.ok
                positiveButtonText = "Submit"
                //defaults to android.R.string.cancel
                negativeButtonText = "Cancel"
                //optional, enables the comment input
                //commentPrompt = "Error case"
                //optional, enables the email input
                //emailPrompt = getString(R.string.dialog_email)
                //defaults to android.R.drawable.ic_dialog_alert
                //resIcon = R.drawable.dialog_icon
                //optional, defaults to @android:style/Theme.Dialog
                resTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) android.R.style.Theme_DeviceDefault_Dialog_Alert else android.R.style.Theme_Dialog
                //allows other customization
                //reportDialogClass = MyCustomDialog::class.java
            }
        }
    }
}