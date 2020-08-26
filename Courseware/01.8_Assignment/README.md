# Assignment

## LED Control via Python

Write a Python script using one or more loops to produce the following effects, in order:

1. Turn off all LEDs
1. Delay 1 second
1. Over a period of 1 second, smoothly ramp the Red light from 0% to 100% intensity, and then back to 0% (i.e., half second ramp up, half second ramp down). 
1. Over a period of 1 second, smoothly ramp the Green light from 0% to 100% intensity, and then back to 0% (i.e., half second ramp up, half second ramp down). 
1. Over a period of 1 second, smoothly ramp the Blue light from 0% to 100% intensity, and then back to 0% (i.e., half second ramp up, half second ramp down). 
1. Over a period of 1 second, smoothly ramp White light from 0% to 100% intensity, and then back to 0% (i.e., half second ramp up, half second ramp down). 
1. Return to Step #1 (loop forever)

## GPIO Assignments

* The Blue light color channel is controlled by GPIO13
* The Red light color channel is controlled by GPIO19
* The Green light color channel is controlled by GPIO26

## Lab Demonstration

Please be prepared to demo your system at the very start of the next class (you should arrive a few minutes early to make sure your system boots up and is working properly).

## What to turn in

Before the start of class next week, one member of each pair should send an email to the instructor with the following:

* Subject:  Assignment 1
* Names of the members of pair
* A short (a few sentences) write up from each member of the pair summarizing what they learned completing the assignment, and one thing that surprised them (good, bad, or just surprising).
* How many hours total you and your partner spent on this week's assignment, rounded to the nearest hour.
* Attach the file containing the required Python script
* Attach a short (5-10 second) video demonstrating the required lamp behaviors.


## Notes

* The following *pigpio* functions might be useful:
  * [_set_PWM_dutycycle()_](http://abyz.me.uk/rpi/pigpio/python.html#set_PWM_dutycycle)
  * [_set_PWM_range()_](http://abyz.me.uk/rpi/pigpio/python.html#set_PWM_range)
  * [_set_PWM_frequency()_](http://abyz.me.uk/rpi/pigpio/python.html#set_PWM_frequency)
* The following Python *time* function might be useful:
  * [_sleep()_](https://docs.python.org/3.5/library/time.html#time.sleep)
* The Python [_range()_](https://docs.python.org/3.5/library/functions.html#func-range) built-in function might be useful.
* Please have everything you need setup and ready to demonstrate before class starts

Other documentation:

* [https://wiki.python.org/moin/ForLoop](https://wiki.python.org/moin/ForLoop)

Next up: go to [Introduction to Ansible](../01.8_Ansible_Introduction/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
