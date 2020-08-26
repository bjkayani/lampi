# Updating your Kivy UI to use Pub/Sub

In the last two sections you built a simple command-line client and a Lamp service.  The service responds to configuration change requests on an MQTT topic, and notifies any subscribers of changes that have taken effect on a different topic.

In this section, you will update your Kivy application from Assignment 2 to use MQTT Pub/Sub

## Assignment Part 3
A solution for Assignment 2 is provided in the repository for Assignment 3.

Update the Kivy application provided, removing any *pigpio* code, and adding MQTT message publications to make lamp configuration changes, and subscribing to the change notifications to update the Kivy UI.

*Note:* since there is now more than one client making changes to the hardware configuration, the Kivy application cannot assume it is the SPOT any longer, but is just another client of the service.

**Hints:**

* `LampiApp` should have a Paho `Client` attribute, created in the _on\_start()_ method 
* _on\_start()_ should use the _loop\_start()_ method (see [Network Loop](https://www.eclipse.org/paho/clients/python/docs/#network-loop) documentation)
* You should probably also set up an _on\_connect()_ handler for the Paho `Client` object, and set up subscriptions and message handlers in the _on\_connect()_ handler.
* Updating the Kivy UI based on receiving an MQTT message should be very similar to how the Assignment 2 solution updates the UI in _on\_hue()_, _on\_saturation()_, etc.

Next up: go to [What to Turn in](../03.8_Assignment/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
