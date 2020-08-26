# MQTT Wills

The various clients in MQTT systems rarely shut down properly, not properly closing sockets and other file descriptors, cleanly shutting down network connections, etc.  They often lose power unexpectdly, or lose their network connection.  With other communication models, this can sometimes lead to strange corner-cases of behavior, half-open sockets, etc.  MQTT supports a simple feature that can help mitigate these unexpected shutdown scenarios, the Will.

## Will Behavior
When connecting to a broker, a client can optionally provide a Will (aka Last Will and Testament - to be read and executed by those left behind).

A Will consists of:

* Message payload
* Topic
* QoS
* Retained Flag

If a client does not properly and orderly disconnect from the broker, the broker will publish the Will Message on the specified Topic on behalf of the recently disconnected client, with the parameters specified at client connection time (e.g., QoS, Retained flag) .

There are several common use cases for this.  The one we will take advantage of is having our Lamp clients publish their connection state to a topic, being a "1" when they are connected, and a "0" when they are disconnected.

## Setting up a Will in Paho
A Will needs to be configured in the client **before** it connects to the broker as the Will is part of the MQTT CONNECT message.  You can use the Paho _will\_set()_ function:

```
will_set(topic, payload=None, qos=0, retain=False)

Set a Will to be sent to the broker. If the client disconnects without calling disconnect(), the broker will publish the message on its behalf.

topic
  the topic that the will message should be published on.
payload
  the message to send as a will. If not given, or set to None a zero length message will be used as the will. Passing an int or float will result in the payload being converted to a string representing that number. If you wish to send a true int/float, use struct.pack() to create the payload you require.
qos
  the quality of service level to use for the will.
retain
  if set to True, the will message will be set as the "last known good"/retained message for the topic.

Raises a ValueError if qos is not 0, 1 or 2, or if topic is None or has zero string length.
```

Open two shells on your Pi.

In the first, set up mosquitto_sub to listen for messages on ```state/+```:

```
$ mosquitto_sub -v -t state/+
```

In the second, start up Python:

```
>>> import paho.mqtt.client as MQTT
>>> c = MQTT.Client()
>>> c.will_set('state/test', "0", qos=1, retain=True)
>>> c.connect('localhost', 1883)
0
>>> c.loop_start()
>>> c.publish('state/test', "1", qos=1, retain=True)
(0, 1)
```

At this point, you should see a message appear in the mosquitto_sub window:

```
$ mosquitto_sub -v -t /tate/+
state/test 1
```

Now, use the _exit()_ function in the Python interpreter to immediately terminate the process:

```
>>> exit()
```

You should immediately see the new state message as the broker executed the Will:

```
$ mosquitto_sub -v -t state/+
state/test 1
state/test 0
```

Because we set the Retained Flag to true for both messages, if you exit mosquitto_sub and start it again:

```
$ mosquitto_sub -v -t state/+
state/test 1
state/test 0
^C
$ mosquitto_sub -v -t state/+
state/test 0
```

So, the combination of Retained messages and Wills allows MQTT to hold some basic connection state information.

Next up: go to [Preventing Unwanted Feedback](../04.05_Preventing_Unwanted_Feedback/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
