# Creating a Lamp Service

In the previous section you created a command-line client to interact with a yet-to-be-built service.  In this section of the assignment you will build that service.

## Service as a choice for SPOT

As we discussed in the last section, our desk lamp is going to need a Single Point of Truth (SPOT) - in our case, one entity that knows the current state of the desklamp and controls the hardware to achieve that state.

You will be extracting the software you wrote for the Kivy UI application in Assignment 2 that interacts with the hardware into a different application, that clients will interact with via MQTT.

## Services

Services are generally long-running applications that clients can access via a well known "endpoint" (e.g., TCPIP hostname and port number, Unix pipe, etc.).  Our service will subscribe to an MQTT topic `lamp/set_config` to monitor for lamp configuration change requests, and publish notifications of lamp changes on the `lamp/changed` topic.

If a device loses power, it should return to either a default configuration, or the last known state.  Since we are building a "smart" device, LAMPI will return to the last known state.  This will require the service to persist the lamp configuration to the filesystem on every change.  There are numerous ways of storing configuration state on a filesystem - for this lab, please use the Python [shelve](https://docs.python.org/3.5/library/shelve.html) library (a very simple database-like mechanism).

## Pub/Sub Interactions

The expected interactions for our service are:

* on startup
    * will open a connection to *pigpio*
    * initialize the hardware with the last saved configuration (if there is no saved configuration, it will initialize the configuration as a side-effect and initialize the lamp to defaults:  Hue of 1.0, Saturation of 1.0, Brightness of 1.0, and lamp On;     * issue a nofication of current lamp settings on `lamp/changed` with the MQTT Retain flag set
* when a `lamp/set_config` is received
    * validate that the requested configuration is valid (e.g., Hue, Saturation, and Brightness are >=0.0 and <=1.0, and the On/Off state is a Boolean); if any values are invalid, the entire configuration change should be ignored;
    * update the hardware to reflect that state, and update the saved configuration
    * issue a nofication of current lamp settings on `lamp/changed` with the
 MQTT Retain flag set


## Assignment Part 2

Create the Lamp service, named `lamp_service.py` that has the above behaviors.  The service should store the state of the lamp in a file `lamp_state.db` stored in the same directory as `lamp_service.py`.  

Create a cron job `/etc/cron.d/2_lampservice` that will start the service, at reboot, running as the `pi` user.

*Note:* you should make sure your Kivy UI application is not running - we do not want it interacting with the hardware at the same time as our service (you might want to remove the cron task)

*Note:* validating input to your service is critical to making it robust; in general you cannot control the origin or correctness of clients making requests of your server

*Note:* your service will need to convert between the color scheme used on-the-wire with MQTT (Hue and Saturation) and with the color scheme used with the hardware (PWM)

*Note:* your service should be built using one or more Python classes; one class should be named *LampService* and should have a method *serve* that starts it up and runs forever; the end of your `lamp_service.py` file should do the `if __name__ == "__main__":` check and if running as main instantiante the *LampService* class and invoke the *serve* method; you should create another class that provides a simple abstraction for the hardware, *LampDriver*, in a file `lamp_driver.py`.

*Note:* you should test the functionality of the service using the command-line tool you built in the previous section.

*Hint:* you might want to incrementally build up the service behavior, testing with the command-line tool; build the simplest configuration possible (e.g., maybe just On/Off), supporting configuration changes (with validation), change notifications, and persistence to the file system, then add the next configuration state variable (e.g., Brightness, then Hue and Saturation); there is no _right_ way to build this, but building it incrementally will increase your odds of success

*Hint:* with your command-line client and now this service, you will begin to have some duplicated data, like MQTT Topic names; you should put those constants into a common file, something like `lamp_common.py`, and import those constants into the Python file namespaces that require them using the `from lamp_common import *` syntax.

Next up: go to [Updating Kivy UI](../03.7_Updated_Kivy/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
