package com.example.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.Log;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements LampiNotifyDelegate, LampMQTTDelegate {

    @Override
    public void receiveState(boolean isOn, double h, double s, double brightness) {
        setLampValues(h, s, brightness, isOn);
    }

    public void setHS (byte h, byte s) {
        final int hue = (int) h & 0xFF;
        final int sat = (int) s & 0xFF;;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
                Log.d("setHS", hue+" "+sat);
                setLampValues(hue/255.0, sat/255.0, null, null);
            }
        });

    }
    public void setB (byte b) {
        final int bright = (int) b & 0xFF;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
                Log.d("setB", bright+"");
                setLampValues(null, null,  bright/255.0, null);
            }
        });
    }
    public void setPower (boolean powered) {
        final boolean on = powered;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
                Log.d("setPower", on+"");
                setLampValues(null, null,  null, on);
            }
        });
    }

    //private TextView mTextMessage;
    private TextView deviceIdTextView;
    private TextView messageTextView;
    private TextView noDeviceTextView;

    private ToggleButton onOffToggle;

    private SeekBar seekBarHue;
    private SeekBar seekBarSat;
    private SeekBar seekBarVal;

    private LinearLayout colorBar;

    protected Drawable getSliderShape(float width, int[] colors ) {
        LinearGradient test;
        test = new LinearGradient(0.f, 0.f, width, 0.0f, colors,
                null, Shader.TileMode.CLAMP);
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setShader(test);
        return (Drawable)shape;
    }

    protected Drawable getSliderThumb(int color, int width) {
//        ShapeDrawable th;
//        th = new ShapeDrawable(new OvalShape());
//        th.setIntrinsicWidth(width);
//        th.setIntrinsicHeight(width);
//        th.setColorFilter(color, PorterDuff.Mode.SRC_OVER);
//
//        th.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
//        th.getPaint().setStrokeWidth(2); // in pixel
//        th.getPaint().setColor(Color.BLACK);


        Bitmap bitmap = Bitmap.createBitmap( 100, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        //«bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        Paint circlePaint = new Paint();
        circlePaint.setColor(color);
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        Paint strokePaint = new Paint();
        strokePaint.setColor(Color.BLACK);
        strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        int diameter = width;
        int radius = diameter/2;

        c.drawCircle(diameter / 2 , diameter / 2, radius, strokePaint);

        c.drawCircle(diameter / 2, diameter / 2, radius-4, circlePaint);
// This converts the bitmap to a drawable
        BitmapDrawable mDrawable = new BitmapDrawable(getResources(),bitmap);

        return mDrawable;
        //return th;
    }

    private BLEDriver ble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BLEDriver.makeInstance(this);
        ble = BLEDriver.instance;

        setContentView(R.layout.activity_main);

        deviceIdTextView = (TextView) findViewById(R.id.deviceId);
        messageTextView = (TextView) findViewById(R.id.message);
        noDeviceTextView = (TextView) findViewById(R.id.nodevicetext);

        messageTextView.setText("Connection Type: Not Connected");

        // Click this button to pass data to target activity and
        // then wait for target activity to return result data back.
        Button passDataReturnResultSourceButton = (Button)findViewById(R.id.buttonSelectDevice);
        passDataReturnResultSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
                intent.putExtra("message", "This message comes from PassingDataSourceActivity's second button");
                intent.putExtra(CONNECTION_TYPE, messageTextView.getText().toString());
                intent.putExtra("DeviceId", deviceIdTextView.getText().toString());

                startActivityForResult(intent, REQUEST_CODE_1);
            }
        });

        seekBarHue = (SeekBar)findViewById(R.id.seekbarHue);
        seekBarSat = (SeekBar)findViewById(R.id.seekbarSat);
        seekBarVal = (SeekBar)findViewById(R.id.seekbarVal);

        colorBar = (LinearLayout)findViewById(R.id.colorbarcontainer);
        //do all three bars at once they are all the same with
        seekBarSat.post(new Runnable() {
            @Override
            public void run() {
                int width = seekBarSat.getWidth(); //height is ready
                int[] hueColors = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                        0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
                seekBarHue.setProgressDrawable(getSliderShape(width, hueColors));
                seekBarVal.setProgressDrawable(getSliderShape(width, new int[] { 0xFF000000, 0xFFFFFFFF}));

                // update the display
                seekBarChange();
                setDeviceStatus(!deviceIdTextView.getText().equals("Not Connected"));

                return;
            }
        });

        seekBarHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarChange();
                if (messageTextView.getText().equals("Netword"))
                {
                    boolean power = onOffToggle.isChecked();
                    double h = seekBarHue.getProgress() / 100.0;
                    double s = seekBarSat.getProgress() / 100.0;
                    double b = seekBarVal.getProgress() / 100.0;
                    MosquittoDriver.instance.publishState(power, h, s, b);
                } else {
                    byte h = (byte) (seekBarHue.getProgress() * 255 / 100);
                    byte s = (byte) (seekBarSat.getProgress() * 255 / 100);
                    Log.d("write h s", h + " " + s);
                    ble.writeHSV(h, s);
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("hello", "there");
            }
        });
        seekBarSat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarChange();
                if (messageTextView.getText().equals("Netword")) {
                    boolean power = onOffToggle.isChecked();
                    double h = seekBarHue.getProgress() / 100.0;
                    double s = seekBarSat.getProgress() / 100.0;
                    double b = seekBarVal.getProgress() / 100.0;
                    MosquittoDriver.instance.publishState(power, h, s, b);

                } else {
                    byte h = (byte) (seekBarHue.getProgress() * 255 / 100);
                    byte s = (byte) (seekBarSat.getProgress() * 255 / 100);
                    Log.d("write h s", h + " " + s);
                    ble.writeHSV(h, s);
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("hello", "there");
            }
        });
        seekBarVal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarChange();
                if (messageTextView.getText().equals("Netword"))
                {
                    boolean power = onOffToggle.isChecked();
                    double h = seekBarHue.getProgress() / 100.0;
                    double s = seekBarSat.getProgress() / 100.0;
                    double b = seekBarVal.getProgress() / 100.0;
                    MosquittoDriver.instance.publishState(power, h, s, b);

                } else {
                    byte b = (byte) (seekBarVal.getProgress() * 255 / 100);
                    Log.d("write b", b + "");

                    ble.writeBrightness(b);
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("hello", "there");
            }
        });

        onOffToggle = (ToggleButton)findViewById(R.id.toggleOnOff);
        onOffToggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {


            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(b) {
                    Log.d ("togglebutton", "checked");
                    onOffToggle.setTextColor(Color.YELLOW);
                }
                seekBarChange();

                if (messageTextView.getText().equals("Netword")) {
                    boolean power = onOffToggle.isChecked();
                    double h = seekBarHue.getProgress() / 100.0;
                    double s = seekBarSat.getProgress() / 100.0;
                    double bright = seekBarVal.getProgress() / 100.0;
                    MosquittoDriver.instance.publishState(power, h, s, bright);
                } else {
                    ble.writePower(b);
                }
            }
        });
//setLampValues(0.5, 1.0, 0.7, false);

    }

    // Show / hide lamp controls
    public void setDeviceStatus(boolean isActive)
    {
        if (isActive){
            seekBarHue.setVisibility(View.VISIBLE);
            seekBarVal.setVisibility(View.VISIBLE);
            seekBarSat.setVisibility(View.VISIBLE);
            colorBar.setVisibility(View.VISIBLE);
            onOffToggle.setVisibility(View.VISIBLE);
            noDeviceTextView.setVisibility(View.GONE);
        } else {
            seekBarHue.setVisibility(View.GONE);
            seekBarVal.setVisibility(View.GONE);
            seekBarSat.setVisibility(View.GONE);
            colorBar.setVisibility(View.GONE);
            onOffToggle.setVisibility(View.GONE);
            noDeviceTextView.setVisibility(View.VISIBLE);
        }
    }

    // Update the lamp display (sliders and button)
    public void setLampValues(Double hue, Double sat, Double val, Boolean isOn)
    {
        if (hue != null) { seekBarHue.setProgress((int) Math.round(hue*100.0)); }
        if (sat != null) { seekBarSat.setProgress((int) Math.round(sat*100.0)); }
        if (val != null) { seekBarVal.setProgress((int) Math.round(val*100.0)); }
        if (isOn != null) { onOffToggle.setChecked(isOn); };
    }


    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final String CONNECTION_TYPE = "";

    public void seekBarChange()
    {
        boolean isOn = onOffToggle.isChecked();

        int width = seekBarSat.getWidth(); //height is ready
        int hue = seekBarHue.getProgress();
        int sat = seekBarSat.getProgress();
        int val = seekBarVal.getProgress();

        int fullcolor = Color.HSVToColor(new float[] {hue * 3.6f, 1.0f, 1.0f} );
        int satcolor = Color.HSVToColor(new float[] {hue * 3.6f, sat / 100f, 1.0f} );
        int gsColor = (255 * val) / 100;
        int valcolor =  Color.rgb(gsColor, gsColor, gsColor);

        seekBarHue.setThumb(getSliderThumb(fullcolor, 100));

        seekBarSat.setThumb(getSliderThumb(fullcolor, 50)); //prevents horizontal bar from getting really tall
        seekBarSat.setProgressDrawable(getSliderShape(width, new int[] { 0xFFFFFFFF, fullcolor}));
        seekBarSat.setThumb(getSliderThumb(satcolor, 100));

        seekBarVal.setThumb(getSliderThumb(valcolor, 100));

        colorBar.setBackgroundColor(satcolor);

        if (isOn) {
            onOffToggle.setTextColor(fullcolor);
            //onOffToggle.setHighlightColor(fullcolor);
        } else {
            onOffToggle.setTextColor(Color.BLACK);
        }

//        setDeviceStatus(!deviceIdTextView.getText().equals("Not Connected"));

    }



    private final static int REQUEST_CODE_1 = 1;
    // This method is invoked when target activity return result data back.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // The returned result data is identified by requestCode.
        // The request code is specified in startActivityForResult(intent, REQUEST_CODE_1); method.
        switch (requestCode)
        {
            // This request code is set by startActivityForResult(intent, REQUEST_CODE_1) method.
            case REQUEST_CODE_1:
                if(resultCode == RESULT_OK)
                {
                    String deviceId = dataIntent.getStringExtra("deviceId");
                    String connectionType = dataIntent.getStringExtra("connectionType");
                    String messageReturn = dataIntent.getStringExtra("message_return");
                    deviceIdTextView.setText(deviceId);
                    messageTextView.setText(connectionType);
                    setDeviceStatus(!deviceId.equals("Not Connected"));

                    if (connectionType.equals("Network"))
                    {
                        MosquittoDriver.instance.setCurrentDevice(deviceId);
                        MosquittoDriver.instance.setDelegate(this);
                    }
                    if (connectionType.equals("BlueTooth") && deviceId.contains(":")) {
                        // Bluetooth device selected
                        String mac = deviceId.split(" ")[1].replace("(", "").replace(")", "");
                        ble.connect(mac, this, this);
                    } else {
                        // no device
                    }
                }
        }
    }
}

