# Assignment

This this assignment you will build a working Bluetooth LAMPI and accompanying iOS application.

## Part 1 - LAMPI Service

The first part of the assignment is to build a working BLTE Service with `bleno` to control LAMPI and update clients on setting changes.

Use `LightBlue` to debug and incrementally test and build your Bluetooth Solution.

All of the NodeJS code for this assignment should located in your repository in the `Lampi/bluetooth` directory with the specified filenames.

The main NodeJS file for your Bluetooth service should be named **peripheral.js**.  **peripheral.js** should be executable as a shell script (e.g., `#! /usr/bin/env node`).

### Services

Your device should Advertise and support the following Services:

| Service Name | Service UUID | Filename |
| ------------ | ------------ | -------- |
| 'Device Information Service' | 0x180A | **device-info-service.js** |
| 'Lamp Service' | 0001A7D3-D8A4-4FEA-8174-1736E808C066 | **lampi-service.js** |

The 'Lamp Service' should be Advertised by name.


### Characteristics

#### Device Information Service Characteristics

The 'Device Information Service' should support the following Characteristics:

| Characteristic | UUID | Descriptors | Value |
| -------------- | ---- | ----------- | ----- |
| [Manufacturer Name String](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.manufacturer_name_string.xml) | 0x2A29 | | 'CWRU' or 'CSU'| 
| | | [0x2901](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_user_description.xml):'Manufacturer Name'| |
| | | [0x2904](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_presentation_format.xml): appropriate value for Characterstic |  | 
| [Model Number String](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.model_number_string.xml) | 0x2A24 | | 'LAMPI'| 
| | | [0x2901](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_user_description.xml): 'Model Number'| |
| | | [0x2904](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_presentation_format.xml): appropriate value for Characterstic |  | 
| [Serial Number String](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.serial_number_string.xml) | 0x2A25 | | <Device_ID> as UTF-8 String | 
| | | [0x2901](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_user_description.xml): 'Serial Number'| |
| | | [0x2904](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_presentation_format.xml): appropriate value for Characterstic |  | 

All of the Device Information Service's Characterstics should be read-only.

#### Lamp Service Characteristics

The 'Lamp Service' should support the following Characteristics:

| Characteristic | UUID |  Filename |
| -------------- | ---- |  ----- |
| On / Off       | 0004A7D3-D8A4-4FEA-8174-1736E808C066 | **lampi-onoff-characteristic.js** |
| Brightness     | 0003A7D3-D8A4-4FEA-8174-1736E808C066 | **lampi-brightness-characteristic.js** |
| HSV            | 0002A7D3-D8A4-4FEA-8174-1736E808C066 | **lampi-hsv-characteristic.js** |


| Characteristic | Descriptors | Value |
| -------------- |  ----------- | ----- |
| On / Off       |  | lamp on/off state encoded in single UINT8 (0x00 is OFF; 0x01 is ON)|
| | [0x2901](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_user_description.xml): 'On / Off'| |
| | [0x2904](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_presentation_format.xml): appropriate value for Characterstic |  |
| Brightness     |  | lamp brightness encoded in UINT8 (0x00 is 0.0; 0xFF is full brightness) |
| | [0x2901](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_user_description.xml): 'Brightness'| |
| | [0x2904](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_presentation_format.xml): appropriate value for Characterstic |  |
| HSV     | | lamp HSV Color encoded in array of three UINT8 elements in this order [hue, saturation, value] (0x00 is 0.0; 0xFF is full hue/saturation) - value should always be 0xFF|
| | [0x2901](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_user_description.xml): 'HSV'| |
| | [0x2904](https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Descriptors/org.bluetooth.descriptor.gatt.characteristic_presentation_format.xml): appropriate value for Characterstic |  |


All of the 'Lamp Service' Service's Characterstics should support: read, write, and notify.


### Lamp State

Your NodeJS application will need to maintain Lamp state, and keep that state updated - this is exactly analogous to the 'DeviceState' of our example `bleno` application.  The Lamp state class should be in a file named **lampi-state.js**


The Lamp Service must communicate with MQTT to change the configuration of LAMPI as well as be updated when other's update the configuration (e.g., the touchscreen).  Unfortunately, the Paho JavaScript client library only works within a Web Browser JavaScript environment.  There is an equivalent MQTT Client, library, though - named [`mqtt`](https://www.npmjs.com/package/mqtt), aptly enough.  It works similarly to Paho.

Install it with NPM:

```bash
npm install mqtt
```

Here is a partial solution for that file to get you started:

```node
var events = require('events');
var util = require('util');
var mqtt = require('mqtt');

function LampiState() {
    events.EventEmitter.call(this);

    this.is_on = true;

    var that = this;

    mqtt_client = mqtt.connect('mqtt://localhost');
    mqtt_client.on('connect', function() {
        console.log('connected!');
        mqtt_client.subscribe('lamp/changed');
    });

    mqtt_client.on('message', function(topic, message) {
        new_state = JSON.parse(message);
        console.log('NEW STATE: ', new_state);
        var new_onoff = new_state['on'];
        var new_brightness = Math.round(new_state['brightness']*0xFF);
        var new_hue = Math.round(new_state['color']['h']*0xFF);
        var new_saturation = Math.round(new_state['color']['s']*0xFF);
        if (that.is_on !== new_onoff) {
            console.log('MQTT - NEW ON/OFF');
            that.is_on = new_state['on'];
            that.emit('changed-onoff', that.is_on);
        }

    });

    this.mqtt_client = mqtt_client;
    
}

util.inherits(LampiState, events.EventEmitter);

LampiState.prototype.set_onoff = function(is_on) {
    this.is_on = is_on;
    var tmp = {'on': this.is_on };
    this.mqtt_client.publish('lamp/set_config', JSON.stringify(tmp));
    console.log('is_on = ', this.is_on);
};


module.exports = LampiState;
```

You should be able to control your lamp with `LightBlue` and see notifications of Lamp state changes (e.g., On/Off, Brightness, Hue, Saturation) when the Lamp is controlled from the touchscreen.

**NOTE:** _your service should ignore lamp state updates that it caused to occur (like our other clients), to prevent feedback loops._

### Supervisor

Your `bleno` NodeJS application should automatically start at boot time.  Here's a Supervisor configuration for it (autostart and autorestart).

```ini
[program:bluetooth_service]
command=/bin/bash -c "source /home/pi/.nvm/nvm.sh && {{ repo_directory }}/Lampi/bluetooth/peripheral.js"
directory={{ repo_directory }}/Lampi/
environment=HOME="/home/pi"
priority=500
autostart=true
autorestart=true
```

## Part 2 - iOS Application

**AFTER** successfully building your Bluetooth solution and testing it with `LightBlue` you can work on Part 2 of the Assignment - updating our iOS application to interact with the lamp.  There is enough complexity in building the iOS app that you should get everything working as expected via `LightBlue` before starting on building the iOS application.

You should start with the iOS solution from the previous week provided in [Mobile/iOS](../Mobile/iOS).  You should modify that project and submit an updated version of that project.

Your iOS application needs to discover and connect to only one LAMPI.

When complete, your iOS application should:

* Upon startup, update the UI to reflect the current state of the LAMP
* Changing On/Off, Brightness, Hue, or Saturation in the iOS application should cause the Lamp to update via BTLE
* Changing On/Off, Brightness, Hue, or Saturation on the touchscreen should cause the iOS application to update via BTLE Notifications
* Do not allow input into the iOS application until all required Characteristics are discovered.
* Limit BTLE write rates to something reasonable (e.g., with `dispatch_after`)
* Handle BTLE disconnections in the application by automatically reconnecting 

The Hue and Saturation is packed into a 3-byte value -- hue is the first byte and saturation is the second byte, represented as 0-255. The third byte is ignored. Here is how to parse it from NSData:

```Objective-C
NSData *data = ...
const unsigned char* bytes = [data bytes];
unsigned char hue = bytes[0];
unsigned char saturation = bytes[1];
float fHue = ((float)hue) / 255.0f;
float fSat = ((float)saturation) / 255.0f;
```

Here is how to pack it into NSData:

```Objective-C
uint32_t hsv = 0;
unsigned char hueInt = (unsigned char)(hue * 255.0);
unsigned char satInt = (unsigned char)(saturation * 255.0);
unsigned char valueInt = 255;

hsv = hueInt;
hsv += satInt << 8;
hsv += valueInt << 16;

NSData *value = [NSData dataWithBytes:&hsv length:sizeof(hsv)];
```

For disabling the user input, [UIView](https://developer.apple.com/library/ios/documentation/UIKit/Reference/UIView_Class/) has a `userInteractionEnabled` property.

## What to Turn in 

You need to turn in the following:

1. A short (a few sentences) write up from each member of the pair summarizing what they learned completing the assignment, and one thing that surprised them (good, bad, or just surprising).  This should in **connected-devices/writeup.md** in [Markdown](https://daringfireball.net/projects/markdown/) format.  You can find a template file in **connected-devices/template\_writeup.md**
2. A Git Pull Request
3. A short video demonstrating the required behaviors emailed to the instructor and TA.  The video should be named **[assignment 3]_[LAST_NAME_1]\_[LAST_NAME_2].[video format]**.  So, for this assignment, if your pair's last names are "Smith" and "Jones" and you record a .MOV, you would email a file named ```2_smith_jones.mov``` to the instructor.
4. A live demo at the beginning of the next class - **be prepared!**

Notes:

* The video must show the iOS app running on an iOS device AND the LAMPI AND your Web Browser Interface at the same time.
* Each pair must have a Mac with Xcode and an iOS device between them.


&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
