# Installing Mosquitto MQTT Broker

### MQTT
[MQTT](http://mqtt.org/) is a "machine-to-machine (M2M) / Internet of Things (IoT) connectivity protocol."  It uses a Publish/Subscribe (aka Pub/Sub) communications model.  All clients, whether publishing or subscribing to message topics, connect to a broker.

We will be using the Open Source [Mosquitto](http://mosquitto.org/) MQTT broker.

### Installing MQTT

> **COMPATIBILITY:** Note: The version of MQTT in the Raspbian repositories is out of date compared to [current versions](https://mosquitto.org/category/releases/), and there are known security issues with this particular release, but the packages available in the repository maintained by the Mosquitto/Eclipse project have broken dependency issues (for example, see [here](https://www.raspberrypi.org/forums/viewtopic.php?t=191027)).  
> 
> For this class, we will use the older, slightly compromised version (version 1.4.10-3).  This is, of course, a bad idea for any real-world system.

Installing:

```
sudo apt-get update
sudo apt-get install mosquitto mosquitto-clients -y
```

After that completes, the mosquitto broker should be installed and running. You can verify that with the "service" tool:

```
pi@raspberrypi:~ $ service mosquitto status
● mosquitto.service - LSB: mosquitto MQTT v3.1 message broker
   Loaded: loaded (/etc/init.d/mosquitto)
   Active: active (running) since Tue 2017-01-31 18:31:55 UTC; 23s ago
   CGroup: /system.slice/mosquitto.service
           └─15640 /usr/sbin/mosquitto -c /etc/mosquitto/mosquitto.conf
```

or with the "ps" command:

```
pi@raspberrypi:~$ ps -A | grep mosquitto
 4803 ?        00:00:00 mosquitto
```
Note: For those new to Unix, that shows the mosquitto broker is running with Process ID (aka "pid") 4803.


Next up: go to [Mosquitto Tools](../03.2_Mosquitto_Tools/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
