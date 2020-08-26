# iOS New Project

Now we are going to build a simple app and deploy it to our iOS device. The assignment will require running on real hardware, so as you run through this make sure you can deploy to a iPhone and not just the iPhone Simulator.

### Start a New Project

From the welcome page of Xcode, click **Create a new Xcode project** (or in the menu, **File** > **New** > **Project...**).

Under **iOS** > **Application**, select **Single View Application** and click **Next**.

![](Images/new_project.png)

In the next dialog, set **Project Name** to **Lampi**, **Organization Name** to your name, and **Organization Identifier** to something like **com.[your_name]**. Set **Devices** to **iPhone** and **deselect Unit and UI tests** (unless you want to use them, they are not part of this exercise).

**This Courseware is for Objective-C (an object-oriented variant of C). If you are familiar with Swift and prefer to use it, that is fine, but you are on your own.**

Click **Next**. You will be asked where you want your project to be created. Choose a directory and click **Create**.

![](Images/project_settings.png)

### Build and Run

Now is a good time to verify that we can run our project on a real phone. While this used to require a paid developer account, now it is free (you still need the paid account to submit to the App Store).

Plug in your iPhone to your Mac and unlock your phone if it is protected with a passcode. Up in the toolbar you should see your **build scheme** and **device**:

![](Images/scheme_and_device.png)

The current device is an iPhone Simulator. Click on it, and under the **Devices** section, choose the iPhone you just plugged in:

![](Images/real_device.png)

Click the **Build and run** (play) button (or press ⌘ + R).

You may be asked to enabled **Developer Mode** on the Mac. Click **Enable** and enter your OS password.

If this is your first time connecting your phone to Xcode, you may need to wait a while for "Processing Symbol Files".

You may then be told there are no signing identities to sign your application with:

![](Images/signing.png)

Click **Fix Issue**. 

Finally, the very first build will probably fail with a not-at-all-vague, easy-to-troubleshoot error like this: 

![](Images/security.png)

To resolve this in iOS 9.1 and later, go to your iPhone **Settings** > **General** > **Profile**. You should see an entry with your Apple ID in there. Tap on it and tap **Trust "[apple_id]"**, then tap **Trust**. 

If you are not on the latest version of iOS, try finding the installed app on your phone, tapping it, and tapping **Trust** when prompted.

Go back to Xcode and click **Build and run** again. It should deploy and launch an app with **just a big white screen** on your phone.

Congratulations! You have successfully built and deployed an app to an iOS device (the app does not do anything, yet, but we will work on that now).

Next up: [iOS Interface Builder](../08.3_iOS_Interface_Builder/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
