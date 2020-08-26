# Introduction to Supervisor

Most operating systems have a variety of applications (or services) that need to be started up at system start time, shutdown cleanly when the computer is shutdown, and ocassionally restart the service if it fails for some reason.

On Unix style operating systems, like Linux and OS X, there are several systems to help with these tasks.  On Unix, everything starts at boot time with the "init" process.  It is always Process One (PID 1):

```
ps aux | grep init
root         1  0.0  0.1   2148  1308 ?        Ss   Sep19   0:01 init [2]  
```
and runs as root, the super user.  Init starts a sequence of services, which in turn often start other services.  These include interactive logins (TTYs), web servers, SSH servers, and many others.

Traditionally, if you wanted to create a new service, you would also create a script in the ```/etc/init.d``` hierarchy (System V Unix).  You can look at one if you are interested, say ```view /etc/init.d/dbus``` to see how the D-Bus Message Bus Daemon (dbus-daemon) is managed.  These scripts can be a little fiddly to get right, and creating a service that properly *daemonizes* can be a little bit tricky (double forking and such).

```/etc/init.d``` scripts will not go away any time soon, but there are several more modern alternatives, including [systemd](https://www.freedesktop.org/wiki/Software/systemd/) (systemd is the choice of Debian and Ubuntu at this point), launchd, daemontools, runit, and supervisor.  We will be using the latter, supervisor, to make sure our long-running services like the Lamp Service and Kivy are started at boot time and restarted when necessary.  

## Supervisor
(sometimes referred to as Supervisord, pronounced "SupervisorDee")

From the [Supervisor](http://supervisord.org/) site:
> Supervisor is a client/server system that allows its users to monitor and control a number of processes on UNIX-like operating systems.

Explicitly, however, it is not a meant to be a substitute for ```init```, but for more ad hoc process control.

## Installing Supervisor

On your Pi:

```
sudo apt-get install supervisor
```

## Configuring Supervisor

**Supervisord** is the server side of the system - it starts/stops/restarts all of the processes.  **Supervisorctl** is the client, used to control the server.  We will be configuring our applications to run as the ```pi``` user when possible, and as ```root``` when needed.  In general, applications and services should run at the lowest user permissions possible (since the ```pi``` user is a member of the ```sudo``` group, it still has root privileges, which is not ideal, but will illustrate the point).

**Supervisorctl** is the client side of the system.  It connects to the server and allows you to start/stop programs, view their stdout, reload configuration, etc.  We will be running **supervisorctl** as root.

Supervisor is configured by means of text configuration files.  It has many, many options, but the configuration changes we will make will be specific to the [programs](http://supervisord.org/configuration.html#program-x-section-settings) that we want Supervisor to manage. We have to create a ```[program:x]``` configuration section for each program that we want Supervisor to manage.

Supervisor's configuration files are in ```/etc/supervisor/```.  It uses an INI file configuration file format.  The ```supervisord.conf``` file is the top-level configuration, and any files in the ```conf.d``` directory ending in ```.conf``` will automatically be used when Supervisor is updated (via the ```include``` directive you will find if you look at ```/etc/supervisor/```).

### \[program:x\]
Begins a program configuration section, ```[program:x]``` where "x" is the program name, so ```[program:foo]``` for the "foo" program.

### command
**command** specifies the path to the program - this is the bare minimum required for a program configuration.  Your command can include double quotes to group command line arguments to the program, if needed.

### priority
**priority** is an integer value and specifies the ordering of the application relative to others.  Lower value priority programs are started before programs with higher priority values.  Sometimes it is important for services to be started after other services that they depend upon, and vice versa, shutdown before those dependent services are shutdown.

### user
**user** to run the program as.  Supervisor will change to this user (via ```setuid```) before running the program.  **NOTE:** the program will not get a shell or the user's HOME directory or other environment variables by default.

### environment
**environment** is a list of ```key=value``` pairs to be included in the program's environment.

### autostart
**autostart** is a boolean ("true" or "false") that specifies whether the program should be started automatically when supervisord starts.

### autorestart
**autorestart** is a boolean ("true" or "false") that specifies whether the program should be restarted automatically if it exits.

### directory
**directory** is a directory path specifying what directory supervisord should change to before running the command.

### Simple Example

As root, open up a new file ```/etc/supervisor/conf.d/test.conf``` with your editor of choice (e.g., nano or vim).

```
$ sudo nano /etc/supervisor/conf.d/test.conf
```

enter the following, save, and exit:

```
[program:test1]
command=/bin/ls
directory=/var/log
autostart=false
autorestart=false
priority=50
user=pi
```

Start supervisorctl as root:

```
$ sudo supervisorctl
```

try the "help" command:

```
supervisor> help

default commands (type help <topic>):
=====================================
add    clear  fg        open  quit    remove  restart   start   stop  update 
avail  exit   maintail  pid   reload  reread  shutdown  status  tail  version
```

then try the "update" command, which causes supervisord to re-read its configuration, and "avail":

```
test1: added process group
supervisor> avail
test1                            in use    manual    999:999
```

Go ahead and start "test1":

```
supervisor> start test1
test1: ERROR (abnormal termination)
```

Uh oh!  Run the "status" command:

```
supervisor> status
test1                            FATAL      Exited too quickly (process log may have details)
```

Better look at that log - we can use the built-in "tail" command - supervisord automatically logs the stderr and stdout from processes it manages:

```
supervisor> tail test1
apt
aptitude
aptitude.1.gz
auth.log
auth.log.1
auth.log.2.gz
bootstrap.log
btmp
btmp.1
<SNIP>
```

Well, clearly, the "ls" program is not meant to be a long-running program, so it should not be surprising that it exited quickly.  You can see that the stdout for "ls" is the, unsurprisingly, the contents of the "/var/log" directory (Note: you can look at ```stdout``` and ```stderr``` of your program with the "tail" command (try ```help tail``` in supervisorctl).

Next up: go to [Updating Lampi Code](../04.07_Updating_Lampi_Code/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
