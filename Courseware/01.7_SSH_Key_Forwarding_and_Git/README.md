# SSH Key Forwarding

We are going to set up SSH Key Forwarding to simplify working with Git on your LAMPI as well as with AWS EC2 in the future.

SSH Key Forwarding is also necessary for Ansible (the DevOps tool we will use) to work in the future as it will automatically clone and/or update the GitHub repository.

Here is a [short blog post](https://nerderati.com/2011/03/17/simplify-your-life-with-an-ssh-config-file/) related to this that you might find useful.

## Set up Public/Private Key SSH for your Raspberry Pi

If you do not already have an RSA key for login use (separate from GitHub!), please follow these [Debian Instructions](https://www.debian.org/devel/passwordlessssh), and install your *public* key in the `~/.ssh/authorized_keys` file on the Raspberry Pi. The [`ssh-copy-id`](https://www.ssh.com/ssh/copy-id) command is a very convenient tool for correctly copying your _public_ key to the remote machine, in the correct location (e.g., `~/.ssh/authorized_keys`), with the required file and directory permissions (e.g., Unix mode 600).

While passphrases are valuable, that level of security is not required for this course.  If you have more than one *private* key in your ```~/.ssh``` directory you might want to configure the login parameters for your Raspberry PI in your ```~/.ssh/config``` file, like so:

```
Host <YOUR RASPBERRY PI IP ADDRESS GOES HERE>
  IdentityFile ~/.ssh/id_rsa # YOUR PRIVATE KEY FILENAME GOES HERE
  User pi
```

Once you have done that, you can log into your Raspberry Pi by just specifying the IP Address to SSH:

```
ssh <YOUR RASPBERRY PI IP ADDRESS GOES HERE>
```

## Set up Key Forwarding

We'll be doing development work directly from our Raspberry Pi. To do that, we'll need access to GitHub. There are a couple solutions to this. You could use http with a username and password. Annoying because you'll need to type your password in everytime you interact with the server. You could create a new key on the server and trust it in GitHub. This is viable but can be dangerous should your server (or Raspberry PI!) become compromised.

The alternative we're going to set up is to use agent forwarding. This will allow our keys to be forwarded from our host machine (i.e. your laptop) to a remote location over an active SSH session. You'll be able to access GitHub as normal without worrying about leaving trusted keys all over the place.

If you are not already using SSH Keys with GitHub, set that up first by doing the following.  **Even if you are already using SSH Keys with GitHub, please read these sections to make sure you understand the key naming conventions we are using for later sections.  Also, if you are using SSH Keys with GitHub already, you should remove any key(s) you have installed on your LAMPI.**

[Key Forwarding](https://developer.github.com/v3/guides/using-ssh-agent-forwarding/) allows you to keep your private keys safe and secure on your computer, but selectively allow access to them from other computers that you connect to using SSH.  The private key is "forwarded" to the machine you SSH'd to when needed, allowing the private key to be used from that machine, but not stored on it.

We will use Key Forwarding to access GitHub from your Raspberry Pi (and, eventually, from AWS EC2).  Your GitHub private key will reside on your computer, but be available to you when you SSH from your computer to the Raspberry Pi.

### Create a Key for GitHub

First we're going to create a key that we use specifically for GitHub. Using a single key for everything is similar to using a single password -- if it gets compromised, trying to "untrust" the key everywhere you've trusted it becomes difficult to manage.

Run through the [instructions at GitHub](https://help.github.com/articles/generating-ssh-keys/) with one important change: name the key **id\_rsa\_github** as it will be used just for GitHub. **Note that Step 5, testing the connection, will not work yet.**

By the end of the instructions, you should have a private / public key called **id\_rsa\_github** and **id\_rsa\_github.pub**, they should be added to the ssh-agent (using the `ssh-add` command), and also trusted on your GitHub account.

#### A Note for Windows Users

For any Windows users, there are a couple ways to get access to `ssh-keygen` and other Unix-y tools. If you're just starting off I would install [GitHub for Windows](https://windows.github.com/) and use the **Git Bash** shortcut it provides. If you want something more customizable that you can use from other consoles, you can install unix tools to your path in the  [Git for Windows](https://git-for-windows.github.io) installer. You can also use [Cygwin](https://www.cygwin.com) which provides a much more isolated, linux-like environment.

There are also some good alternatives to cmd.exe out there, including [ConEmu](https://conemu.github.io) and [ConsoleZ](https://github.com/cbucher/console).

### Use Key for GitHub

We'll need to specify that we want to use our **id_rsa_github** key for GitHub specifically. Edit `~/.ssh/config` so it contains:

```
Host github.com
    IdentityFile ~/.ssh/id_rsa_github
```

This tells ssh to use our id_rsa_github key specifically when talking to github.com.

Save and close the file, then test using the following command:

```
host$ ssh -T git@github.com
```

You may see something like this:

```
The authenticity of host 'github.com (207.97.227.239)' can't be established.
RSA key fingerprint is 16:27:ac:a5:76:28:2d:36:63:1b:56:4d:eb:df:a6:48.
Are you sure you want to continue connecting (yes/no)?
```

Respond `yes`. If all went well, you'll see:

```
Hi [user]! You've successfully authenticated, but GitHub does not provide shell access.
```

### Enable ForwardAgent for your Raspberry Pi

Now we'll need to enable agent forwarding so we can use our GitHub key while ssh'd into our Raspberry Pi. Once again edit your `~/.ssh/config` and add the following:

```
Host <YOUR RASPBERRY PI IP ADDRESS GOES HERE>
  IdentityFile ~/.ssh/id_rsa # YOUR PRIVATE KEY FILENAME GOES HERE
  ForwardAgent yes
  User pi
```

Now you should be able to just run `ssh [your_lampi_ip_address]` without specifying the key.

### Adding your GitHub Private Key to the Forwarding Agent

You need to add your GitHub Private Key to the SSH Forwarding Agent.  You do this with the `ssh-add` command:

```
ssh-add ~/.ssh/id_rsa_github
```

you can see the list of all keys that the SSH Forwarding Agent will forward with:

```
ssh-add -l
```

**NOTE:** On Mac OS X the SSH Forwarding Agent loses all keys on reboot, so you will need to use `ssh-add` every time you reboot your computer.

### Testing ForwardAgent

ssh into your LAMPI and once again run:

```
ssh -T git@github.com
```

This is pretty cool - your GitHub key is not stored on your LAMPI, but is available when needed from your computer.

You should see a success messsage as before.

# GitHub Repository

We need to clone the Course Git repository from GitHub to LAMPI.  Many software components will require the repository to be located at:

`/home/pi/connected-devices`

so let's go ahead and clone the repository.

## Install Git

```shell
$ sudo apt-get install git
```

## Clone the GitHub Repository

1. SSH to your LAMPI
1. cd to your home directory

    ```shell
    $ cd
    ```

1. Clone the GitHub Repository (substituting the correct git URI and noting the `connected-devices` directory after the URI):

    ```shell
    $ git clone git@github.com:CWRU-Connected-Devices/connected-devices-spring20.git connected-devices
    ```

1. When the clone is complete, you should have a copy of the repository on your LAMPI.

Next up: go to [Assignment 1](../01.8_Assignment/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
