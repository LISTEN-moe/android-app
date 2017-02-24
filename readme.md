# LISTEN.moe Unofficial Android App 

The app serves as a native solution for the [LISTEN.moe](https://listen.moe/#/home "LISTEN.moe Homepage") website on android devices running Android Lollipop (21) and above. Support for Android Kitkat 4.4.2 (19) is coming soon (probably).
In its current state the app was completely rewritten from the Beta release. The new design is intended to be as asynchronous as possible which allows for future code to integrate seamlessly.
***
# Features:
##### Currently Implemented:
- Connect to WebSocket (ReConnects if Unintended Disconnection Occurs)
- Parse JSON from WebSocket
- Play Music Stream (Restarts if Unintended Disconnection Occurs)
- User Authentication
- Login Features:
    * Authenticate WebSocket
    * Toggle Favorite Status of Currently Playing Song
    * Search Song Database to:
        * Request Songs
        * Toggle Favorite Status of Songs
    * Manage Favorites List
- Notification:
    * Displays Now Playing info
    * Play/Pause
    * Toggle Favorite Status of Currently Playing Song
    * Stop Music Stream & Close Notification
##### Planned:
-  __High Priority__ 
    * Song History
    * Display Requests Remaining
    * Display Number of Songs in Queue 
- __Low Priority__
    * [Bluetooth Control Compatibility?? ](https://snag.gy/tiYgwn.jpg)
***
# 3rd Party Libraries Used
|Name        | License	|           
| :-------------: |:-------------: |
| [__nv-websocket-client__](https://github.com/TakahikoKawasaki/nv-websocket-client)      | [Apache 2.0]	|
| [__OkHttp__](https://github.com/square/okhttp)      | [Apache 2.0]		|
| [__ExoPlayer__](https://github.com/google/ExoPlayer) | [Apache 2.0]	|      
[Apache 2.0]:https://www.apache.org/licenses/LICENSE-2.0
***
| Command | Description |
| --- | --- |
| git status | List all new or modified files |
| git diff | Show file differences that haven't been staged |
