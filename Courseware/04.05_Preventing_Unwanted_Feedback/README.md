# Preventing Unwanted Feedback

In Assignment 3 you may have experienced some UI issues caused by feedback.  You may have seen two, somewhat related issues:

* **excessive floating-point precision** leading to "run-away" behavior (dragging a slider initiates a slider run-away - the slider keeps moving after you take your finger off)
* **jittery or shaky or cyclic behavior** (slider keeps jumping back and forth between two values)

## **Excessive Floating-Point Precision** Issue

The **excessive floating-point precision** issue is caused by numeric-roundoff issues and feedback between the Kivy UI and the Lamp Service, and excessive floating-point precision in the lamp state.  We can assume that for Hue, Saturation, or Brightness that users will not be able to distinguish changes smaller than 1%.  Given that, we can make sure our Lamp Service does not store lamp state with greater precision than that, or issue updates with greater precision.  We can use the Python [`round(number[, ndigits])`](https://docs.python.org/3.7/library/functions.html#round) built-in to round floating-point values to  the number of digits specified.

The **Lampi/lamp_service.py** file provided in the solution to Chapter 03 does this for you.

## **Jittery** Issue

The **jittery or shaky or cyclic behavior** (from here on shortened to **jittery**) is caused by delays between publishing messages to **lamp/set_config** and receiving the related notification message on **lamp/changed**.  When the Kivy UI receives a message on **lamp/changed** it immediately updates the UI to reflect the new state; the UI update causes a new state to be requested on **/amp/set_config** because of the Kivy bindings, which results in an another set of messages...

To break this loop, we can introduce a new variable to the lamp state:

* `client` - a string that contains a client name

Here's how `client` will be used:

* when publishing a requested lamp state on **lamp/set_config**, `client` must contain the _name_ of the requesting client

* when receiving a new lamp state on **lamp/changed**, `client` will contain the name of the client that requested the state

Client's can then compare the value of `client` in messages received on the **lamp/changed** topic, and ignore any messages that contain their client name.

This will prevent the **jittery** feedback.


Next up: go to [Introduction to Supervisor](../04.06_Supervisord/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
