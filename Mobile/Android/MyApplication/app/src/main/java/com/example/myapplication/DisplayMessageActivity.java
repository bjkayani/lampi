package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItem;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DisplayMessageActivity extends AppCompatActivity implements LampDiscoveryDelegate {

    public void discoveredLamp(String deviceName){
        if (bleDevices.contains(deviceName)) return;
        if (deviceName == null) return;
        if (bleDevices.contains("Not Connected")) {
            bleDevices.remove("Not Connected");
        }
        Log.d ("discovered", deviceName);
        // add device to spinner (dropdown list)
        bleDevices.add(deviceName);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bleDevices);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        blueToothDeviceSpinner.setAdapter(spinnerArrayAdapter);
    }

    private  String CONNECTION_TYPE = "";
    private  String DEVICE_ID = "";

    private TextView textViewMessage;
    private EditText editNetworkDeviceIdEditText;

    private List<String> bleDevices;
    private BLEDriver ble = BLEDriver.instance;// = BLEDriver.instance;
    private Spinner blueToothDeviceSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bleDevices = new ArrayList<String>();
        bleDevices.add("Not Connected");
        //ble = new BLEDriver(this);
        ble.startBrowsing(this);
        //bleDevices.add("WELF");
        //bleDevices.add("Ellis");

        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        String connectionType = intent.getStringExtra(MainActivity.CONNECTION_TYPE);
        CONNECTION_TYPE = connectionType;

        String deviceId = intent.getStringExtra("DeviceId");
        DEVICE_ID =  deviceId;

        // Capture the layout's TextView and set the string as its text
        textViewMessage = findViewById(R.id.textView);
        textViewMessage.setText(message);

//        TextView t = findViewById(R.id.text);
        textViewMessage.setText("Select a LAMPI using either the bluetooth device dropdown list or the network device edit box.\nThen click the appropriate button to select.");

        // Selection of the spinner
        blueToothDeviceSpinner = (Spinner) findViewById(R.id.spinner);

//        blueToothDeviceSpinner.post(new Runnable() {
//            @Override
//            public void run() {
//
//        //        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colors);
//        //        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
//        //        blueToothDeviceSpinner.setAdapter(spinnerArrayAdapter);
//                return;
//            }
//        });

        // Application of the List to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bleDevices);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        blueToothDeviceSpinner.setAdapter(spinnerArrayAdapter);

        editNetworkDeviceIdEditText = (EditText) findViewById(R.id.editNetworkDeviceId);
        editNetworkDeviceIdEditText.setText("Enter Device Id");
        editNetworkDeviceIdEditText.setText("b827eb63a49c");
        if (connectionType.equals("Network")) {
            editNetworkDeviceIdEditText.setText(deviceId);
        } else if (connectionType.equals("BlueTooth")){
            blueToothDeviceSpinner.setSelection(spinnerArrayAdapter.getPosition(deviceId));
        }

        // Click this button to send response result data to source activity.
        Button passDataTargetReturnDataButton = (Button)findViewById(R.id.buttonSelect);
        passDataTargetReturnDataButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View view) {
                Intent intent = new Intent();
                String deviceName = blueToothDeviceSpinner.getSelectedItem().toString();
                intent.putExtra("deviceId", blueToothDeviceSpinner.getSelectedItem().toString());
                intent.putExtra("connectionType", "BlueTooth");
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        // Click this button to send response result data to source activity.
        Button networkIdButton = (Button)findViewById(R.id.buttonNetworkId);
        networkIdButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //EditText editText = (EditText) findViewById(R.id.editNetworkDeviceId);

                intent.putExtra("deviceId", editNetworkDeviceIdEditText.getText().toString());
                intent.putExtra("connectionType", "Network");
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }


    // This method will be invoked when user click android device Back menu at bottom.
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("deviceId", DEVICE_ID);
        intent.putExtra("connectionType", CONNECTION_TYPE);
        intent.putExtra("message_return", "This data is returned when user click back menu in target activity.");
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item  instanceof ActionMenuItem) // this is a hack to make the back button send values back
        {
            onBackPressed();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.home: //this logic did not work
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}