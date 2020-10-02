package com.example.myapplication;

public interface LampMQTTDelegate
{
    void receiveState(boolean isOn, double h, double s, double brightness);
}
