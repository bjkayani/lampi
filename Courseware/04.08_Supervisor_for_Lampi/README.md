# Supervisor for Lampi

## Assignment Part 2

As you probably experienced, ```cron``` is useful, but not the best tool for running the kind of services we are creating (it was just a temporary crutch).  Also, ```cron``` will not automatically restart our services if they fail and exit for some reason.

We will use Supervisor to start/restart our two long-running applications:

* `lamp_service.py` - the hardware daemon
* `main.py` - the Kivy UI client

as well as our two infrastructure programs:

* `fbcp` - the frame buffer copier program
* `pigpiod` - the Pi GPIO Dameon

> **COMPATIBILITY:** - the ```pigpiod``` application automatically daemonizes itself; Supervisor expects the applications it is managing to _not_ daemonize but to stay connected to the terminal process.  Newer versions of the ```pigpio``` library have support for the ```pigpiod``` application to stay in the foreground.  To make sure you have a recent enough version, run ```pigpiod -v``` on your Raspberry Pi and make sure the version number it outputs is >= ```60```.

## Supervisor Configuration

Create four (4) Supervisor configuration files in `/etc/supervisor/conf.d`:

1. ```fbcp.conf``` - for the fbcp program
1. ```pigpiod.conf``` - for the PI GPIO daemon
1. ```lamp_service.conf``` - for the hardware service
1. ```lamp_ui.conf``` - for the Kivy app


The files should be only readable and writable by root (hint: look at the `chmod` command).

Configure ```priority``` in each file so the applications start in the order above, from top to bottom.

The two infrastructure programs, `fbcp` and `pigpiod`, should run as the root user.

_Note:_ `pigpiod` will run as a daemon by default.  Generally, that's a good thing.  For managing it with Supervisor, though, that creates problems.  So, when you create your Supervisor configuration for `pigpiod` use the ```-g``` command-line switch to keep it running in the foreground.  Also, `pigpiod` listens for local and remote connections by default.  Since we do not need any remote connections, and allowing remote GPIO control is a security risk, please use the ```-l``` command-line switch to disable remote connections.

The two LAMPI specific applications should run as the ```pi``` user _and_ use the ```pi``` user's Kivy configuration file (hint: you will need to set an environment variable).

The Supervisor name for each ```[program:x]``` should be the base name of the configuration file (e.g., ```[program:lamp_service]``` for ```lamp_service.conf```


All applications should be automatically started upon system start, and automatically restarted if they stop.

*Note:* while you would not normally deploy production code from your home directory (for a variety of security and other reasons), it will be fine for this course.

*Note:* at various points during development you may want disable Supervisor's execution of `lamp_service.py` and/or `main.py` as it might interfere with you are doing.  Depending on your needs, you might rename the file from `lampi.conf` to something like `lampi.conf.disabled`, or use `supervisorctl` and `stop` the unneeded services.

*Note:* Please undo/remove any ```cron``` configurations (`/etc/cron.d`) at this point in favor of Supervisor.

*Note:* remove the `test.conf` file we created. 

Next up: go to [MQTT Bridging](../04.09_Mqtt_Bridging/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
