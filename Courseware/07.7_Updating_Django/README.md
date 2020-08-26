# Updating Django 

To complete User-Device Association we need a mechanism for a user to provide the association code (or, specifically, the first 6 digits of one).  We will assume that the user already has a user account in the LAMPI system, and that we will only allow a logged-in user to associate a device with their account.

## Django Forms

The obvious choice for this functionality is an HTML form.  Django has sophisticated support for forms, including form validation (e.g., check all of the values entered to verify that they conform to the expected data types and/or any other business rules you might need).

Please read up on [Django Forms](https://docs.djangoproject.com/en/2.1/topics/forms/).

### Updating our Django Application - Adding the Form

A complete Django Form subclass has been provided for you in **Web/lampisite/lampi/forms.py** named `AddLampiForm`:

```python
from django import forms
from django.contrib.auth import get_user_model
from django.core.exceptions import ValidationError
from django.conf import settings
from .models import Lampi


def device_association_topic(device_id):
    return 'devices/{}/lamp/associated'.format(device_id)


class AddLampiForm(forms.Form):
    association_code = forms.CharField(label="Association Code", max_length=6)

    def clean(self):
        cleaned_data = super(AddLampiForm, self).clean()
        print("received form with code {}".format(
              cleaned_data['association_code']))
        # look up device with start of association_code
        uname = settings.DEFAULT_USER
        parked_user = get_user_model().objects.get(username=uname)
        devices = Lampi.objects.filter(
            user=parked_user,
            association_code__startswith=cleaned_data['association_code'])
        if not devices:
            self.add_error('association_code',
                           ValidationError("Invalid Association Code",
                                           code='invalid'))
        else:
            cleaned_data['device'] = devices[0]
        return cleaned_data
```

You can review how [Django Form Validation and "cleaning" happens](https://docs.djangoproject.com/en/2.1/ref/forms/validation/), but essentially our `clean()` method above calls the `clean()` method of the super (parent) class and verifies that there is an unassociated device with an association code that starts with the value provided; if there is such a device, a reference to the `Lampi` instance for the device is stored in the `cleaned_data` dictionary, and returned.  The value is then available to the Django view using `AddLampiForm`.  If there is no such device, we add a `ValidationError` with some hopefully helpful information, that will eventually work its way up to the web UI.

### Updating our Django Application - Adding the View

We need to add a Django View to **Web/lampisite/lampi/views.py** to use our form subclass:

```python
class AddLampiView(LoginRequiredMixin, generic.FormView):
    template_name = 'lampi/addlampi.html'
    form_class = AddLampiForm
    success_url = '/lampi'

    def form_valid(self, form):
        device = form.cleaned_data['device']
        # do something here...
        #   you have the device object
        #   the logged in user object is available in self.request.user
        return super(AddLampiView, self).form_valid(form)
```

you need to fill in the missing code.

The form class is specified in the `form_class` attribute.  

When the form submission completes successfully, the browser will be redirected to the URL specified in `success_url`.

The `form_valid` method of the `generic.FormView` is called if the form's validation and cleaning completes successfully.  The form instance is passed into the `form_valid` method, which has the `cleaned_data` dictionary attribute (including the `'device'` key that holds a reference to the `Lampi` device object).  Your code should do what is needed complete the "add" process, before the `return super(AddLampiView, self).form_valid(form)` call to the super (parent) class's `form_valid` method.

You also need to create the Django template in **Web/lampisite/lampi/template/lampi/addlampi.html** like this:

```
{% extends "lampi/base.html" %}

{% block title %}Add LAMPI{% endblock %}

{% block content %}
<form action="" method="post">
    {% csrf_token %}
    {{ form.as_p }}
    <input type="submit" value="Add LAMPI" />
</form>
{% endblock %}
```

### Updating our Django Application - Adding a URL path

Now that we have a new view, with a form and a template, we need to update our `urlpatterns` in **Web/lampisite/lampi/urls.py**.  Add a new `path`, with a route of `'add/'`, the `AddLampiView` as the view, and a name off `'add'`.

### Updating our Django Application - Updating `index`

We need some easy way to get to our new "Add Lampi" form.  The natural place for this is in the **Web/lampisite/lampi/template/lampi/index.html**.  Add links (`<a>` anchor tags) to **Web/lampisite/lampi/template/lampi/index.html** so that a user with no LAMPIs, or a user that already has one or more LAMPIs, sees an "Add a LAMPI Device" link that takes them to the "add" view.

## Testing

Test that a user can log into the site and use the 'Add a LAMPI Device' link to access a new Django View, with a Django Form, that accepts the 6-digit code.  The form code does a database query for any 'Lampi's owned by the `parked_device_user` that have an association code that starts with the 6-digits provided.  Once the LAMPI device has been added, the device should appear in the user's list of devices, and they should be able to follow that link and control their lamp remotely.  Try entering an invalid association code.

Verify the end-to-end behavior (from LAMPI to the Django site).  Remember that portions of the system are stateful (e.g., the database and MQTT retained messages), and the notes in previous sections on testing and debugging, in that you might need to "reset" some state for testing.

Next up: go to [Assignment](../07.8_Assignment/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
