Suddenly WiFi - NFC Initiated WiFi-Direct for Android [![CircleCI](https://circleci.com/gh/damian-burke/suddenly-wifi.svg?style=svg)](https://circleci.com/gh/damian-burke/suddenly-wifi)
===================

This repository contains an Android project I have worked on for my bachelor's thesis in computer science: **Suddenly WiFi**. The project is a fully-functional Android application (tested on Samsung Galaxy Nexus with Android 4.1 JellyBean), with the following functionality:

- Exchange of unique identification via NFC
- Initiation of WiFi Direct connection between two phones (theoretically more are possible)
- Exchange of trivial information (e.g. chat example or walkie-talkie example)
- Statistics of the network (incoming/outgoing transfer, connected peers, etc.)

----------

Requirements
-------------

This application requires an Android device with:
- minSdkVersion 16
- NFC
- WiFi Direct

-----------


Usage
-------------
Make sure that both NFC and WiFi are activated on both of your Android devices. Start the application on both devices. Hold the devices' backs against each other (or wherever the NFC chip in your device is located). Via NFC, the device's MAC addresses will be exchanged. The MAC addresses are used as unique identifier within the WiFi Direct realm. 

With both MAC addresses available, a WiFi Direct connection will be established. Several kinds of user-based authentication are possible here (password, PIN, ...), depending on which one is activated within the application.

> **Example Use-Cases**

> - WiFi-Direct offers a higher bandwidth than NFC, Bluetooth or infrared and allows for bigger files to be transferred (e.g. high-resolution photos, videos, documents).
> - WiFi-Direct has a range of roughly 100 meters, depending on the circumstances. This reduces the interruption by running transfers, as the users do not have to be within a couple of meters to keep the connection alive.
> - Wi-Fi Direct works with multiple peers in the same network. It can easily be used for either both gaming or cooperating.


----------

Structure
-------------

The project offers two abstract activity classes: **NfcActivity** and **WiFiActivity** - both of these take care of their respective connection's life-cycle. 

The **NfcActivity** can be extended to easily set a defined payload, or to register callbacks for payload discovery. In the example application, the extended activity sets the device's MAC address as payload, and on the other hand waits for messages of a certain structure to receive the other party's address.

The **WiFiActivity** can be extended to react properly to certain WiFi Direct related events, such as changes in the group, or an established or terminated connection. The same behavior is also mirrored in the abstract **WiFiFragment** class, to offer more flexible behavior with WiFi Direct related information. The sample application uses the WiFiActivity only to send out the network invitation to create the network. Use-case specific handlers are within WiFiFragment instances.  

In the background of the example application and to offer more information on the WiFi Direct group, a BroadcastReceiver is deployed connected to the application's WifiController instance, listening to the following broadcasts:

| Action     | Impact |
| :------- | ----: |
| WIFI_P2P_STATE_CHANGED_ACTION | The device's WiFi is either enabled or disabled |
| WIFI_P2P_PEERS_CHANGED_ACTION    | Peers in the WiFi network have changed. Requesting an updated peer list  |
| WIFI_P2P_CONNECTION_CHANGED_ACTION     | Check if the connection is still established. If connected, re-request connection and group info    |
| WIFI_P2P_THIS_DEVICE_CHANGED_ACTION     |  Ignored at the moment   |



----------

Notes
-------------

The application uses an insensitive MAC address comparison which allows for errors. This has been an issues for the last couple of years, but since the development of this project has come to halt, this has not been addressed. I have posted on stackoverflow whilst developing this application ([MAC address inconsistency](http://stackoverflow.com/questions/10968951/wi-fi-direct-and-normal-wi-fi-different-mac), [WPA supplicant internal errors](http://stackoverflow.com/questions/12216085/wifi-direct-on-jellybean-wpa-supplicant-messed-up), [Lost packets if transmission is not delayed](http://stackoverflow.com/questions/11089232/udp-packets-via-wifi-direct-never-arrive)) but since at that time, WiFi Direct was rather new and not as good commented, it did not solve the problem. 

I do not guarantee that the project works out-of-the-box with current Android APIs. It has been tested on two Samsung Galaxy Nexus running Android 4.1 a couple years ago.

----------

License
-------------

This software is released under the [Apache License v2](https://www.apache.org/licenses/LICENSE-2.0).

---------

Copyright
-------------

Copyright 2016 Damian Burke
