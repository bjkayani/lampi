#!/usr/bin/env node
const client_id = "blynk_client";
const hostPort = "1883";

var config = {
    color: {h: 1.0, s: 1.0},
    brightness: 1.0,
    client: client_id,
    on: true
}

var backlight = {
    backlight: 1.0
}

var mqtt = require('mqtt');
var Blynk = require('/usr/local/lib/node_modules/blynk-library');
var client = mqtt.connect("mqtt://localhost", {clientId:client_id, port:hostPort})

var AUTH = 'zbbhNxKXyrykpW3kHX_nOdyw9xrHkBf3';

var blynk = new Blynk.Blynk(AUTH);

var v0 = new blynk.VirtualPin(0);
var v1 = new blynk.VirtualPin(1);
var v2 = new blynk.VirtualPin(2);
var v3 = new blynk.VirtualPin(3);
var v4 = new blynk.VirtualPin(4);

v0.on('write', function(param) {
    if(param == 1){
        config.on = true;
        client.publish("lamp/set_config", JSON.stringify(config), {qos:1});
    }
    else if(param == 0){
        config.on = false;
        client.publish("lamp/set_config", JSON.stringify(config), {qos:1});
    }
    
  });

v1.on('write', function(param) {
    config.color.h = parseFloat(param);
    client.publish("lamp/set_config", JSON.stringify(config), {qos:1});
  });

v2.on('write', function(param) {
    config.color.s = parseFloat(param);
    client.publish("lamp/set_config", JSON.stringify(config), {qos:1});
  });

v3.on('write', function(param) {
    config.brightness = parseFloat(param);
    client.publish("lamp/set_config", JSON.stringify(config), {qos:1});
  });
  
v4.on('write', function(param){
    backlight.backlight = parseFloat(param);
    client.publish("lamp/backlight", JSON.stringify(backlight), {qos:1});
})

client.on('connect', function () {
    console.log("MQTT Connected");
    client.subscribe("lamp/changed");
})

client.on('message',function(topic, message, packet){
    if(topic == "lamp/changed"){
        if(!message.includes(client_id)){
            config = JSON.parse(message);
            config.client = client_id;
            v1.write(config.color.h);
            v2.write(config.color.s);
            v3.write(config.brightness);
            if(config.on){
                v0.write(1);
            }
            else if(!config.on){
                v0.write(0);
            }
        }
    }
});

blynk.on('connect', function() { console.log("Blynk ready."); });
blynk.on('disconnect', function() { console.log("DISCONNECT"); });