# Connecting Serial and WiFi Setup

## Inserting the SD Card into the Pi

Your SD Card will go into the SD slot in the BACK of the Pi. Make sure to insert the card in the proper orientation.
 ![Inserting SD Card](Images/Insert_SD_Card_into_Pi.png)


## Connecting the Pi

**NOTE:** you will need to get the serial connection working eventually, but if you are having troubhle with the following, skip ahead to the WiFi section and use the `/boot/wpa_supplicant.conf` trick to get LAMPI on the WiFi network.

7. Carefully connect the Micro-USB Cable to the Micro-USB connector on the board, matching the cable orientation to match the connector.
<br/>**BE VERY, VERY CAREFUL WITH THE MICRO-USB CONNECTION - IT HAS MINIMAL STRAIN RELIEF AND CAN BE PULLED OFF THE BOARD IF YOU ARE CARELESS**
<br/>![UART Connection](Images/uart_connection.png)
8.  Plug the power cord into an AC outlet, then plug the power adapter into the LAMPI.  The Pi should have a steady red light on.
9.  Plug the USB part of the Serial Cable into your USB port of your computer.
10. The LAMPI Interface Board has an FTDI Serial-USB chip (an FTDI FT230X) to simplify connecting your computer to the serial port on the Raspberry Pi. Install any needed Virtual COM Port Drivers for the FTDI USB-Serial chip [FTDI VPC Drivers](http://www.ftdichip.com/Drivers/VCP.htm) needed for your OS - **NOTE: if you are using [Serial](https://www.decisivetactics.com/products/serial/) on the Mac, no drivers need to be installed**.
10. Install any necessary terminal emulator software needed for your OS (see [Serial Terminal Basics](https://learn.sparkfun.com/tutorials/terminal-basics/all) which has helpful basic information as well as links to several popular terminal emulator applications for various Operating Systems; note: the baud rate and other settiings in that link are focused on connecting to Arduino devices, which are slower and have a much lower baud rate - use the settings below for the Raspberry Pi).
11. Then in your terminal emulator, connect to your Pi.  The following images are from the [Serial app for OSX](https://www.decisivetactics.com/products/serial/), a non-free application.
![Connected Pi Serial Terminal](Images/Connect_Pi_Serial_Terminal.png)
![Terminal Settings](Images/Terminal_Settings.png)
The Line Settings within Terminal Settings should be:
	* Baud Rate: 115200
	* Data Bits: 8
	* Parity: None
	* Stop: 1
12. Log into your Raspberry Pi:
	* login: ``pi``
	* password: ``raspberry``
13. Run `sudo raspi-config` to perform initial setup.
15. Highlight `1 Change User Password ` and choose a new password.

**Congratulations, your LAMPI is up and running!**

## Now let's connect your Raspberry Pi to WiFi via the Command Line.

**NOTE:** your Raspberry Pi is about to be connected to the network - you _must_ change the password from the default. In several cases, we have seen Raspberry Pis with default passwords become compromised by malicious users within 10-15 minutes of being connected to the internet. When this happens it can be destructive but not immediately obvious.


Some helpful instructions: [https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md](https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md)

**NOTE:** the Linux `sudo` command allows you to execute commands as another user.  By default, that other user is _root_, the superuser.  Executing commands as _root_ can be dangerous - you can, for example, delete every file on the system.  That would probably be bad.  We will require superuser privileges to access certain hardware devices, change system settings, and generally muck about with the Raspian system.

Use sudo carefully.  [Don't let the power go to your head.](https://xkcd.com/149/)

### Configure Your Wireless Network

1. In the terminal, scan for WiFi networks via `sudo iwlist wlan0 scan`. You'll see networks listed. You'll want to find the name of the network and password.
	* The name of the network is from either ssid or ESSID.
2. Add the network details to your Raspberry Pi.  A configuration for connecting to "CaseGuest" and "csuguest" is shown below.
	* Using whatever terminal text editor you prefer ([https://www.raspberrypi.org/documentation/linux/usage/text-editors.md](https://www.raspberrypi.org/documentation/linux/usage/text-editors.md) particularly the *Command-line editors*b section), open the `wpa_supplicant.conf` configuration file. The command will look something like: `sudo vi /etc/wpa_supplicant/wpa_supplicant.conf`.
	* The configuration file will open, add the information for your network below the existing content. 

    ```
    country=US
    ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
    update_config=1


    network={
        ssid="CaseGuest"
        key_mgmt=NONE
    }

    network={
        ssid="csuguest"
        key_mgmt=NONE
    }
    ```
	* Save the updated `wpa_supplicant.conf` file.
3. At this stage, reboot your Raspberry Pi via `sudo reboot` (a reboot is not strictly necessary, but it is the easiest way to ensure all of the network configurations are updated).
4. After the pi reboots, login again via the serial console and test your network connection via `ping www.google.com` to verify that you are connected to the Internet.
5. Test that ssh is working.  Get the IP address of your Pi via `ifconfig` which will have output like

    ```
    nbarendt@nick-raspberrypi:~$ ifconfig
    eth0      Link encap:Ethernet  HWaddr b8:27:eb:2f:9f:38  
              UP BROADCAST MULTICAST  MTU:1500  Metric:1
              RX packets:0 errors:0 dropped:0 overruns:0 frame:0
              TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
              collisions:0 txqueuelen:1000
              RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)

    lo        Link encap:Local Loopback  
              inet addr:127.0.0.1  Mask:255.0.0.0
              UP LOOPBACK RUNNING  MTU:65536  Metric:1
              RX packets:72 errors:0 dropped:0 overruns:0 frame:0
              TX packets:72 errors:0 dropped:0 overruns:0 carrier:0
              collisions:0 txqueuelen:0
              RX bytes:6288 (6.1 KiB)  TX bytes:6288 (6.1 KiB)

    wlan0     Link encap:Ethernet  HWaddr 00:13:ef:30:02:72  
              inet addr:10.0.1.34  Bcast:10.0.1.255  Mask:255.255.255.0
              UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
              RX packets:40 errors:0 dropped:4 overruns:0 frame:0
              TX packets:45 errors:0 dropped:0 overruns:0 carrier:0
              collisions:0 txqueuelen:1000
              RX bytes:6883 (6.7 KiB)  TX bytes:8131 (7.9 KiB)
    ```

    SSH is the "Secure Shell".  Linux and Mac's have an SSH client installed by default.  If you are on Windows, consider [PuTTy](http://www.putty.org/).  
    Fom ssh to your Raspberry Pi's IP address via 

    ```
    ssh -l pi PUT_YOUR_PIS_IP_ADDRESS_HERE
    ```

     or, alternatively, 

    ```
    ssh pi@PUT_YOUR_PIS_IP_ADDRESS_HERE
    ```

### `/boot/wpa_supplicant.conf` trick

Recently releases of Raspbian support a convenient trick to configure WiFi.  You can put an appropriately configured `wpa_supplicant.conf` file in the `/boot` partition on the SD card when it is inserted into your computer and mounted.  At boot time, a helper script will check `/boot` for a `wpa_supplicant.conf` file; if the file is found, it is moved to `/etc/wpa_supplicant/wpa_supplicant.conf` replacing any file that might be there already.  (This is similar to the trick of creating an `ssh` file in `/boot`, which we did previously, to enable the SSH Server).

Here's how to use this trick, if needed:

1. Power off LAMPI / Raspberry Pi
1. Remove SD Card from the Raspberry Pi
1. Insert SD Card into your computer
1. Mount the `/boot` partition, if needed
1. Create a properly formatted `wpa_supplicant.conf` file in `/boot`
1. Unmount `/boot` / Properly Eject the SD card from your computer
1. Insert the SD Card into the Raspberry Pi
1. Power LAMPI / Rasbperry Pi

Next up: go to [Interface LED](../01.4_Interface_LED/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
