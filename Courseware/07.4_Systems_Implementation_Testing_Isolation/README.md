## Systems-Wide Implementation, Testing, and Isolation

User-Device Association is a systems-wide feature, necessitating updates to everything from the device (Kivy UI, MQTT Broker Bridge) to the Web/Cloud (**mqtt-daemon**, Django models and database, Django views, etc.)

Developing a methodical plan to incrementally develop and test a system-wide feature is critical to success (and your sanity) - there are many places to make mistakes.  Similarly, having the ability to isolate the subsystems and test them individually will be important.  "Seams" in the system, interfaces where components touch, are often useful places to isolate, test, and debug (for an excellent introduction to the concept of Seams, see [Testing Effectively with Legacy Code](http://www.informit.com/articles/article.aspx?p=359417&seqNum=3) or read the full book that exceprt comes from by the same author, Michael Feathers, [Working Effectivley with Legacy Code](https://www.amazon.com/Working-Effectively-Legacy-Michael-Feathers/dp/0131177052); it is excellent).

### Our Plan

Here's the suggested order for development for this assignment:

1. Update LAMPI - Kivy UI (full implementation provided in the assignment)
1. Update the MQTT Bridge Topic Mapping Configuration
2. Update the Django `Lampi` Model 
2. Update the **mqtt-daemon**
3. Update the Django application (portions of the solution provided in the assignmet)


Next up: go to [Updating LAMPI](../07.5_Updating_LAMPI/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
