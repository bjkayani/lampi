package com.example.myapplication;
import org.eclipse.paho.client.mqttv3.*;
import android.util.Log;

import com.google.gson.*;

import java.util.UUID;

public class MosquittoDriver
{


    public static MosquittoDriver instance = new MosquittoDriver();
    private MqttAsyncClient client;
    private String device;
    private final String clientName = UUID.randomUUID().toString() + " Android";
    private LampMQTTDelegate delegate;


    private MosquittoDriver()
    {
        try
        {
            Log.d("Mqtt", "Connecting to server");
            client = new MqttAsyncClient("tcp://iot.eclipse.org:1883", MqttAsyncClient.generateClientId());
            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(true);
            client.setCallback(new LampiCallback());
            client.connect(opts);
        }
        catch (Exception e)
        {
            Log.d("mqtt", "Conncetion error");
            //"+me.getReasonCode());e.getReasonCode()
            Log.d("mqtt", e.getMessage() + " ");
            Log.d("mqtt", e.getLocalizedMessage());
            //Log.d("mqtt", e.getCause().toString());


        }
    }

    public void setCurrentDevice(String deviceName)
    {
        try
        {
            Log.d("Mqtt", "Subsribing device");
            client.subscribe("devices/" + deviceName + "/lamp/changed", 1);
            client.unsubscribe("devices/" + device + "/lamp/changed");
            this.device = deviceName;

        }
        catch (Exception e)
        {
            Log.d("Mqtt", "Error Subsribing device");
            this.device = deviceName;
        }
    }

    public void setDelegate(LampMQTTDelegate delegate)
    {
        this.delegate = delegate;
    }

    public void publishState(boolean isOn, double h, double s, double brightness)
    {
        if(client.isConnected())
        {
            Log.d("Mqtt", "Publishing state");

            JsonObject newState = new JsonObject();
            newState.addProperty("client", clientName);
            newState.addProperty("brightness", brightness);
            newState.addProperty("on", isOn);

            JsonObject newColor = new JsonObject();
            newColor.addProperty("h", h);
            newColor.addProperty("s", s);

            newState.add("color", newColor);

            Log.d("Mqtt", newState.toString());

            MqttMessage stateMsg = new MqttMessage(newState.toString().getBytes());

            try {
                client.publish("devices/" + device + "/lamp/set_config", stateMsg);
            } catch (Exception e) {
                Log.d("Mosqutto", "could not send message");
            }
        }
        else
        {
            Log.d("Mosquitto", "Client disconnected");
        }
    }


    class LampiCallback implements MqttCallback
    {

        public void connectionLost(Throwable cause)
        {

        }

        public void deliveryComplete(IMqttDeliveryToken token)
        {

        }

        public void messageArrived(String topic, MqttMessage message)
        {

            try {
                Log.d("Mqtt", "Receiving state");
                String jsonString = message.toString();
                JsonElement parsed = new JsonParser().parse(jsonString);
                JsonObject newState = parsed.getAsJsonObject();

                Log.d("Mqtt", "Converted to JSON obj");
                if(!newState.getAsJsonObject("client").getAsString().equals(clientName)) {

                    JsonObject color = newState.getAsJsonObject("color");
                    JsonObject hue = color.getAsJsonObject("h");
                    JsonObject sat = color.getAsJsonObject("s");
                    double h = hue.getAsDouble();
                    double s = sat.getAsDouble();

                    JsonObject brightness = newState.getAsJsonObject("brightness");
                    double b = brightness.getAsDouble();

                    JsonObject on = newState.getAsJsonObject("on");
                    boolean isOn = on.getAsBoolean();

                    if (delegate != null) {
                        Log.d("Mqtt", "Parsed full json obj");
                        delegate.receiveState(isOn, h, s, b);
                    }
                }
            }
            catch (Exception e)
            {
                Log.d("Json", "parsing error");
            }

            //{"color": {"h": 0.25, "s": 0.74}, "on": true, "client": "lamp_ui", "brightness": 1.0}
        }
    }

}
