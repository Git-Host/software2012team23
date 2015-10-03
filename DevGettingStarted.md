# Emulator Settings #
| Device | Nexus 5 xxhdpi |
|:-------|:---------------|
| Target | 4.3.1 API Level 18 |
| CPU/ABI | Intel Atom (x86), see [link](http://software.intel.com/en-us/android/articles/speeding-up-the-android-emulator-on-intel-architecture) |
| Back Camera | none           |
| Front Camera | none           |
| RAM    | 800            |
| VM Heap | 128            |
| Emulation Options | Use Host GPU   |


# Eclipse import #
  1. download and install android SDK from [developer.android.com](http://developer.android.com/sdk/index.html#ExistingIDE)
  1. install Elcipse SDK plugin for android from download [this Eclipse site](https://dl-ssl.google.com/android/eclipse/)
  1. checkout the Git repository i.e. using .netrc
    * create ~/.netrc as described at [code.google.com](https://code.google.com/p/software2012team23/source/checkout?)
```
mkdir websmstool-source
cd websmstool-source
git clone https://code.google.com/p/software2012team23/
```
  1. create new Eclipse project
    * File -> New -> Project... -> Android -> Android Project from Exisiting Code
    * choose root directory ~/websmstool-source/software2012team23/
    * select both projects
    * click finish
  1. create new AVD as described above
  1. run tests
    * right click on the test project -> Run As -> Android JUnit Test


# Run JUnit tests #
  1. import both projects to eclipse like mentioned before
  1. start emulator i.e. 4.3.x
  1. right click on WebSMSToolTest project -> Run As -> Android JUnit Test
  1. **NEVER EVER RUN TESTS ON YOUR MOBILE WHEN CONNECTED TO CELL (tests send a pot of sms to arbitrary phonenumbers and modify contacts)!**

# Run QUnit tests #
  1. install app on device
  1. configure username/password/http(s)
  1. start service
  1. follow steps described in **Access web UI**
  1. launch tests: https://localhost:8888/clientTest.html


# Access web UI #
to make the web UI accessible a port redirection must be set up
```
rubienr@thinkpad:~$ telnet localhost 5554
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
Android Console: type 'help' for a list of commands
OK
redir add tcp:8888:8888
OK
```

# Simulate receive SMS #
simulate receive short message from "+43/12345678"
```
rubienr@thinkpad:~$ telnet localhost 5554
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
Android Console: type 'help' for a list of commands
OK
sms send +4312345678 the message....
OK
```

# Testing features required by the application #
see also [Features Reference](http://developer.android.com/guide/topics/manifest/uses-feature-element.html#features-reference)
```
rubienr@thinkpad:~/git-source$ ../android-dk/build-tools/21.0.2/aapt dump badging source/WebSMSTool/bin/WebSMS
Tool.apk 
package: name='at.tugraz.ist.akm' versionCode='1' versionName='1.0' platformBuildVersionName='4.1.2-1425332'
sdkVersion:'16'
maxSdkVersion:'19'
targetSdkVersion:'19'
uses-permission: name='android.permission.INTERNET'
uses-permission: name='android.permission.READ_CONTACTS'
uses-permission: name='android.permission.READ_SMS'
uses-permission: name='android.permission.SEND_SMS'
uses-permission: name='android.permission.RECEIVE_SMS'
uses-permission: name='android.permission.WRITE_SMS'
uses-permission: name='android.permission.READ_PHONE_STATE'
uses-permission: name='android.permission.ACCESS_NETWORK_STATE'
uses-permission: name='android.permission.ACCESS_WIFI_STATE'
uses-permission: name='android.permission.WAKE_LOCK'
application-label:'WebSMSTool'
application-icon-160:'res/drawable-mdpi-v4/ic_launcher.png'
application-icon-240:'res/drawable-hdpi-v4/ic_launcher.png'
application-icon-320:'res/drawable-xhdpi-v4/ic_launcher.png'
application-icon-480:'res/drawable-xxhdpi-v4/ic_launcher.png'
application-icon-640:'res/drawable-xxxhdpi-v4/ic_launcher.png'
application: label='WebSMSTool' icon='res/drawable-mdpi-v4/ic_launcher.png'
application-debuggable
launchable-activity: name='at.tugraz.ist.akm.activities.MainActivity'  label='WebSMSTool' icon=''
feature-group: label=''
  uses-feature: name='android.hardware.telephony'
  uses-implied-feature: name='android.hardware.telephony' reason='requested a telephony permission'
  uses-feature: name='android.hardware.touchscreen'
  uses-implied-feature: name='android.hardware.touchscreen' reason='default feature for all apps'
  uses-feature: name='android.hardware.wifi'
  uses-implied-feature: name='android.hardware.wifi' reason='requested android.permission.ACCESS_WIFI_STATE permission'
main
other-services
supports-screens: 'small' 'normal' 'large' 'xlarge'
supports-any-density: 'true'
locales: '--_--'
densities: '160' '240' '320' '480' '640'
```