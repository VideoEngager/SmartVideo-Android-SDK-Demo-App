# SmartVideo-Android-SDK-Demo-App

The VideoEngager SDK for Android allows you to integrate SmartVideo application in your own Android mobile applications. This way, you would enable your customers to call your agents directly from your Android application through Click to Audio / Video buttons.


## SDK Highlights

* Click to audio
* Click to video
* supports Genesys Cloud
* support SmartVideo standalone
* supports Genesys Engage (coming soon)
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
    implementation 'com.videoengager:smartvideo-sdk:1.6.0'
}
```
**Note**: `minSdkVersion` for the Android SDK is 21 (Android 5.0 "Lollipop").
## Permissions

These permissions should be added to the application's `AndroidManifext.xml` file:

```
<uses-permission android:name="android.permission.INTERNET" />  
<uses-permission android:name="android.permission.CAMERA" />  
<uses-permission android:name="android.permission.RECORD_AUDIO" />  
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />  
<uses-permission android:name="android.permission.BLUETOOTH" />  
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />  
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />    
<uses-feature android:name="android.hardware.camera" />    
<uses-permission android:name="android.permission.FLASHLIGHT"/>    
<permission android:name="android.permission.FLASHLIGHT"  
  android:permissionGroup="android.permission-group.HARDWARE_CONTROLS"  
  android:protectionLevel="normal" />  
<uses-feature android:name="android.hardware.camera.flash" android:required="false" />
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

For more details on how to obtain some of your specific parameters, please consult with your Genesys Cloud administrator or refer to our [HelpDesk article](https://help.videoengager.com/hc/en-us/articles/360061175891-How-to-obtain-my-Genesys-Cloud-Parameters-required-to-setup-SmartVideo-SDKs).



## Get started with SmartVideo SDK
After installation and configuration is done, it is time to integrate the SmartVideo SDK within your own app. Easiest would be to use the demo app in this repository. If you want to do so, pls open with AndroidStudio SmartVideo-Android-SDK-Demo-App Project and Run it.


The SmartVideo SDK will expose the following functionalities to hosting Android/Kotlin/ app:
* place a voice call to VideoEngager standalone or Genesys Cloud queue
* place a video call to to VideoEngager standalone or Genesys Cloud queue
* send chat message to a Genesys Cloud agent over Genesys Cloud chat channel
* receive chat message from a Genesys Cloud agent over Genesys Cloud chat channel

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
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import com.videoengager.sdk.model.Settings

data class Params(
  val genesys_cloud_params_init:Settings,
  val generic_params_init:Settings,
  val genesys_engage_params_init:Settings
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

findViewById<Button>(R.id.button_audio).setOnClickListener {
  //allow more verbose debug Logcat messages 
  VideoEngager.SDK_DEBUG=true
  //change some additional values like preferred Language
  params.genesys_cloud_params_init!!.Language=VideoEngager.Language.ENGLISH
  val video = VideoEngager(this, params.genesys_cloud_params_init!!, VideoEngager.Engine.genesys)
  if (video.Connect(VideoEngager.CallType.audio)) {
      //handle events with event listener
    video.onEventListener = listener
  } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
}

findViewById<Button>(R.id.button_video).setOnClickListener {
  //allow more verbose debug Logcat messages 
  VideoEngager.SDK_DEBUG=true
  //change some additional values like preferred Language
  params.genesys_cloud_params_init!!.Language=VideoEngager.Language.ENGLISH
  val video = VideoEngager(this, params.genesys_cloud_params_init!!, VideoEngager.Engine.genesys)
  if (video.Connect(VideoEngager.CallType.video)) {
    //handle events with event listener
    video.onEventListener = listener
  } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
}

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

https://drive.google.com/file/d/1f5zqj9O65OH42OV-FI5F3smzrwwxXz2B

