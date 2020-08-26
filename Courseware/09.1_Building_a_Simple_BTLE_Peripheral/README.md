# Building a Simple BTLE Peripheral

During this Chapter we will be building a simple Bluetooth Low Energy (BTLE or BLE) peripheral and an iOS application to connect and use it.

BTLE has become very popular for connecting to "smart" devices. The combination of broad support in smartphones and tablets and low-cost BTLE chips has accelerated its adoption.  It is useful for a whole range of applications, particularly for devices that have limited UIs and/or are battery powered - the standard truly provides a low-energy communications mechanism.

The BTLE specification has two roles:  **Central** (typically a device like your smartphone) and **Peripheral** (a device like a fitness tracker).  Some devices can switch between roles as needed, while many devices, like a fitness tracker are only peripherals.  We will be adding BTLE Peripheral support to LAMPI so that a mobile app can control it.

The [Raspberry PI 3 B board has WiFI and BTLE built-in](https://www.raspberrypi.org/products/raspberry-pi-3-model-b-plus/).  Under Linux, Bluetooth is supported by the [BlueZ](http://www.bluez.org/about/) project.  We will be using BlueZ, but indirectly through a [NodeJS](https://nodejs.org/en/) library called [bleno](https://github.com/sandeepmistry/bleno) designed specifically for building BTLE peripherals.  Bleno is currently one of the best options available for rapidly building BTLE Peripherals on Linux.

## NodeJS

NodeJS is a JavaScript environment that allows JavaScript to be used as a general-purpose programming language, freeing it from web browsers.  Like many dynamic languages, `node` is the binary used to run JavaScript programs, and is also an interactive REPL.  You can just run `node` and begin evaluating Javascript statements.  (Note: to avoid naming conflicts on some Linux systems, the `node` binary is renamed to `nodejs` - there was already an unrelated package nanmed "node").

## Installing NodeJS

We need to install NodeJS.  Unfortunately, the versions in the Raspbian Package repository tend to be fairly dated given how rapidly NodeJS is developing.  We will be installing from NodeJS with [nvm](https://github.com/creationix/nvm) the Node Version Manager.

Note: we are bypassing the Raspbian (Debian) package management systems (e.g., `apt-get`) with this.

> **COMPATIBILITY:** we will be installing a stable, Long-term Support (LTS) NodeJS version 8.15.1

### Installing **nvm**

Run the following:

```bash
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.34.0/install.sh | bash
```

That will install **nvm** in **your** directory (do not attempt to install this as the superuser).  A set of hidden directories are created, under **~/.nvm**.

### Installing NodeJS 

After installing **nvm**, you need to "source" the Bash script that enabled **nvm** in your shell (logging out and logging back in should do this, as well, as installing **nvm** modifies your Unix shell environment):

```bash
source ~/.nvm/nvm.sh
```

> **NOTE:** since NodeJS is not installed system-wide, or in standard binary paths that the shell will search for programs, you need to **source** the **/home/pi/.nvm/nvm.sh** script to properly add the search paths, environment variables, etc. required to access NodeJS.

Then you can install NodeJS.  We will install a stable Long-Term Support (LTS) version, 8.15.1.

```bash
nvm install 8.15.1
```

This will instal NodeJS as `node` and `npm` the NodeJS Package Manager.  `npm` is equivalent to Python's `pip` - it will install third party libraries and their dependencies.

### Install `bleno`:

We need to install some system dependencies needed for `bleno`:

```bash
sudo apt-get install libudev-dev
```

Install `bleno` with 'npm':

```bash
npm install bleno
```

(Note: the output from the above might look like it is full of scarry warnings and horrible things happening.  Odds are, the install will succeed, despite the warnings.)

#### Granting Permissions

We need to grant system permissions to `node` so that it can acess "raw" devices (including the Bluetooth hardware):

```bash
sudo setcap cap_net_raw+eip $(eval readlink -f `which node`)
```

#### Testing `bleno`

Do a quick test with bleno to make sure it is installed, permissions are set correctly, etc.:

```bash
echo "var bleno = require('bleno'); process.exit();" | node
```

If everything is installed correctly, that should run and exit without errors, returning you to a shell prompt.

### Disable 'bluetooth' Daemon

We need to disable the default **bluetooth** daemon; it will interfere with `bleno`.:

Do the following to stop it

```bash
sudo systemctl stop bluetooth
```

and then do the following to make sure it will not be restarted when the system is restarted:

```bash
sudo systemctl disable bluetooth
```

Then reboot your LAMPI to make sure we are properly configured.

After rebooting:

```bash
sudo systemctl status bluetooth
```

which should indicate that the 'bluetooth' is disabled.

### Starting Bluetooth HCI

Next we want to make sure the Bluetooth Host Controller Interface (HCI) is "up".  HCI is the standard used for software communication to Bluetooth hardware.  `hciconfig` is the Bluetooth equivalent of the `ifconfig` command for network interfaces.  On the Raspberry Pi 3, 'hci0' is the built-in Bluetooth device.

Run:

```bash
sudo hciconfig
```

which should output something like:

```bash
hci0:	Type: BR/EDR  Bus: UART
	BD Address: B8:27:EB:A2:EF:B4  ACL MTU: 1021:8  SCO MTU: 64:1
	UP RUNNING
	RX bytes:654 acl:0 sco:0 events:33 errors:0
	TX bytes:419 acl:0 sco:0 commands:33 errors:0
```

If you do not see 'UP RUNNING' you can start the HCI with 

```bash
sudo hciconfig hci0 up
```

To make sure that happens reliably everytime the system starts up, create a **supervisord** configuration like so:

```ini
[program:hci0_up]
command=/bin/hciconfig hci0 up
priority=200
autostart=true
autorestart=false
startretries=1
```

(note the `autorestart=false` and `startretries=1` - **hciconfig** runs and exits - it does not keep running).

## Simple iBeacon with `bleno`

We are going to build our first BTLE Service with `bleno`; we will build a simple [iBeacon](https://en.wikipedia.org/wiki/IBeacon) compatible service.  iBeacon's are used for proximity detection - they periodically (typicallly a few times per second) broadcast a BTLE advertising packet.  BTLE Central devices can scan for nearby BTLE peripherals.  By measuring the strength of the BTLE received RF signal from the beacon, the Central can estimate the distance to the iBeacon device.  Because RF power measurements are noisy, the distance is just an estimate.  If multiple iBeacons are in the area, though, and the precise locations of iBeacons are known, a BTLE Central can develop a fairly accurate position (this is the indoor equivalent of GPS).

Generally BLTE Central's connect to Peripherals in two-phases:

1.  Discover BTLE Peripherals that are Advertising
2.  Connect to the desired BTLE Peripheral

For iBeacons, though, only Step 1 is done - iBeacon's Advertise, but are not connectable - they do not provide any actual BLTE Services (although they can provide a small amount of useful information in their broadcast Advertisement packets).

### Some Tools

To see whether or not your Raspberry Pi 3 BTLE is behaving as expected you will need some software tools.

These will be critical for the Assignment.

For general Bluetooth Debugging:

* [LightBlue Explorer for iOS](https://itunes.apple.com/us/app/lightblue-explorer-bluetooth-low-energy/id557428110?mt=8)
* [LightBlue for Android](https://play.google.com/store/apps/details?id=com.punchthrough.lightblueexplorer&hl=en_US)
* [BLE Scanner for Android](https://play.google.com/store/apps/details?id=com.macdom.ble.blescanner&hl=en)

Additionally, for the following section, having an iBeacon tool is handy:

* [Locate Beacon for iOS](https://itunes.apple.com/us/app/locate-beacon/id738709014?mt=8)
* [Locate Beacon for Android](https://play.google.com/store/apps/details?id=com.radiusnetworks.locate&hl=en)

(there are many options - find one you like).

## Bluetooth Low Energy Device Name

A BTLE Device can provide a friendly name in a few different ways - it can be included in the BTLE Advertisement Data, and can be provided as a Characteristic in some GATT Services (including the [Generic Access Service](https://www.bluetooth.com/specifications/gatt/)).  

**WARNING:** iOS's Core Bluetooth, which we will be using later, has some potentially annoying caching behavior - it will cache a device's name, and even if later that device's name changes, the cache will not update without resetting the phone.  For that reason, please set up your device name **the first time you use `bleno`**.

Configure your Device Name like so in NodeJS:

```node
var child_process = require('child_process');
var device_id = child_process.execSync('cat /sys/class/net/eth0/address | sed s/://g').toString().replace(/\n$/, '');
var device_id = 'LAMPI ' + device_id;
```

This will result in a name like `'LAMPI b827eb08451e'` (similar to our Device ID).

### iBeacon

Create a new file on your LAMPI named **ibeacon.js**:

```node
var child_process = require('child_process');
var device_id = child_process.execSync('cat /sys/class/net/eth0/address | sed s/://g').toString().replace(/\n$/, '');

process.env['BLENO_DEVICE_NAME'] = 'LAMPI ' + device_id;

var bleno = require('bleno');

// iBeacon UUID and variables
var uuid = 'B9407F30-F5F8-466E-AFF9-25556B57FE6D'; // Estimote iBeacon UUID
var major = 0;
var minor = 0;
var measuredPower = -59;

bleno.on('stateChange', function(state) {
  if (state === 'poweredOn') {
    console.log('poweredOn');
    bleno.startAdvertisingIBeacon(uuid, major, minor, measuredPower, function(err)  {
      if (err) {
        console.log(err);
      }
    });
  }
  else {
    console.log('not poweredOn');
  }
});

bleno.on('advertisingStart', function(err) {
  if (!err) {
    console.log('advertising...');
    // normally after 'advertisingStart' we would set our services
    //   for iBeacon, though, that is not useful
  }
});
```

Run this program:

```bash
node ibeacon.js
```

You should see ```advertising``` show up in the shell to indicate that `bleno` is running and advertising now.

Using `LightBlue` in Discovery mode, you should see your device show up in the list (you can use the Filter option in the top right corner of the `LightBlue` discovery screen to filter out devices with low signal strength - this is helpful if you're in an area with many BTLE devices; put your phone near your LAMPI so the signal is very strong until you're sure you are seeing your device).

Using `LocateBeacon` look for an Estimote Beacon with Major and Minor of 0.  [Estimote](https://estimote.com/) is  a popular manufacturer of iBeacons.  We are using their UUID for this test, so `LocateBeacon` will identify your Raspberry Pi 3 device as an Estimote Beacon.

![](Images/our_ibeacon.png)

If you kill your NodeJS program (CTRL-C) the Beacon should disappear from the list (you might need to kill `LocateBeacon` and restart - it seems to cache the Beacon for a while).

#### iBeacon Application Explanation

What is the application doing?  Let's walk through it:

```node
var child_process = require('child_process');
var device_id = child_process.execSync('cat /sys/class/net/eth0/address | sed s/://g').toString().replace(/\n$/, '');

process.env['BLENO_DEVICE_NAME'] = 'LAMPI ' + device_id;
```


> `process.env` is a NodeJS specific JS module that helps map OS process information, including Environment Variables, into NodeJS.  By default, `bleno` will use the device's hostname for the BTLE Device Name.  Setting the `BLENO_DEVICE_NAME` key of `process.env`, however, will override that with whatever string is provided.  As described above, this is generating a unique BTLE device name based on our Device ID (eth0 MAC Address).


```node
var bleno = require('bleno');
```

> `require()` is analogous to `import` in Python; in NodeJS, though, we have to import the module, and assign the resulting value to a local variable, by convention, the name of the module being imported.

```node
// iBeacon UUID and variables
var uuid = 'B9407F30-F5F8-466E-AFF9-25556B57FE6D'; // Estimote iBeacon UUID
var major = 0;
var minor = 0;
var measuredPower = -59;
```

> iBeacon's include four pieces of information in their Advertisement broadcasts (along with the standard BTLE Advertisement data):

> * a 128-bit UUID (BTLE makes heavy use of UUIDs) identifying the beacon type - typically identical for all beacons in a group; in our example we are using Estimote's UUID - since many third party tools (like `Locate Beacon` support Estimote iBeacons, our beacon shows up automatically
> * major number - a 16-bit value used to identitfy a specific beacon, in conjunction with `minor number`
> * minor number - a 16-bit value used to identitfy a specific beacon, in conjunction with `major number`
> * measured power at 1m - the transmit power of the beacon measured at 1m away in dB; used by receivers to estimate distance based on the [RSSI](https://en.wikipedia.org/wiki/Received_signal_strength_indication) (we are just making a number up here) 

```node
bleno.on('stateChange', function(state) {
  if (state === 'poweredOn') {
    console.log('poweredOn');
    bleno.startAdvertisingIBeacon(uuid, major, minor, measuredPower, function(err)  {
      if (err) {
        console.log(err);
      }
    });
  }
  else {
    console.log('not poweredOn');
  }
});

bleno.on('advertisingStart', function(err) {
  if (!err) {
    console.log('advertising...');
    // normally after 'advertisingStart' we would set our services
    //   for iBeacon, though, that is not useful
  }
});
```

> `bleno` uses a common programming pattern in NodeJS, an asynchronous event model; many NodeJS classes, including `bleno` inherit from an [EventEmitter](https://nodejs.org/api/events.html) class.  Clients of the class may attach event callbacks (aka "handlers" - functions basically) to events they are interested in; later, when those events occur, events are delivered to the handlers (events are 'emitted'). Clients register handlers with the `on` function, and provide an event name (a character string), and the callback function.  In many cases, the function is an "anonymous" function - one that is not named, but declared in-line, like the above.  Descendants of the EventEmitter class invoke callbacks with the `emit()` function, which can pass zero or more variables to the called function.
> 
> If you are not familiar with this style of programming, it can be a little confusing at first.
> In the code above, we are registering callbacks for two events: `"stateChange"` and `"advertisingStart"`. In both cases we are providing an anonymous function. 
>
> Additionally, the call to the `bleno.startAdvertisingIBeacon()` function demonstrates another common pattern in NodeJS, where a callback function is provided and is used to handle the result of the function - by convention the first parameter to the function is an error code.  We are passing in an anonymous function that takes a single argument, **err**, with ```function(err)```.
> 
> The "stateChange" event indicates changes to the hardware state (HCI) - in this case we watch for the "poweredOn" state to let us know that we can start advertising.
>
> The "advertisingStart" event indicates that the BTLE hardware is broadcasting the Advertising packets.  Normally we would set up our BTLE Services when Advertising has started.  Since iBeacons do not have any Services, that is not needed here (but will be later).

FYI, the Bluetooth Specifications require some additional Services for compliance - `bleno` provides those automatically for you - you can see them in `bleno`'s source code if you look in `hci-socket/gatt.js` in the `setServices()` function.



Next up: [09.2 Building a Simple BTLE GATT Service](../09.2_Building_a_Simple_BTLE_GATT_Service/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
