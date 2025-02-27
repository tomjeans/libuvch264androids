package com.xxp.callbackh264;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.TextView;

import com.xxp.callbackh264.databinding.ActivityMainBinding;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'callbackh264' library on application startup.
    static {
        System.loadLibrary("libuvc_split");
        System.loadLibrary("jpeg-turbo212");
        System.loadLibrary("usb1.0");
        System.loadLibrary("uvc");
    }

    private ActivityMainBinding binding;

    int index=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String ACTION_USB_PERMISSION = "com.xxp.callbackh264.USB_PERMISSION";

        Context context = getApplicationContext();

        // 获取 USB 管理器
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);//
        //if this param is true ok capture device
        //mHasAudioCapture = true
        //mHasAudioPlayback = true
        //
        //mHasVideoCapture = true
        //mHasVideoPlayback = true
        //

        // 枚举所有连接的 USB 设备
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        // 请求权限（以指定设备为例）
        for (UsbDevice usbDevice : deviceList.values()) {
            usbManager.requestPermission(usbDevice, permissionIntent);
        }


        // Example of a call to a native method
        //TextView tv = binding.sampleText;
        //tv.setText(stringFromJNI());

        boolean status=streamInit(1);
        //0x2bc5, 0x0529
        boolean status2=devicesopen(1,0x2bc5,0x0529);

        boolean status2_ = setparam(1,1920,1080,30,0x10);//h264

        boolean status3=h264startcallback(1);



    }

    /**
     * A native method that is implemented by the 'callbackh264' native library,
     * which is packaged with this application.
     */





    public native String stringFromJNI();

    public native boolean streamInit(int streamid);

    public native boolean devicesopen(int streamid,int vendor_id,int production_id);

    public native boolean h264startcallback(int streamid);

    public native boolean setparam(int streamid,int width,int height,int fps,int format_type);


    void callbackframe(byte[] frames_buffer,int width,int height)
    {
        //frames_buffer byte yuv origin data
        //width;//width
        //height;//height
        System.out.println("the decoder width :"+width+"height :"+height);
        index++;
        getFileFromBytes(frames_buffer,"/sdcard/nv21_mpp_decoder_usb/"+index+".yuv");


    }


    public File getFileFromBytes(byte[] b, String outputFile) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.flush();
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }
}