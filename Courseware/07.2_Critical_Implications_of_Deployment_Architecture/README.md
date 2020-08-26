# Critical Implications of Our New Deployment Architecture

There are two critical things to remember going forward about our new, production-ready deployment configuration:

1. When you make _any_ changes to your Django code (e.g., updating views, templates, etc.), you need to remember to restart uWSGI with something like `sudo supervisorctl restart uwsgi` otherwise your changes will not be "live"
1. When you modify _any_ static files in **Web/lampisite** (e.g, CSS, HTML, or JavaScript files) you need to remember to run `python3 manage.py collectstatic` - otherwise, NGINX will still be serving up older files

# You can still run the Django debug server on a high-numbered port (e.g., 8000) for debug purposes, but all assignments going forward must serve up everything from NGINX from standard port numbers.

Next up: go to [User-Device Association](../07.3_User_Device_Association/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
