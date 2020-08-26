# Assignment

This assignment consists of two parts:  deploying your Django application for production, and implementing User-Device Association

## Part 1 - Production Deployment of Django

Following the model demonstrated in the first part of this chapter, configure your EC2 instance to serve your Django application with NGINX and uWSGI.  NGINX should be configured to serve all static files.  uWSGI should be managed by **supervisord** and be autostarted and autorestarted.  All HTTP content should be served on port 80 by NGINX (e.g., static files, Django, etc.)

## Part 2 - User-Device Association

Following the path outlined in this chapter, finish implementing User-Device Association (that is, add the form, the view, the links, etc.).

## What to turn in

You need to turn in the following:

1. A short (a few sentences) write up from each member of the pair summarizing what they learned completing the assignment, and one thing that surprised them (good, bad, or just surprising).  This should be in **connected-devices/writeup.md** in [Markdown](https://daringfireball.net/projects/markdown/) format.  You can find a template file in **connected-devices/template\_writeup.md**
2. A Git Pull Request
3. A short video demonstrating the required behaviors emailed to the instructor and TA.  The video should be named **[assignment 3]_[LAST_NAME_1]\_[LAST_NAME_2].[video format]**.  So, for this assignment, if your pair's last names are "Smith" and "Jones" and you record a .MOV, you would email a file named ```2_smith_jones.mov``` to the instructor.
4. A live demo at the beginning of the next class - **be prepared!**

Notes:

* All Python code should be formatted to conform to PEP8 standards. See [PEP8 documentation](https://pypi.python.org/pypi/pep8) for more info.
* The video should demonstrate::
    * serving all content from port 80 (default HTTP)
    * NGINX, uWSGI, etc. all autostart - no usage of the Django `runserver` command
    * starting with an **unassociated** LAMPI device:
        * demonstrate the device showing the association code
        * demonstrate the Web UI "Add a LAMPI device" link on the "index" page for a logged-in user
        * enter the association code from the LAMPI touchscreen into the Django "add" form
        * show that the form submission completes and auto redirects to the "index" page
        * show that the LAMPI touchscreen popup disappears to be replaced with the regular LAMPI UI
        * show that the "index" page has a new entry for the recently added device, including a link to the "detail" page for that device
        * show that the "detail" page controls the LAMPI, and the touchscreen controls the "detail" page 
    * show what happens when an invalid association code is entered
* Your in-class demo should involve the same **unassociated** LAMPI to **associated** scenario







&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
