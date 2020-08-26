# MQTT Bridging

We will connect the mosquitto broker on our Pi's to a broker in EC2.

## Assignment Part 3

Create Mosquitto configuration files for bridging `lampi_bridge.conf` in `/etc/mosquitto/conf.d` on **all LAMPIs** in your group, connecting to **one EC2 broker**.  

The files should be only readable and writable by root (hint: look at the chmod command).

The configuration should:

* connect to your MQTT broker in EC2
* specify a `remote_clientid` of **Device\_ID**\_broker  (with appropriate values for Device ID)
* Map the following topics with QoS 1 in the direction specified:
    * `lamp/set_config` from EC2 to Pi
    * `lamp/changed` from Pi to EC2
    * `lamp/connection/+/state` from Pi to EC2
* On EC2, the topics should be located within the `devices/<device_id>/` hierarchy


Prove that the bridging is working as expected by manually testing the following scenarios:

1. Using mosquitto\_sub on EC2, prove that both lamps are bridged to the EC2 broker by monitoring the broker connection state in the `$SYS`` topic hierarchy (restart mosquitto on the Pi to see the state change; eventually the bridge will reconnect)
1. Using mosquitto\_sub on EC2, prove that the two long-running services connection state is properly indicated in the `devices/` topic hierarchy (kill each ervice in turn to prove that the state changes as expected)
1. Using mosquitto\_sub on EC2, and using the Kivy UI app on first one, then the other, lamp, prove that the correct change notification messages are visible on EC2
1. Using mosquitto\_sub on EC2, and using the command-line client on first one, then the other, lamp,  prove that the correct change notification messages are visible on EC2 (this allows you to verify that the correct numbers are showing up, as opposed to the cruder Kivy test)
1. Using mossquitto\_pub on EC2, send a configuration change request to first one, then the other, lamp, and verify that the proper lamp updates its state, and, with mosquitto\_sub on EC2, responds with the proper change notification


*Note:* during development, you may want to specify `cleansession true`, but for your final submission you should have `cleansession false` (or no `cleansession` configuration since `false` is the default)

*Note:* you do not need to programmatically generate this file.   

Next up: go to [Assignment](../04.10_Assignment/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
