package com.example.myapplication;
import android.app.Activity;
import android.bluetooth.le.BluetoothLeScanner;
import java.util.*;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.bluetooth.*;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.*;
import android.util.Log;

public class BLEDriver
{
    private BluetoothAdapter adaptor = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner scanner = adaptor.getBluetoothLeScanner();

    private final String serviceUUID = "0001A7D3-D8A4-4FEA-8174-1736E808C066";
    private final UUID powerUUID = UUID.fromString("0004A7D3-D8A4-4FEA-8174-1736E808C066");
    private final UUID hsvUUID = UUID.fromString("0002A7D3-D8A4-4FEA-8174-1736E808C066");
    private final UUID brightnessUUID = UUID.fromString("0003A7D3-D8A4-4FEA-8174-1736E808C066");

    private BluetoothGattCharacteristic power;
    private BluetoothGattCharacteristic hsv;
    private BluetoothGattCharacteristic brightness;

    private BluetoothDevice currentDevice = null;
    private BluetoothGatt mygatt = null;
    private LampiCallBack callBack;

    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    public static BLEDriver instance;

    public static void makeInstance(Activity activity)
    {
        if(instance == null)
        {
            instance = new BLEDriver(activity);
        }
    }

    private BLEDriver(Activity activity)
    {
        //Everything breaks if you don't have this.
        int permissionCheck = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    public void startBrowsing(LampDiscoveryDelegate delegate)
    {
        disconnect();
        ParcelUuid serviceId = new ParcelUuid(UUID.fromString(serviceUUID));
        ScanFilter serviceFilter = new ScanFilter.Builder().setServiceUuid(serviceId).build();
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(serviceFilter);
        //scanner.startScan(new BrowserStartCallBack(delegate));
        scanner.startScan(filters, new ScanSettings.Builder().build(), new BrowserStartCallBack(delegate));
    }

    public void stopBrowsing()
    {
        scanner.stopScan(new BrowserStopCallBack());
        //devices = new ArrayList<BluetoothDevice>();
    }

    public void connect(String mac, Context context, LampiNotifyDelegate delegate)
    {
        stopBrowsing();
        BluetoothDevice device = matchDeviceMac(mac);
        currentDevice = device;
        callBack = new LampiCallBack(delegate);
        device.connectGatt(context, true, callBack);
    }

    public void disconnect()
    {
        if(mygatt != null && currentDevice != null)
        {
            mygatt.close();
            mygatt = null;
            currentDevice = null;
            power = null;
            hsv = null;
            brightness = null;
        }
    }

    private BluetoothDevice matchDeviceMac(String mac)
    {
        for(BluetoothDevice device : devices)
        {
            if(device.getAddress().equals(mac))
            {
                return device;
            }
        }
        return null;
    }

    private boolean isDeviceNew(String mac)
    {
        for(BluetoothDevice device : devices)
        {
            if(device.getAddress().equals(mac))
            {
                return false;
            }
        }
        return true;
    }

    public void writePower(boolean isOn)
    {
        if(mygatt != null && power != null)
        {
            Log.d("BLE", "Writing power");
            WriteRequest req = new WriteRequest();
            req.characteristic = power;
            if(isOn)
            {
                req.data = new byte[]{0x01};

            }
            else
            {
                req.data = new byte[]{0x00};
            }
            callBack.writeRequest(req);
        }

    }

    public void writeHSV(byte h, byte s)
    {
        if(mygatt != null && hsv != null)
        {
            Log.d("BLE", "Writing hs");
            WriteRequest req = new WriteRequest();
            req.characteristic = hsv;
            req.data = new byte[]{h, s, (byte) 0xFF};
            callBack.writeRequest(req);
        }
    }

    public void writeBrightness(byte brightnessVal)
    {
        if(mygatt != null && brightness != null)
        {
            Log.d("BLE", "Writing b");
            WriteRequest req = new WriteRequest();
            req.characteristic = brightness;
            req.data = new byte[]{brightnessVal};
            callBack.writeRequest(req);
        }
    }

    class BrowserStartCallBack extends ScanCallback
    {
        LampDiscoveryDelegate delegate;
        public BrowserStartCallBack(LampDiscoveryDelegate delegate)
        {
            this.delegate = delegate;
        }

        public void onScanResult (int callbackType,
                                  ScanResult result)
        {
            BluetoothDevice discovered = result.getDevice();
            //Log.d("BLEDriver","Found Device");
            //result.
            if (discovered != null && isDeviceNew(discovered.getAddress()))
            {
                devices.add(discovered);
                //discovered.
                if (discovered.getName() != null)
                {
                    delegate.discoveredLamp(discovered.getName() + " (" + discovered.getAddress() + ")");
                }
            }
        }
    }

    class BrowserStopCallBack extends ScanCallback
    {
    }

    class WriteRequest
    {
        BluetoothGattCharacteristic characteristic;
        byte[] data;
    }

    class LampiCallBack extends BluetoothGattCallback
    {
        private LampiNotifyDelegate delegate;

        private List<BluetoothGattCharacteristic> readQueue = new LinkedList<BluetoothGattCharacteristic>();
        private List<BluetoothGattCharacteristic> notifyQueue = new LinkedList<BluetoothGattCharacteristic>();

        private List<WriteRequest> writeQueue = new LinkedList<WriteRequest>();

        private boolean isWriting = false;

        public LampiCallBack(LampiNotifyDelegate delegate)
        {
            this.delegate = delegate;
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("BLE", "State change " + newState);
            if(newState == BluetoothProfile.STATE_CONNECTED)
            {
                gatt.discoverServices();
                mygatt = gatt;
                Log.d("BLE", "Connected");
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            super.onServicesDiscovered(gatt, status);

            BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
            Log.d("BLE", "Services discoverred");
            Log.d("BLE gatt", service.getUuid().toString());
            if(service.getUuid().equals(UUID.fromString(serviceUUID)))
            {
                List<BluetoothGattCharacteristic> chars = service.getCharacteristics();
                Log.d("BLE gatt", "Characteristics: " + chars.size());
                power = service.getCharacteristic(powerUUID);
                hsv = service.getCharacteristic(hsvUUID);
                brightness = service.getCharacteristic(brightnessUUID);

                //gatt.readDescriptor();
                //notifyQueue.add()
                gatt.setCharacteristicNotification(power, true);
                gatt.setCharacteristicNotification(hsv, true);
                gatt.setCharacteristicNotification(brightness, true);

                //setNotify(power, gatt);
                //setNotify(hsv, gatt);
                //setNotify(brightness, gatt);
                notifyQueue.add(power);
                notifyQueue.add(hsv);
                notifyQueue.add(brightness);

                Log.d("BLE", "Setting notifications to true");

                //Set delegate values
                //gatt.readCharacteristic(power);
                //gatt.readCharacteristic(hsv);
                //gatt.readCharacteristic(brightness);

                readQueue.add(power);
                readQueue.add(hsv);
                readQueue.add(brightness);

                //This calls the read queue when it's done.
                setFromNotifyQueue(gatt);
                //readQueuedChars(gatt);
                //readPower();
                //readhsv();
                //readbrightness();
            }
        }

        private void readQueuedChars(BluetoothGatt gatt)
        {
            if(readQueue.size() > 0)
            {
                gatt.readCharacteristic(readQueue.get(0));
                readQueue.remove(0);
            }
        }

        public void writeRequest(WriteRequest req)
        {
            if(isWriting)
            {
                Log.d("BLE", "Adding req to queue");
                writeQueue.add(req);
            }
            else
            {
                Log.d("BLE", "Writing Request");
                req.characteristic.setValue(req.data);
                mygatt.writeCharacteristic(req.characteristic);
                //isWriting = true;
            }
        }

        public void writeQueuedChars()
        {
            if(writeQueue.size() > 0)
            {
                WriteRequest req = writeQueue.get(0);
                req.characteristic.setValue(req.data);
                mygatt.writeCharacteristic(req.characteristic);
                readQueue.remove(0);
            }
            else
            {
                isWriting = false;
            }
        }

        public void setFromNotifyQueue(BluetoothGatt gatt)
        {
            if(notifyQueue.size() > 0)
            {
                BluetoothGattCharacteristic characteristic = notifyQueue.get(0);
                BluetoothGattDescriptor desc = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                if(desc != null)
                {
                    Log.d("BLEDriver","Setting notify");
                    desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(desc);
                }
                else
                {
                    Log.d("BLE", "No notify");
                }
                notifyQueue.remove(0);
            }

            else
            {
                Log.d("BLE", "Starting to read values");
                readQueuedChars(gatt);
            }
        }

        private void setNotify(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
        {
            BluetoothGattDescriptor desc = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
            if(desc != null)
            {
                Log.d("BLEDriver","Setting notify");
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(desc);
            }
            else
            {
                Log.d("BLE", "No notify");
            }
        }

        public void onCharacteristicRead (BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status)
        {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("BLE", "Reading Characteristic " + characteristic.getUuid().toString());
            UUID current = characteristic.getUuid();

            if(current.equals(powerUUID))
            {
                Log.d("BLE", "Reading Power");
                readPower();
            }

            if(current.equals(hsvUUID))
            {
                Log.d("BLE", "Reading Color");
                readhsv();
            }

            if(current.equals(brightnessUUID))
            {
                Log.d("BLE", "Reading Brightness");
                readbrightness();
            }

            readQueuedChars(gatt);
        }

        public void onDescriptorWrite (BluetoothGatt gatt,
                                       BluetoothGattDescriptor descriptor,
                                       int status)
        {
            super.onDescriptorWrite(gatt, descriptor, status);

            Log.d("BLE", "Wrote a notify descriptor");
            setFromNotifyQueue(gatt);
        }

        public void onCharacteristicWrite (BluetoothGatt gatt,
                                                         BluetoothGattCharacteristic characteristic,
                                                         int status)
        {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("BLE", "Completed request");
            writeQueuedChars();
        }

        public void onCharacteristicChanged (BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic)
        {
            super.onCharacteristicChanged(gatt, characteristic);

            characteristic.getValue();
            UUID current = characteristic.getUuid();

            if(current.equals(powerUUID))
            {
                Log.d("BLE", "Reading Power");
                readPower();
            }

            if(current.equals(hsvUUID))
            {
                Log.d("BLE", "Reading Color");
                readhsv();
            }

            if(current.equals(brightnessUUID))
            {
                Log.d("BLE", "Reading Brightness");
                readbrightness();
            }
        }

        public void readPower()
        {
            boolean isNull = power == null;
            Log.d("BLE", "Power null " + isNull);
            byte val = power.getValue()[0];
            if(val == 0x00)
            {
                delegate.setPower(false);
            }
            else
            {
                delegate.setPower(true);
            }
        }

        public void readhsv()
        {
            byte[] val = hsv.getValue();
            Log.d("BLE", "HS: " + val[0] + " " + val[1]);
            delegate.setHS(val[0], val[1]);
        }

        public void readbrightness()
        {
            byte[] val = brightness.getValue();
            Log.d("BLE", "Brightness: " + val[0]);
            delegate.setB(val[0]);
        }
    }

}


