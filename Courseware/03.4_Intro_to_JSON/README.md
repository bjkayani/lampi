# Quick Introduction to JSON
We will be using JSON extensively for message representation in the course.

## JavaScript Object Notation
JavaScript Object Notation (JSON) has become a popular way to represent data being passed between applications.  It is human-readable, arguably more so than XML.  It also has relatively simple mappings from common programming data structures, making it a convenient mechanism for serliazing (converting a data structure to a representation that can be transferred "one-the-wire") and deserializing (taking an "on-the-wire" representation and reconstructing an in-memory data structure from it).

You can learn more about [JSON](http://json.org/), but there are two fundamental collection types:

* an ordered list of values (aka "list", "array", "vector")
* a collection of key/value pairs (aka "hash", "dict" or "dictionary", "associative array", etc.) known as an object

Values in these collections can be strings, numbers, boolean True or False, or lists or objects (dicts) (e.g., nested lists or objects).

## Python JSON support
Python has a built-in ```json``` module.

Python has two handy functions for encoding (converting a value to a JSON string) and decoding (converting a JSON string to a Python value):

* _json.dumps()_ - convert the Python value provided to a JSON string
* _json.loads()_ - convert a JSON string to a Python value

Here is an example of "dumping" a Python dictionary to JSON (a Python character string):

```
>>> payload = json.dumps({'red': 100, 'blue':200, 'green':300})
>>> payload
'{"blue": 200, "green": 300, "red": 100}'
```

We can convert that string containing a JSON value back into a Python dictionary:

```
>>> d = json.loads(payload)
>>> d
{u'blue': 200, u'green': 300, u'red': 100}
```

*Note:* if you pass invalid JSON into _json.loads()_, you will cause a Python ValueError exception:

```
>>> json.loads('{"blue": 200, "green": 300, } "red": 100')
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
  File "/usr/lib/python2.7/json/__init__.py", line 326, in loads
    return _default_decoder.decode(s)
  File "/usr/lib/python2.7/json/decoder.py", line 365, in decode
    obj, end = self.raw_decode(s, idx=_w(s, 0).end())
  File "/usr/lib/python2.7/json/decoder.py", line 381, in raw_decode
    obj, end = self.scan_once(s, idx)
ValueError: Expecting property name: line 1 column 28 (char 28)
```

Unlike XML, which has Schemas and other mechanisms to validate conformance of documents (messages) to a require format, JSON is typically used more in the manner of [Postel's Law or Robustness Principle](https://en.wikipedia.org/wiki/Robustness_principle) of well-designed, robust systems.

**Note:** In Python2, character strings and byte arrays were essentially the same thing, leading to poor coding practices (assuming all strings were ASCII encoded and/or bytes).  Python3 makes the difference explicit - strings `str` are Unicode character arrays (with a particular encoding, such as UTF-8) and `bytes` are arrays of bytes (8-bit values).  This is important when sending and receiving messages via MQTT Paho, which send and receives `bytes`, requiring `str` to be encoded to `bytes` when publishing and `bytes` to be decoded to `str` when receiving messages.

### Encoding JSON Strings for use in MQTT Messages

`json.dumps()` returns a `str` - you should encode that `str` in UTF-8 format, resulting in a `bytes` before passing to the Paho `publish()` method, like so:

```python3
msg = {'red': 100, 'blue':200, 'green':300}
c.publish('sometopic',json.dumps(msg).encode('utf-8'))
```

### Decoding JSON from MQTT Messages

When you receive an MQTT Message, you need to decode the payload, which is of type `bytes` to a UTF-8 `str` before passing it to `json.loads`, like so:

```python
json.loads(message.payload.decode('utf-8'))
```

Next up: go to [Creating a Command-Line Tool](../03.5_Create_CommandLine_Tool/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
