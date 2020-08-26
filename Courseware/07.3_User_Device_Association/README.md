# User-Device Association

## The Out-of-the-Box Experience

We need to spend a little time and effort on the user experience of the LAMPI [unboxing](https://en.wikipedia.org/wiki/Unboxing) - that is, what happens when a consumer takes their LAMPI out of the box for the first time?  Consumer electronics companies now spend a great deal of time and money on the initial out-of-the-box experience (experience design, software, packaging, material selection, getting started documents, etc.) as it has shown to improve user ratings and reduce support requests when done well.

Given your experience with products and smart phone and tablets, you could generate an unboxing scenario, and what needs to happen at each step.  It might go something like:

1. Consumer opens the box
2. Consumer removes the top cardboard insert, revealing a Getting-Started card and LAMPI
3. Consumer removes LAMPI from the bottom cardboard insert (revealing the power supply, laying below LAMPI)
4. Consumer removes the power supply
5. Seeing the friendly diagram on the Getting-Started card, Consumer plugs the power supply into LAMPI and plugs the power supply into the wall.
6. LAMPI's friendly, intuitively touch screen interface immediately pops up
7. Consumer configures wireless network settings on LAMPI to join their home network
8. Consumer, directed by the Getting-Started card, goes online and creates a user account in your Django application
9. Consumer associates their physical LAMPI device with their online account and can control LAMPI from anywhere in the world

Steps **1-5** are packaging related and you can probably envision how they might look.

Step **6** is done (well, the touchscreen interface doesn't pop up _immediately_, but that could be optimized).

Step **7** is not done, but you can use your experience with how other devices configure wireless networking (e.g., showing a list of visible network SSIDs and presenting a keyboard interface to enter the network password, using Bluetooth Low Energy on a phone to transfer network SSID and password to a device, etc.) and your experience with Kivy and Raspbian to imagine how a solution might work.

Step **8** can be easily provided by Django and you have certainly experienced similar workflows, signing up for an online account (e.g., GitHub, AWS, etc.).

Step **9**, User-Device association, however, requires some additional thought.  That's what we will focus on next (we will assume that the device is configured from the factory with the hostname and port numbers to use to connect to the Web/Cloud infrastructure over MQTT).

## User-Device Association

Currently, new LAMPI devices are detected by your system when they first connect to the MQTT Broker running on your EC2 instance.

As a refresher, we detect this by monitoring the `$SYS/broker/connection/<CONNECTION>/state` topic (the **mqtt-daemon** background command we created in [Building a Database Model](../06.4_Building_a_Database_Model/README.md)). When a new device is detected (a message is received on `$SYS/broker/connection/+/state` with a payload of `'1'` and no `Lampi` model record can be found for the device's **Device ID**), the **mqtt-daemon** automatically creates a new `Lampi` model instance in the database, and the new device is associated with a `parked_device_user` Django User account.

Associating that LAMPI device with a real User has to be done manually in the Django Admin interface.  That's not a great, long-term solution.

If users were able to just specify a LAMPI **Device ID** in the online system and have that associated with their account, malacious users might (no, definitely would) enter known **Device ID**'s (or even random ones) to steal control of other users's devices.  We need an alternate solution.

There are several different ways to associate a physical device, like LAMPI, with a user's online identity (e.g., a Django User account).  We will use one technique here that allows users to "self-serve" with at least a modicum of security.

### Shared Secret

To securely associate a Django user account with a physical LAMPI device we will use a [Shared Secret](https://en.wikipedia.org/wiki/Shared_secret).  A shared secret is just that - a secret value that is shared only among trusted entities.  In this case, the shared secret will be a unique value that is known to the LAMPI infrastructure (Web/Cloud) and shared with a specific LAMPI device.  The consumer's physical possession of that device (ability to view the touchscreen) allows them to join the "sharing circle".  By entering that shared secret on a Web page, they will prove their ownership of the device.  

#### Generating our Shared Secret

We will generate a sufficiently good enough random value with the [**uuid4()** Python function](https://docs.python.org/3/library/uuid.html#uuid.uuid4), using the hexadecimal string representation:

```python
import uuid
uuid.uuid4().hex
```

Here is an example in the interactive Python3 REPL:

```bash
Python 3.5.2 (default, Nov 23 2017, 16:37:01)
[GCC 5.4.0 20160609] on linux
Type "help", "copyright", "credits" or "license" for more information.
>>> import uuid
>>> secret = uuid.uuid4().hex
>>> print(secret)
da3c72cf84e04f1ab6440132cdf0a087
```

Given the length and randomness of the secret produced this way, the security of this part of our system is "good enough" for our needs (in a more secure system, the shared secret should have a short-lifetime and be updated periodically, maybe every few minutes, like many two-factor authentication systems do).

Next up: go to [Systems-Wide Implementation, Testing, and Isolation](../07.4_Systems_Implementation_Testing_Isolation/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
