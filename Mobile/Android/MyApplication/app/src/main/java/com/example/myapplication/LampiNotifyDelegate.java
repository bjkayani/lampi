package com.example.myapplication;

public interface LampiNotifyDelegate {
    public void setHS (byte h, byte s);
    public void setB (byte b);
    public void setPower (boolean powered);
}
