# Introduction to Ansible

The IT community has often separated "software development" from "operations" - the former being the team that writes software applications and the latter being the team responsible for running them (e.g., managing servers, databases, network connections, security, etc.).  This has often lead to less than stellar systems and organizational dynamics.  Upon hearing from _operations_ that some new software is not functioning correctly, a _software developer_ might respond "Well, it works fine on my machine."  Upon receiving news from a _software developer_ that a time-critical update to an application is ready to be released, a security person from the _operations_ team might respond, "Well, that's nice - we'll schedule a security audit for next month."  These and other similar scenarios are surprisingly common.  And frustrating.

This has lead to the "DevOps" movement - a combination of _Developer_ and _Operations_ - in the last few years, to highlight the fact that both teams are in this together and need to focus on the overall delivery of high quality, secure, valuable software.

A number of tools have sprung up to address some common DevOps problems, particularly server provisioning and service configuration and deployment.  Here are a few popular tools:

* [Ansible](https://www.ansible.com/)
* [Chef](https://www.chef.io/)
* [Puppet](https://puppet.com/)

## Why A DevOps Tool?

Why do we need a DevOps tool for this course?  For a typical programming course ("Intro to Language X" or "Data Structures"), the instructor provides a solution set of source code for the previous assignment.  Unlike a typical programming course, though, this class builds upon everything we have built in the course to date.  Given that we are working with real hardware devices (and soon, cloud servers), working source code is not necessarily sufficient to make sure you can continue building this week's solution on top of your solution for last week - there is a huge amount of configuration files, software packages, and other miscellaenous settings that are needed for a functional system.  A DevOps tool can help automate updating your computing environment to a known working solution.

These tools are sometimes described as "configuration management" tools.  While they have different architectures and philosophies (e.g., "push" or "pull"), they all have mechanisms to describe the desired state of a system in a text file (effectively source code), and tools to transform a system to the desired state.

Because the desired state of the remote system is described in text files, these configuration files can be version controlled the same way as the software application source code.  In fact, they are often stored in the same source code repository as the application source code, allowing the application and configuration code to be managed together (e.g., releases, tags, branches, etc.).

## Why Ansible

For this course we have selected Ansible.  Any of the popular DevOps tools could have been chosen.  We chose Ansible because it is relatively straight-forward to set up and create configurations.  It does not require any software to be installed on the computers being managed, just the "control" machine (e.g., your laptop).  All the remote computer being managed requires is an SSH Server and Python.

> *Why is it called Ansible?*

> Michael DeHaan, developer of the framework, named it after the device of the same name in Orson Scott Card's __Ender's Game__, which was used to control a large number of remote space ships at incredible distances.  Ursula K. LeGuin originally invented the word "Ansible" to describe a device capable of instantaneous (faster-than-light) interstellar communication.

## How we will use Ansible

This course is not a DevOps course.  You will not be expected to develop your own Ansible configurations.  Each week, though, the solution to the previous week's assignment will generally include updated Ansible files to update your device (and eventually, your cloud servers) to a known working state consistent with the solution set.

So, you will be expected to run Ansible each week to update your environment.

Feel free to read up on Ansible, including [Getting Started](http://docs.ansible.com/ansible/intro_getting_started.html).

## Terminology

There are essentially two kinds of computers/machines/servers in Ansible

* **Control Machine** - the computer (e.g., your laptop) where you run Ansible commands to update a collection of remotes/servers/hosts
* **Remote Machines** - the computers that you are managing (your Raspberry Pi at this point)

## Installation on Control Machine

Let's start by getting Ansible installed on your computer (Control Machine).

### Linux

Use your package manager to [install Ansible on Linux](http://docs.ansible.com/ansible/intro_installation.html#installing-the-control-machine)

> **COMPATIBILTY**:  Verify that your package manager installs an up-to-date version of Ansible (>= 2.2).  If it installed an older version, uninstall it and install using [Python Pip](http://docs.ansible.com/ansible/intro_installation.html#latest-releases-via-pip). 

### Mac OS X

Install via [Python Pip](http://docs.ansible.com/ansible/intro_installation.html#latest-releases-via-pip) (no need to install the development version).

### Windows

Unfortunately, while Ansible can be used to manage Windows computers, using Windows as the "Control Machine" is not supported.

If your main computer is Windows, you have a few options:

* run Linux, either as a dual-boot computer or in a Virtual Machine (e.g., VMWare, VirtualBox [free])
* use your Raspberry Pi as the "Control Machine" (and, as a "remote")

To use your Raspberry Pi as the "Control Machine", install like so:

```
sudo apt-get update
sudo apt-get -y install python-pip python-dev sshpass libffi-dev
sudo pip install ansible markupsafe
```

**NOTE:** do not use `apt-get` to install Ansible on the Raspberry Pi - the version in the repositories is too old.

When you configure your ```hosts``` file below, be sure to use "localhost" as the IP address of your Raspberry Pi, not the IP address from DHCP on CaseGuest.

You should be able to run everything normally then.

## Configuring your Ansible Inventory file

In order to manage a group of remote servers ("hosts") you need to list them for Ansible.  There are several ways to do this.  We will use the Ansible ```inventory``` file, an [Inventory File](http://docs.ansible.com/ansible/intro_inventory.html) in Ansible terminology.

In the `connected-devices` git repo, you will find an `ansible` directory.  

### Copy the inventory template file

```bash
cd ansible
cp inventory.ini.template inventory.ini
```

For Ansible's default behavior to work, along with the configuration specified in `ansible/ansible.cfg`, the `inventory` must be in the `ansible` directory and named `inventory.ini`.

The file looks like this:

```
# Ansible Hosts file
# this is the list of hosts (computers) and groups of hosts to be managed
#  by ansible


# replace <IP> with the IP address of your LAMPi
#   (e.g., "ansible_ssh_host=10.0.1.34")
lampi ansible_user=pi ansible_ssh_host=<IP>

# our "group" of LAMPIs, listing the hostname for each on a separate line
[lampis]
lampi
```


The file consists of definitions of hosts (one host per line) and Groups (defined in an INI syntax).

For now, we only have one host defined, ```lampi```.  There are parameters after the host name, including the user name to use and the IP address.

Please replace the ```<IP>``` with the IP address of your LAMPI.

For now, we only have one Group defined, ```lampis``` and it only contains one host ```lampi```.

## Basic "ping" test

Ansible does all of its work with remote systems through SSH.  Let's do the simplest test to verify that Ansible can successfully connect to all (one) of our hosts.

```
ansible all -m ping
```

If it succeeds, you should see output simliar to this:

```
lampi | success >> {
    "changed": false,
    "ping": "pong"
}
```

If you do not see a successful run, stop here and figure out what is wrong.

## YAML, Playbooks, and Roles

Many Ansible files are formatted in [YAML](http://yaml.org/).  Here is an [Ansible YAML Introduction](http://docs.ansible.com/ansible/YAMLSyntax.html).

Ansible allows you to define the desired state of a remote server as a series of "Plays".  Plays are collected into [Playbooks](http://docs.ansible.com/ansible/playbooks_intro.html), which are essentially lists of Plays.  Each Play is "atomic" in that it succeeds or failures.  Plays contain one or more Tasks.  Each Task defines one aspect of the desired state (e.g., installing a software package, modifying a configuration file, etc.)  Plays are executed synchronously and in top-to-bottom order with a Playbook.

It is common for a whole bunch of remote servers to have the same collection of Playbooks applied to them.  Ansible makes this easy by defining "Groups" of remote servers (hosts), and then defining one or more [Roles](http://docs.ansible.com/ansible/playbooks_roles.html).  In a Web application deployment you might have dozens of servers, with varying roles (e.g., FrontEnd, Database, Cache), and some servers may play multiple roles.

For our purposes, we only have one Role at the moment, the role of ```lampi```.

Ansible has some default behaviors and [Best Practices](http://docs.ansible.com/ansible/playbooks_best_practices.html) that can make things easy.  By convention, information (Playbooks, Files, templates, etc.) for Roles are stored in a directory named ```roles``` (in our case ```ansible\roles``` with subdirectories for each role's Playbooks, Files, Variables, etc.).

## Process to Update your LAMPI

Ansible Playbooks are executed with the ```ansible-playbook``` command (instead of the ```ansible``` command that we used earlier to ping our hosts).

Essentially, we are going to update all hosts when we run our Playbook (our hosts file only has one host at this point).  Our top-level Playbook is named ```site.yml```.

From inside the ```ansible``` subdirectory, run

```
ansible-playbook  site.yml
```

This may take a while to run.  It will output messages like this:

```

PLAY [lampis] ******************************************************************

TASK [setup] *******************************************************************
ok: [lampi]

TASK [lampi : update apt cache] ************************************************
changed: [lampi]

TASK [lampi : install git] *****************************************************
ok: [lampi]

TASK [lampi : enable SSH] ******************************************************
ok: [lampi]

TASK [lampi : see if pigpiod already installed] ********************************
ok: [lampi]

TASK [lampi : see if pigpiod already installed] ********************************
ok: [lampi]

TASK [lampi : create directory] ************************************************
changed: [lampi]

TASK [lampi : extract files from archive] **************************************
changed: [lampi]

TASK [lampi : make pigpio] *****************************************************
changed: [lampi]

TASK [lampi : install pigpio] **************************************************
changed: [lampi]

TASK [lampi : create cron entry to run daemon at reboot] ***********************
changed: [lampi]

TASK [lampi : append block to the config.txt] **********************************
changed: [lampi]

TASK [lampi : make sure wifi power management is disabled] *********************
changed: [lampi]

PLAY RECAP *********************************************************************
lampi                      : ok=13   changed=8    unreachable=0    failed=0

```

#### ```ansible-playbook``` output
```
TASK [lampi : enable SSH] ******************************************************
ok: [lampi]
```

Each line that starts with "TASK" lists the name of Role ("lampi") and the Task Name ("enable SSH").  The status of the Task execution on each host is listed on following lines "ok: [hostname]" ("lampi" in this case).

**NOTE:** It is safe to run ```ansible-playbook site.yml``` more than once (it is essentially [idempotent](https://en.wikipedia.org/wiki/Idempotence))

If there are transient errors (e.g., your Raspberry Pi was off), you can run the Playbook again.  If there's a problem with the Playbook (e.g., syntax error), running it again will just fail again, obviously.

## Summary

Each week, before starting the next assignment, update your LAMPI by running the:

```
ansible-playbook site.yml
```

command.  It will automatically perform all of the operations necessary from the previous assignment to make your LAMPI function correctly.

## Starting From Scratch with a Vanilla Raspbian Image

If you are having problems with your LAMPI configuration, you can reimage in a prety fast way by doing the following:

1. Flash your SD card with the steps in [01.1_Burning_an_SD_Card_Image](../01.1_Burning_an_SD_Card_Image/README.md)
2. Connect via the [Serial Port and Configure WiFi](../01.3_Connecting_Serial_and_Wifi_Setup/README.md)
4. Update your ```ansible/inventory.ini``` file if necessary (e.g., if your IP address changed)
5. Run ```ansible-playbook site.yml```

This will take a generic Raspbian image and update it for the current week of the class.

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
