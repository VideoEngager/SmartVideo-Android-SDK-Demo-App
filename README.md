# SmartVideo-Android-SDK-Demo-App

The VideoEngager SDK for Android allows you to integrate SmartVideo application in your own Android mobile applications. This way, you would enable your customers to call your agents directly from your Android application through Click to Audio / Video buttons.


## SDK Highlights

* Click to audio
* Click to video
* Short Url call
* supports Genesys Cloud
* supports Genesys Cloud Web Messaging API
* supports Genesys Cloud Schedule Callbacks
* support SmartVideo standalone
* supports Genesys Engage
* Localization (supports English, Spanish, Portuguese, German, and Bulgarian)


## Prerequisites

* AndroidStudio 4.1 or higher
* androidx.core:core-ktx:1.3.2 or higher
* Android SDK 21 (Android 5.0 "Lollipop") or higher


## Installation

### Gradle
Add the necessary MavenCentral repository into your `build.gradle` file:

```
repositories {
        ...
        mavenCentral()
    }
```

Add the necessary artifact into your `build.gradle` file:
```
dependencies {
    ....
    implementation 'com.videoengager:smartvideo-sdk:1.18.1'
}
```
**Note**: `minSdkVersion` for the Android SDK is 21 (Android 5.0 "Lollipop").
## Permissions

These permissions should be added to the application's `AndroidManifext.xml` file:

```XML
<uses-permission android:name="android.permission.INTERNET" />  
<uses-permission android:name="android.permission.CAMERA" />  
<uses-permission android:name="android.permission.RECORD_AUDIO" />  
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />  
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />  
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />    
```


## Configure to use with your Genesys Cloud Center

### Run within the VideoEngage Genesys Cloud organization
The demo app comes with pre-configured Genesys Cloud account, within the Video Engager organization. To quickly test the demo app, one needs to:
* visit Genesys Cloud [https://login.mypurecloud.com/](https://login.mypurecloud.com/)
* login as agent, by using the following credentials
  * username: mobiledev@videoengager.com
  * password: [ask Video Engager support team to obtain your password](mailto:support@videoengager.com)
* clone and compile this project without any change of parameters in `assets/params.json`
* run the compiled project in his/her Android Phone
* select `Genesys Cloud` and tap on `Start Video` or `Start Audio` button.

Please refer to the following video, from one of our webinars to get a better understand of how to use it the Android SDK.


### Run within the VideoEngage Genesys Cloud organization
If you want to configure the demo app to run with your own Genesys Cloud organization, then `assets/params.json` file shall be updated to provide the correct parameters of your organization.


| Name                        | Type            | Value      |
| --------------------------- | --------------- | ---------------------------------------------------------------- |
| VideoengagerUrl (required)   | String          | `https://videome.videoengager.com` (unless you have a custom subdomain)  |
| TennathId (required)         | String          | `hbvvUTaZxCVLikpB` |
| AgentShortURL (required)     | String          | `mobiledev`   |
| Environment  (required)      | String          | `https://api.mypurecloud.com` or your preferred Genesys Cloud location |
| Queue  (required     )       | String          | Here you need to provide the name of your GenesysCloud queue. This the queue that is setup to process SmartVideo interactions |
| OrganizationId (required)    | String          |  Your GenesysCloud organization Id |
| Deployment ID (required)     | String          | Your SmartVideo deployment Id |

### Run within the VideoEngage Genesys Cloud organization with WEB Messaging channel
If you want to configure the demo app to run with your own Genesys Cloud organization, then `assets/params.json` file shall be updated to provide the correct parameters of your organization.


| Name                        | Type            | Value      |
| --------------------------- | --------------- | ---------------------------------------------------------------- |
| VideoengagerUrl (required)   | String          | `https://videome.videoengager.com` (unless you have a custom subdomain)  |
| TennathId (required)         | String          | `hbvvUTaZxCVLikpB` |
| AgentShortURL (required)     | String          | `mobiledev`   |
| Environment  (required)      | String          | `https://apps.mypurecloud.com` or your preferred Genesys Cloud location |
| Deployment ID (required)     | String          | Your MESSAGING channel deployment Id |


For more details on how to obtain some of your specific parameters, please consult with your Genesys Cloud administrator or refer to our [HelpDesk article](https://help.videoengager.com/hc/en-us/articles/360061175891-How-to-obtain-my-Genesys-Cloud-Parameters-required-to-setup-SmartVideo-SDKs).



## Get started with SmartVideo SDK
After installation and configuration is done, it is time to integrate the SmartVideo SDK within your own app. Easiest would be to use the demo app in this repository. If you want to do so, pls open with AndroidStudio SmartVideo-Android-SDK-Demo-App Project and Run it.


The SmartVideo SDK will expose the following functionalities to hosting Android/Kotlin/ app:
* place a voice call to VideoEngager standalone, Genesys Cloud queue or Genesys Cloud web messaging channel
* place a video call to to VideoEngager standalone, Genesys Cloud queue or Genesys Cloud web messaging channel
* send chat message to a Genesys Cloud agent over Genesys Cloud chat channel or Genesys Cloud web messaging channel
* receive chat message from a Genesys Cloud agent over Genesys Cloud chat channel or Genesys Cloud web messaging channel


By integrating `VideoEngager.EventListener`, Android developers will get an expose to a few more methods that will be covered in section [Error Handling](#Error-Handling).

All the remaining functionalities of the SmartVideo SDK remain under the control of the SDK and are not exposed to hosting Android app.

### Initialize SmartVideo
The initialization step requires to:

* import com.videoengager.sdk.VideoEngager
* Load settings from params.json
* initialize VideoEngager.class




This would require to add inside your Activity the following code snippets:

```Kotlin

import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import com.videoengager.sdk.model.Settings

data class Params(
  val genesys_cloud_params_init:Settings,
  val generic_params_init:Settings,
  val genesys_engage_params_init:Settings,
  val genesys_cloud_messaging_params_init:Settings,
)

//....
lateinit var params:Params;
override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  //load params.json from assets folder using Gson lib
  val params = Gson().fromJson(assets.open("params.json").reader(Charsets.UTF_8),Params::class.java)
  //...
}


```

### Add buttons for Click-to-Video and/or Click-to-Audio
This step requires to create buttons for adding a click to video and/or a click to voice functionality. Below is an example:

```Kotlin
// ....

val listener = object : VideoEngager.EventListener(){
  override fun onDisconnected() {
    finish()
  }

  override fun onErrorMessage(type: String, message: String) {
    Toast.makeText(this@VE_Activity, "Error:$message", Toast.LENGTH_SHORT).show()
  }
  override fun onPeerConnectionLost() {
    //fires when Connection from agent was lost  
  }
  override fun onIsConnectedToInternet(isConnected:Boolean) {
    //fires when Internet Connection is changed  
  }
}

/////////////////// Genesys Cloud Chat Channel /////////////////////

fun StartAudioCallOverGenesysChatChannel() {
  //allow more verbose debug Logcat messages 
  SmartVideo.SDK_DEBUG=true
  //change some additional values like preferred Language
  params.genesys_cloud_params_init!!.Language=VideoEngager.Language.ENGLISH
  if(SmartVideo.IsInCall){
      Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
  }else {
      SmartVideo.Initialize(this, params.genesys_cloud_params_init, Engine.genesys)
      if (SmartVideo.Connect(CallType.audio) == true) {
          SmartVideo.onEventListener = listener
      } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
  }
}

fun StartVideoCallOverGenesysChatChannel() {
  //allow more verbose debug Logcat messages 
  SmartVideo.SDK_DEBUG=true
  //change some additional values like preferred Language
  params.genesys_cloud_params_init!!.Language=VideoEngager.Language.ENGLISH
  if(SmartVideo.IsInCall){
      Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
  }else {
      SmartVideo.Initialize(this, params.genesys_cloud_params_init, Engine.genesys)
      if (SmartVideo.Connect(CallType.video) == true) {
          SmartVideo.onEventListener = listener
      } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
  }
}

/////////////////// Genesys Cloud WEB Messaging Channel /////////////////////

fun StartAudioCallOverGenesysWebMessagingChannel() {
  //allow more verbose debug Logcat messages 
  SmartVideo.SDK_DEBUG=true
  //change some additional values like preferred Language
  params.genesys_cloud_messaging_params_init!!.Language=VideoEngager.Language.ENGLISH
  if(SmartVideo.IsInCall){
      Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
  }else {
      SmartVideo.Initialize(this, params.genesys_cloud_messaging_params_init, Engine.genesys_messenger)
      if (SmartVideo.Connect(CallType.audio) == true) {
          SmartVideo.onEventListener = listener
      } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
  }
}

fun StartVideoCallOverGenesysWebMessagingChannel() {
  //allow more verbose debug Logcat messages 
  SmartVideo.SDK_DEBUG=true
  //change some additional values like preferred Language
  params.genesys_cloud_messaging_params_init!!.Language=VideoEngager.Language.ENGLISH
  if(SmartVideo.IsInCall){
      Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
  }else {
      SmartVideo.Initialize(this, params.genesys_cloud_messaging_params_init, Engine.genesys_messenger)
      if (SmartVideo.Connect(CallType.video) == true) {
          SmartVideo.onEventListener = listener
      } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
  }
}


```

### Additional settings
SDK customizations are available with some additional parameters in ```Settings.class```:
```Kotlin
//Map of KeyValues sent as headers with start of interaction 
var CustomFields : Map<String,Any>

//Image URL that will be displayed to Agent 
var AvatarImageUrl : String?

//Allows user to switch from Audio Call to Video Call in audio only scenario
var allowVisitorToSwitchAudioCallToVideoCall : Boolean

//Additional Label shown at the top of CallScreen (supports base HTML tags)
var informationLabelText:String?

//Image URL that will be displayed as background in Call Screen (supports hex color strings as #AABBCC)
var backgroundImageURL:String?

//Class to control RingingScreen UI
var outgoingCallVC:OutgoingCallVC? {
      //Show/Hide Agent avatar image
      var hideAvatar:Boolean?=false,
      //Show/Hide Agent name
      var hideName:Boolean?=false 
    }
    
//Controls direct "minimized" start of CallScreen    
var startCallWithPictureInPictureMode:Boolean?

//Controls direct start with SpeakerPhone in CallScreen
var startCallWithSpeakerPhone:Boolean?=false,

//CallScreen Toolbars visibility mode 
// Possible values :
//-1 ->  toolbar is always shown and never hidden
// 0 ->  toolbar opens when the user tap and hides when the user tap again. No timeouts
// 1 and up -> toolbar opens when the user tap and hides when the user tap again. Hide timeout (in seconds) is value
var toolBarHideTimeout:Int=10

//Label that will be displayed when we don't have information about Agent.
var customerLabel:String?=null,

// Wait time (in seconds) for Agent pickup.
var agentWaitingTimeout:Int=120

```

### Listen for events with `VideoEngager.EventListener`
```kotlin
val listener = object : VideoEngager.EventListener(){  
        override fun onChatAccepted(){
            //fires when agetn accept chat interaction  
        }
      
        override fun onMessageAndTimeStampReceived(timestamp: String,message:String){
            //same as "onMessageReceived" but with timestamp of message
        }
        
        override fun onAgentOnline(agentInfo:AgentInfo?){
             //fires if agent is online
        }
        
        override fun onAgentUnavailable(){
             //fires when agent go offline or is unavaiable  
        }
        
        override fun onPeerConnectionLost(){
           //fires when Connection from agent was lost  
        }
        
        override fun onIsConnectedToInternet(isConnected:Boolean){
            //fires when Internet Connection is changed
        }
        
        override fun onError(error:Error):Boolean{
            //fires on every error message thrown from SDK modules
          // if error.hasInternalMessage==true you can prevent showing of internal message by return false , default is true (shows internal SDK message to user) 
          // if error.severity==FATAL then SDK is stopped automatically otherwise SDK continues work. 
          return super.onError(error)
        }
        
        override fun onCallWaiting(callInfo:CallInfo){
            //fires when SDK starts Ringing
        }
        
        override fun onCallStarted(){
            //fires when Call is started
        }
        
        override fun onCallOnHold(){
            //fires when Call state is changed to onHold
        }
        
        override fun onCallResume(){
            //fires when Call state is resumed
        }
        
        override fun onCallFinished(){
             //fires when Call state is ended or disconnected
        }
        
        override fun onAgentTimeout():Boolean {
         //Fires when Agent didn't pickup call for Settings.agentWaitingTimeout seconds
         //If return true integrated AgentBusy Dialog will be shown otherwise No and Disconnect will be executed internally
        return true
        }

}
```

### DeepLink Handling
This feature is used for "Escalation from WebChat/SMS/Email channel to video call" and "Schedule video call" scenarios.
To implement this feature you must do these steps:
* Add following to your ```AndroidManifest.xml``` for activity that plays with SmartVideo SDK:
```xml
<activity
    android:name=".<YOURSMARTVIDEO Activity>"
    android:screenOrientation="portrait"
    android:exported="true">
    <intent-filter android:label="SmartVideo Call"
        android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="videome.videoengager.com"
            android:pathPrefix="/ve/" />
        <data
            android:scheme="https"
            android:host="videome.leadsecure.com"
            android:pathPrefix="/ve/" />
    </intent-filter>
</activity>
```

* Add following logic to corresponding activity to handle deep link requests :
```kotlin
  //handle deep links
  if(intent.action== Intent.ACTION_VIEW && intent.data!=null){
      if (SmartVideo.IsInCall) {
          Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
      } else {
          SmartVideo.Initialize(this, sett, Engine.generic)
          if (SmartVideo.Connect(CallType.video)) {
              SmartVideo.onEventListener = listener
              SmartVideo.VeVisitorVideoCall(intent.dataString ?: "")
          } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
      }
  }
```

* Send to VideoEngager following information:
```text
      1.Your app PACKAGE NAME
      2.Your app test and production SHA-256 keys fingerprint
```
This information will be added to our ```.well-known/assetlinks.json``` for verification

You can read more about Android deep links here : https://developer.android.com/training/app-links/verify-site-associations#auto-verification

### Short url call
You can use this method of SDK to make you own implementation for Escalation or Schedule scenarios.
If your users receives special VeVisitorVideoCall Url you can pass it in ```VeVisitorVideoCall(Url:String)``` and SDK will call associated Agent.
Example:
```kotlin
val veVisitorUrl="https://videome.leadsecure.com/ve/aBcDef"
if (SmartVideo.IsInCall) {
    Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
} else {
    SmartVideo.Initialize(this, sett, Engine.generic)
    if (SmartVideo.Connect(CallType.video)) {
        SmartVideo.onEventListener = listener
        SmartVideo.VeVisitorVideoCall(veVisitorUrl.text.toString())
    } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
}
```

### PIN code call
You can use this method of SDK to make you own implementation for Escalation or Schedule scenarios.
If your users receives special `PIN code` you can pass it in ```VeVisitorVideoCall(Url:String)``` and SDK will call associated Agent.
Example:
```kotlin
val PIN="1234"
if (SmartVideo.IsInCall) {
    Toast.makeText(this, "Call is in progress!", Toast.LENGTH_SHORT).show()
} else {
    SmartVideo.Initialize(this, sett, Engine.generic)
    if (SmartVideo.Connect(CallType.video)) {
        SmartVideo.onEventListener = listener
        SmartVideo.VeVisitorVideoCall(PIN)
    } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
}
```


### Schedule callback
We provide several methods to request schedule meeting with Genesys Cloud Agent.
You can see implementation in ``GC_Activity.kt`` class in ClickListener of `buttonschedule` element.
Examples:

* Create schedule meeting at specific date/time
```kotlin
val meetingDateTime = Calendar.getInstance()
meetingDateTime.set(2022, 7, 15, 7, 0) //we request meeting at 15.07.2022 07:00
val video = VideoEngager(this, sett, VideoEngager.Engine.genesys)
video.onEventListener = listener
val scheduleCallbackAnswer = object : Answer() {
  override fun onSuccessResult(result: Result) {
   // here we can read information about meeting 
  // please look at ScheduleResultActivity.kt for examples how to proceed with meeting result 
  }
}
video.VeVisitorCreateScheduleMeeting(meetingDateTime.time,true, scheduleCallbackAnswer)
```

* Create schedule meeting as soon as possible
```kotlin
val video = VideoEngager(this, sett, VideoEngager.Engine.genesys)
video.onEventListener = listener
val scheduleCallbackAnswer = object : Answer() {
  override fun onSuccessResult(result: Result) {
    // here we can read information about meeting 
    // please look at ScheduleResultActivity.kt for examples how to proceed with meeting result 
  }
}
video.VeVisitorCreateScheduleMeeting(null, true, scheduleCallbackAnswer)
```

* Read schedule meeting info
  To verify status of meeting you must read information about it.For this operation you need `callId` from requested `Result`.
```kotlin
val callId = "abcfef-asdv-122sasdsd-fasfd"
val video = VideoEngager(this@GC_Activity, sett, VideoEngager.Engine.genesys)
video.onEventListener = listener
val scheduleCallbackAnswer = object : Answer() {
  override fun onSuccessResult(result: Result) {
    // here we can read information about meeting 
    // please look at ScheduleResultActivity.kt for examples how to proceed with meeting result 
  }
}
video.VeVisitorGetScheduleMeeting(callId, scheduleCallbackAnswer)
```

* Delete schedule meeting info
  For this operation you need `callId` from requested `Result`.
```kotlin
val callId = "abcfef-asdv-122sasdsd-fasfd"
val video = VideoEngager(this@GC_Activity, sett, VideoEngager.Engine.genesys)
video.onEventListener = listener
val scheduleCallbackAnswer = object : Answer() {
  override fun onSuccessResult(result: Result) {
    // on success deletion we handle here with Result with null values
  }
}
video.VeVisitorGetScheduleMeeting(callId, scheduleCallbackAnswer)
```

### Availability
We provide several methods to request schedule meeting with Genesys Cloud Agent.
You can see implementation of `Availability API` in ``GCActivity.kt`` and ``AvailabilityActivity.kt`` .

Examples and usages:

* Check for enabled availability support of organization
  For this operation you need to call method `VeChackAgentAvailability` of the SDK.
```kotlin
val video = VideoEngager(this, sett, VideoEngager.Engine.genesys)
video.onEventListener = listener
video.VeChackAgentAvailability(object:Availability(){
  override fun onIsAvailableResult(hasAvailability: Boolean) {
    video.Disconnect()
    if(hasAvailability){
      // Organization support Availability
    }else{
      // Organization do not support Availability
    }
  }
})
```

* Request free Agent's time slots
For this operation you need to call method `VeGetAgentAvailabilityTimeSlots` of the SDK.
For full implementation please look at ``AvailabilityActivity.kt`` method : `fun loadSlots(forDate:Date){...}`

```kotlin
val video = VideoEngager(this@AvailabilityActivity, settings, VideoEngager.Engine.genesys)
video.onEventListener = listener
val forDate = Calendar.getInstance().time
video.VeGetAgentAvailabilityTimeSlots(forDate, 1, object : Availability() {
  override fun onTimeSlotsResult(timeSlots: List<AvailabilityTimeSlots>) {
    // Read and populate timeSlots based on your logic
    //You can use timeSlot to request Schedule video meeting
  }
})
```

### Error Handling
This step requires to prepare your app for error handling.


Run time errors cover the usage of the SDK within the host app. When the host app starts a video or audio call, the SDK will initiate a series of REST calls and socket exchange, between the SDK, Genesys Cloud and SmartVideo data centres. If any error happens before the call is established, the SDK won't provide any UI for error handling and will only return error message through an optional  method, named `onErrorMessage(type:String,message:String)`. An error at this stage can be triggered, if wrong parameters are provided in `assets/params.json`. Host app developers shall be responsible for handling errors at this stage. This demo app provides a simplified error handling, which can be reviewed by going to file `GC_Activity.kt` and looking in the VideoEngager.EventListener method ` override fun onErrorMessage(type: String, message: String)`


The other type of errors that this SDK handles is during already established call. For instance, if the host app internet gets down for some reason, the SDK will inform the mobile app user and shortly after that the call will be ended. The host app can implement an optional delegate method, if some other action is required to be processed, when this event is triggered. This event method is `override fun onIsConnectedToInternet(isConnected:Boolean)`.
Another example of error handling inside the SDK, are when agent's internet connectivity is down. In this case, the SDK will again inform the mobile app user and shortly after that the call will be also ended. The host app can implement an optional delegate method, if some other action is required to be processed, when this event is triggered. This delegate method is `override fun onPeerConnectionLost()`.



## Minimum Supported Version

`minSdkVersion` for the Android SDK is 21 (Android 5.0 "Lollipop").


## How to contact us

If you have any questions, please contact our [support team](mailto:support@videoengager.com), and we will be happy to help.


## Demo app download link

https://drive.google.com/file/d/1LXyTmuxMD9I8IN41DOwiAnb6eDU98VOo/view?usp=drive_link

