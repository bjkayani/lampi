# Assignment

Working through this Chapter you should have completely ported our simplistic and hard-coded static HTML/JS/CSS/WS solution from the previous Chapter to a more flexible website with authentication and a database to manage User/LAMPI associations.  The site can support an aribtrary number of Users and LAMPIs.

# What to turn in

You need to turn in the following:

1. A short (a few sentences) write up from each member of the pair summarizing what they learned completing the assignment, and one thing that surprised them (good, bad, or just surprising).  This should be in **connected-devices/writeup.md** in [Markdown](https://daringfireball.net/projects/markdown/) format.  You can find a template file in **connected-devices/template\_writeup.md**
2. A Git Pull Request
3. A short video demonstrating the required behaviors emailed to the instructor and TA.  The video should be named **[assignment 3]_[LAST_NAME_1]\_[LAST_NAME_2].[video format]**.  So, for this assignment, if your pair's last names are "Smith" and "Jones" and you record a .MOV, you would email a file named ```2_smith_jones.mov``` to the instructor.
4. A live demo at the beginning of the next class - **be prepared!**

Notes:

* All Python code should be formatted to conform to PEP8 standards. See [PEP8 documentation](https://pypi.python.org/pypi/pep8) for more info.
* The video should demonstrate::
    * Login and Logout
    * Website with a regular user
    * Website with a superuser
    * "/" with a User logged in that has 0 LAMPIs
    * "/" with a User logged in that has 1 LAMPIs
    * "/" with a User logged in that has 2 (or more)+ LAMPIs
    * Demonstrate bi-directional (Web UI and Kivy) control for **2 LAMPis** (either in parallel with two browser tabs or sequentially)
* Your in-class demo should involve 2 LAMPIs connected to the same EC2 instance.

**NOTE:** if you are not in a group (solo) on this project, ask a classmate to let you borrow their LAMPI for an hour (switch it over to use your EC2 MQTT broker; record the video; switch it back to their EC2 MQTT broker).  You will not have to demonstrate two devices in class, then, just one.


&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
