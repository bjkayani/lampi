# Updating the Django Model and MQTT Daemon

We will update our "backend":

1. Add the association field to the `Lampi` model, plus some helper methods and functions
1. Modfiy the logic of the **mqtt-daemon** to publish the **unassociated** message

## Update Django Model

We need to modify our `Lampi` database model to hold the association code.

Add a function to **lampi/models.py** to generate the association code (fill in the missing code):

```python
def generate_association_code():
    # put your code here
```

Modify the `Lampi` model in **lampi/models.py** to include a new field `association_code` that is a Django `CharField` with `max_length=32` and `default=generate_association_code`.  The `default` with a Python "callable" (a function in this case, that you defined above) will cause `generate_association_code()` to be called whenever a new instance of `Lampi` is created, so each instance will get a unique, 32 character code.

Add a helper method to the `Lampi` model (there's a convention in Python that method names for "private" methods are prefixed with an underscore `_`):

```python
    def _generate_device_association_topic(self):
        return 'devices/{}/lamp/associated'.format(self.device_id)
```

This will return the MQTT Topic for the particular device the `Lampi` instance represents.

Add two additional methods to the `Lampi` model, and fill in the missing code:

```python
    def publish_unassociated_msg(self):
        # send association MQTT message
        assoc_msg = {}
        # your code goes here

    def associate_and_publish_associated_msg(self,  user):
        # update Lampi instance with new user
        # publish associated message
        assoc_msg = {}
        # your code goes here
```

For simplicity, when publishing MQTT messages from your `Lampi` class in these two methods, use the [Paho Publish Single](https://pypi.python.org/pypi/paho-mqtt/1.1#single) module function (this will take care of creating a client, connecting to the broker, and publishing your message, all in one call).  Be sure to provide all of the parameters required (hostname, port number, etc.).

Test your new `Lampi` class in the Django shell.  You can make up `device_id` values for testing (just make sure they are 12 characters long).

**NOTE:** do not forget to create the new database migrations, and apply those migrations to your database!

## Update MQTT Daemon

Modify **Web/lampisite/lampi/management/commands/mqtt-daemon.py** to invoke the new `publish_unassociated_msg()` method on the `Lampi` object after it is created and saved.

Remember, your **mqtt-daemon** is being manged by **supervisord**, so you might want to stop it, using **supervisorctl** and run it manually for testing and debug.

## Testing the New Backend Functionality

You can test this with your LAMPI, and by creating test devices (to fool the **mqtt-daemon** into creating a new `Lampi` instance and publishing a message, publish a message with **mosquitto_pub** to `$SYS/broker/connection/<SOME MADE UP DEVICE_ID>_broker/state` with a value of `1`).

From time to time, you may need to remove Retained messages from your brokers (EC2 or LAMPI) - you can use the `-r` and `-n` command-line options with `mosquitto_pub`.

You can delete a `Lampi` object from the database like so:

```bash
$ ./manage.py shell
Python 3.5.2 (default, Nov 23 2017, 16:37:01)
[GCC 5.4.0 20160609] on linux
Type "help", "copyright", "credits" or "license" for more information.
(InteractiveConsole)
>>> from lampi.models import Lampi
>>> Lampi.objects.all()
<QuerySet [<Lampi: b827ebba0387: My LAMPI>]>
>>> d = Lampi.objects.get(device_id='b827ebba0387')
>>> d
<Lampi: b827ebba0387: My LAMPI>
>>> d.delete()
(1, {'lampi.Lampi': 1})
>>> Lampi.objects.all()
<QuerySet []>
```

NOTE: Remember that there are a few things going on here (and some are "stateful" that is, they have a peristent record in the database and/or the MQTT broker(s) as retained messages):

* the **mqtt-daemon** is an "edge" detector (it detects _new_ devices being bridged to the Mosquitto Broker by monitoring the `$SYS/broker/connection/+/state` subscription with payloads of `b'1'` (byte arrays with an ASCII value of numeral 1); only _new_, unassociated devices will cause the **unassociated** message to be published
* when the **mqtt-daemon** detects a new (previously unknown) LAMPI device, it creates a new `Lampi` database model instance, and publishes an **unassociated** mesage as a retained message
* when the LAMPI device receives an **unassociated** message, it displays the popup

Testing this set of behaviors will likely require you to periodically remove `Lampi` objects from the database, monitor MQTT messages, publish MQTT messages, and periodically remove retained messages from the broker(s).

You should be able to manually associate a device with a user using the Django shell and fully test the LAMPI and "backend" systems behavior at this point.  All that is remaining is to build a web UI in the next section. 

Next up: go to [Updating Django](../07.7_Updating_Django/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
