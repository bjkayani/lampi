# Updating Lampi Code

## Assignment Part 1

In this Assignment you will update the code in the three Lampi applications:

* ```lamp_service.py``` - the hardware daemon
* ```main.py```  - the Kivy UI client
* ```lamp_cmd``` - the command-line client tool

## `client` State

To prevent unwanted feedback, modify all three applications to support the `client` variable in **lamp/set_config** and **lamp/changed** messages, as described in [Preventing Unwanted Feedback](../04.05_Preventing_Unwanted_Feedback/README.md).

Specifically:

* `lamp_service.py`
    * should store `client` in the database  whenever the state is updated
(pick a default value to store in the database if there's no value at startup; an empty string `''` is a good choice)
    * should ignore as invalid any incoming message on **lamp/set_config** that does not contain `client`
    * should publish the value of `client` from the database in **lamp/changed** messages
* `main.py` - the Kivy Client
    * should include a value in `client` when publishing a message on **lamp/set_config** of `'lamp_ui'`
    * should ignore any incoming messages on **lamp/changed** where `client` is equal to `'lamp_ui'`
* `lamp_cmd` - the command-line tool
    * should include a value in `client` when publishing a message on **lamp/set_config** of `'lamp_cmd'`
    * (it doesn't suffer from the feedback issue, so ignoring messages not important)
    
## Quality of Service

We have been using the Paho library defaults of QoS 0 so far.  The messages we are sending currently are important enough that users will observe unexpected behavior if they are not received.  We will fix that.

Update all three apps to use QoS 1 on all messages published and topic subscriptions.

## Client\_ID

Each application should specify a MQTT client\_id (passed into the Paho Client consturctor):

* `lamp_service.py` should use a client\_id of "lamp_service"
* `main.py` should use a client\_id of "lamp_ui"
* `lamp_cmd` should use a client\_id of "lamp_cmd"

## Connection Status

It will be handy to observe the MQTT connection status of our two long-running services, "lamp_service.py" and "main.py".

Modify both of them to have the following behavior.

When connecting, each service should specify a Will with the following values:

* a Payload of `"0"`
* a topic of `lamp/connection/<client_id>/state`
* Retain True
* a QoS of 2

where &lt;client\_id&gt; is the same as the service's &lt;client\_id&gt; specified above.

_Immediately_ after connecting to the broker, each service should publish a message:

* a Payload of `"1"`
* a topic of `lamp/connection/<client_id>/state`
* Retain True
* a QoS of 2

Using mosquitto\_sub on the Pi, prove that the services state is being published properly, by killing one service at a time and observing the state change.

Next up: go to [Supervisor for Lampi](../04.08_Supervisor_for_Lampi/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
