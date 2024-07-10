package com.neurodyne.plugins.usbserial;

import android.util.Log;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;

//import com.google.common.util.concurrent.RateLimiter;
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
//import com.hoho.android.usbserial.driver.Ch34xSerialDriver;
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
//import com.hoho.android.usbserial.driver.ProlificSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
//import com.viewtrak.plugins.usbserial.Utils.*;
import com.neurodyne.plugins.usbserial.Utils.*;

import org.json.JSONArray;

import java.io.IOException;
import java.lang.Error;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// brent - USB stuff
import android.app.AlertDialog;
// import android.hardware.usb.UsbManager;
// import android.hardware.usb.UsbDevice;
// import android.content.BroadcastReceiver;
// import android.content.IntentFilter;
// import android.app.PendingIntent;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;


public class NeurodyneUsbSerial  implements SerialInputOutputManager.Listener {
    private final Context context;
    // call that will be used to send back usb device attached/detached event
    private final Callback callback;

    // activity reference from UsbSerialPlugin
//    private AppCompatActivity mActivity;
    // call that will have data to open connection
//    private PluginCall openSerialCall;

    // usb permission tag name
    // public static final String USB_PERMISSION = "com.viewtrak.plugins.usbserial.USB_PERMISSION";
    private static final int WRITE_WAIT_MILLIS = 2000; // brent - these are kind of strange; maybe the read call should pass in this value (time) or number of bytes to read?
    private static final int READ_WAIT_MILLIS = 2000;

    

    private enum UsbPermission {Unknown, Requested, Granted, Denied}

    // logging tag
//    private final String TAG = UsbSerial.class.getSimpleName();

    //    private boolean sleepOnPause;
    // I/O manager to handle new incoming serial data
    private SerialInputOutputManager usbIoManager;
    // Default Usb permission state
    private UsbPermission usbPermission = UsbPermission.Unknown;
    // The serial port that will be used in this plugin
    private UsbSerialPort usbSerialPort;
    // Usb serial port connection status
//    private boolean connected = false;
    UsbDevice connectedDevice;
    // USB permission broadcastreceiver
    private final Handler mainLooper;
    String messageNMEA = "";


    // brent - custom USB permission handler
    UsbDevice device;
    private static final String ACTION_USB_PERMISSION =  "com.neurodyne.seizuresense.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            // call method to set up device communication
                            Log.d("NeuroSerial", "USB device " + device);
                            if (callback != null) {
                                callback.receivedMsg("READY"); // brent - let the client know the device is connected
                            }
                        }
                    }
                    else {
                        Log.d("NeuroSerial", "permission denied for device " + device);
                        if (callback != null) {
                            callback.receivedMsg("PERMISSION_DENIED"); // brent - let the client know the permissions were denied
                        }
                    }
                }
            }
        }
    };


//    private RateLimiter throttle = RateLimiter.create(1.0);

    public NeurodyneUsbSerial(Context context, Callback callback) {
        super();
        this.context = context;
        this.callback = callback;

        mainLooper = new Handler(Looper.getMainLooper());

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra("device");
                    callback.usbDeviceAttached(usbDevice);
                }
            }
        }, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra("device");
                    callback.usbDeviceDetached(usbDevice);

                    // brent - let the client know the device was disconnected
                    if (callback != null) {
                        callback.receivedMsg("NO_DEVICE");
                    }
                }
            }
        }, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    @Override
    public void onNewData(byte[] data) {
        mainLooper.post(() -> updateReceivedData(data));
    }

    @Override
    public void onRunError(Exception e) {
        mainLooper.post(() -> {
            updateReadDataError(e);
            closeSerial();
        });
    }

    void setConnectedDevice(UsbDevice connectedDevice) {
        if (connectedDevice != this.connectedDevice) {
            this.connectedDevice = connectedDevice;
            this.callback.connected(connectedDevice);
        }
    }

    public void closeSerial() {
        setConnectedDevice(null);
        if (usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
            usbIoManager = null;
        }
        usbPermission = UsbPermission.Unknown;
        try {
            if (usbSerialPort != null) {
                usbSerialPort.close();
            }
        } catch (IOException ignored) {
        }
        usbSerialPort = null;
    }

    // brent - add separate permission request method for calling from the js client
    public void requestPermissions() {
        try {
            UsbManager m_usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
            
            // MUST USE explicit intent (working on OnePlus 12R test device) -- other issues, notes here: https://stackoverflow.com/questions/77275691/targeting-u-version-34-and-above-disallows-creating-or-retrieving-a-pendingin
            Intent explicitIntent = new Intent(ACTION_USB_PERMISSION);
            explicitIntent.setPackage(this.context.getPackageName()); // .packageName);
            PendingIntent permissionIntent = PendingIntent.getBroadcast(this.context, 0, explicitIntent, PendingIntent.FLAG_IMMUTABLE);
                        
            
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            this.context.registerReceiver(usbReceiver, filter);

            HashMap<String, UsbDevice> deviceList = m_usbManager.getDeviceList();
            Log.d("NeuroSerial", "Devices = " + deviceList.keySet()); // Devices = [/dev/bus/usb/002/002]

            if (!deviceList.isEmpty()) {
                // for phones, expect only 1 device to be connected so just use the first one...
                device = (UsbDevice) deviceList.values().toArray()[0]; // [0]; // "/dev/bus/usb/002/002"); // device = deviceList.get("Espressif Device");

                String deviceInfo = "Device Name:" + device.getDeviceName() + " | Device Id:" + device.getDeviceId() + " | Vendor Id:" + device.getVendorId() + " | Product Id:" + device.getProductId();
                Log.d("NeuroSerial", "Current device = " + deviceInfo);
                Log.d("NeuroSerial", "Full Device Info = " + device);

                if (!m_usbManager.hasPermission(device)) {
                    if (device != null) {
                        m_usbManager.requestPermission(device, permissionIntent);
                    }
                }
                else {
                    // device is ready!
                    callback.receivedMsg("READY");
                }
            }
            else {
                // no USB device connection detected
                callback.receivedMsg("NO_DEVICE");
            }
        }
        catch (Exception exception) {
            throw new Error(exception.getMessage(), exception.getCause());
        }

    }

    public Boolean openSerial(UsbSerialOptions settings) {
        try {
            Log.w("NeuroSerial", "Trying to open serial comms...");

            closeSerial();

            // Sleep On Pause defaults to true
            // this.sleepOnPause = openSerialCall.hasOption("sleepOnPause") ? openSerialCall.getBoolean("sleepOnPause") : true;            

            UsbDevice device = null;
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            for (UsbDevice v : usbManager.getDeviceList().values()) {
                if (v.getDeviceId() == settings.deviceId)
                    device = v;
            }

            if (device == null) {
                // throw new Error("connection failed: device not found", new Throwable("connectionFailed:DeviceNotFound"));
                if (callback != null) {
                    callback.receivedMsg("NO_DEVICE");
                }
                return false;
            }

            String deviceInfo = "Device Name:" + device.getDeviceName() + "Device Id:" + device.getDeviceId();
            Log.w("NeuroSerial", deviceInfo);

            UsbSerialDriver driver = getProper().probeDevice(device);
            if (driver == null) {
                // tyring custom
                driver = getDriverClass(device);
            }
            if (driver == null) {
                // throw new Error("connection failed: no driver for device", new Throwable("connectionFailed:NoDriverForDevice"));
                if (callback != null) {
                    callback.receivedMsg("NO_DEVICE");
                }
                return false;
            }
            if (driver.getPorts().size() < settings.portNum) {
                // throw new Error("connection failed: not enough ports at device", new Throwable("connectionFailed:NoAvailablePorts"));
                if (callback != null) {
                    callback.receivedMsg("NO_DEVICE");
                }
                return false;
            }

            UsbDevice usbDev = driver.getDevice();
            if (usbDev == null) {
                if (callback != null) {
                    callback.receivedMsg("NO_DEVICE");
                }
                return false;
            }

            String usbDevName = usbDev.getDeviceName();
            int usbDevId = usbDev.getDeviceId();
            String usbProductName = usbDev.getProductName();
            int usbProductId = usbDev.getProductId();            

            String deviceInfo2 = "Device Name:" + usbDevName + "Product Id:" + usbProductName + "Device Id:" + usbDevId + "Product Name:" + usbProductName;
            Log.w("NeuroSerial", deviceInfo);

            usbSerialPort = driver.getPorts().get(settings.portNum);
            int portnumber = settings.portNum;
            Log.w("NeuroSerial", "post-ports" + " Port Number: " + portnumber);


            // updateReadDataError(new Exception(deviceInfo22));
            // updateReadDataError(new Exception(driver.getClass().getSimpleName()));

            UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
            if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {

                if (callback != null) {
                    callback.receivedMsg("NO_DEVICE");
                }
                return false;
            }

            if (usbConnection == null) {
                if (callback != null) {
                    callback.receivedMsg("NO_DEVICE");
                }
                return false;
            }

            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(settings.baudRate, settings.dataBits, settings.stopBits, settings.parity);
            if (settings.dtr) usbSerialPort.setDTR(true);
            if (settings.rts) usbSerialPort.setRTS(true);
            usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
            usbIoManager.start();

            setConnectedDevice(device);

            return true;

        } catch (Exception exception) {
            closeSerial();
            throw new Error(exception.getMessage(), exception.getCause());
        }
    }

    String readSerial() {
        if (connectedDevice == null) {
            throw new Error("not connected", new Throwable("NOT_CONNECTED"));
        }
        try {
            byte[] buffer = new byte[8192];
            int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);
//            str.concat("\n");
            return HexDump.toHexString(Arrays.copyOf(buffer, len));
        } catch (IOException e) {
            // when using read with timeout, USB bulkTransfer returns -1 on timeout _and_ errors
            // like connection loss, so there is typically no exception thrown here on error
            closeSerial();
            throw new Error("connection lost: " + e.getMessage(), e.getCause());
        }
    }

    void writeSerial(String str) {
        if (connectedDevice == null) {
            throw new Error("not connected", new Throwable("NOT_CONNECTED"));
        }
        if (str.length() == 0) {
            throw new Error("can't send empty string to device", new Throwable("EMPTY_STRING"));
        }
        try {
            byte[] data = (str + "\r\n").getBytes();
            usbSerialPort.write(data, WRITE_WAIT_MILLIS);
        } catch (Exception e) {
            closeSerial();
            throw new Error("connection lost: " + e.getMessage(), e.getCause());
        }
    }


//    void onResume() {
//        if (sleepOnPause) {
//            if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted)
//                mainLooper.post(() -> {
//                    openSerial(this.openSerialCall);
//                });
//        }
//    }
//
//    void onPause() {
//        if (connected && sleepOnPause) {
//            disconnect();
//        }
//    }
 

    // brent - data conversion to HEX for parsing on js app
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    // brent - parse messageses coming from serial data based on our own spec (hard-coded)    
    int frameSize = 2 * (4 + (32*2)); // HEX: 2 bytes per val => 2 x (4-byte header, 32 2-byte numbers) // int frameSize = (4 + (32*2)); // ASCII: 4 bytes per val => 4-byte header, 32 2-byte numbers
    String eegMesssage = "";
    String begin = "44415441"; // "DATA";
    private void updateReceivedData(byte[] data) {
        try {
            this.eegMesssage += bytesToHex(data); // new String(data, StandardCharsets.US_ASCII);   // .UTF_8);
            if (this.eegMesssage.contains(this.begin) && this.eegMesssage.length() >= frameSize) {
                int bol = this.eegMesssage.indexOf(this.begin);
                if (this.eegMesssage.length() - bol >= frameSize) {
                    String frame = this.eegMesssage.substring(bol, bol + frameSize);
                    callback.receivedData(frame);
                    this.eegMesssage = this.eegMesssage.substring(bol + frameSize);
                }
            }            
            if (this.eegMesssage.length() >= 3 * frameSize) {
                updateReadDataError(new Exception("(length=" + this.eegMesssage.length() + ") =>" + this.eegMesssage));
                this.eegMesssage = "";
            }
        } catch (Exception exception) {
            updateReadDataError(exception);
        }
    }

    private void updateReadDataError(Exception exception) {
        callback.error(new Error(exception.getMessage(), exception.getCause()));
    }

    UsbSerialDriver getDriverClass(final UsbDevice usbDevice) {
        Class<? extends UsbSerialDriver> driverClass = null;

        final int vid = usbDevice.getVendorId();
        final int pid = usbDevice.getProductId();

        if (vid == 1027) {
            switch (pid) {
                case 24577:
                case 24592:
                case 24593:
                case 24596:
                case 24597:
                    driverClass = FtdiSerialDriver.class;
            }
        } else if (vid == 4292) {
            switch (pid) {
                case 60000:
                case 60016:
                case 60017:
                    driverClass = Cp21xxSerialDriver.class;
            }
        } else if (vid == 1659) {
            switch (pid) {
                case 8963:
                case 9123:
                case 9139:
                case 9155:
                case 9171:
                case 9187:
//                case 9203:
//                    driverClass = ProlificSerialDriver.class;
            }
        } else if (vid == 6790) {
            switch (pid) {
                case 21795:
//                case 29987:
//                    driverClass = Ch34xSerialDriver.class;
            }
        } else {
            if (vid == 9025 || vid == 5446 || vid == 3725
                    || (vid == 5824 && pid == 1155)
                    || (vid == 1003 && pid == 8260)
                    || (vid == 7855 && pid == 4)
                    || (vid == 3368 && pid == 516)
                    || (vid == 1155 && pid == 22336)
            )
                driverClass = CdcAcmSerialDriver.class;
        }

        if (driverClass != null) {
            final UsbSerialDriver driver;
            try {
                final Constructor<? extends UsbSerialDriver> ctor =
                        driverClass.getConstructor(UsbDevice.class);
                driver = ctor.newInstance(usbDevice);
            } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return driver;
        }

        return null;
    }

    private UsbSerialProber getProper() {
        ProbeTable customTable = UsbSerialProber.getDefaultProbeTable();

        // 0x0403 / 0x60??: FTDI
        customTable.addProduct(1027, 24577, FtdiSerialDriver.class); // 0x6001: FT232R
        customTable.addProduct(1027, 24592, FtdiSerialDriver.class); // 0x6010: FT2232H
        customTable.addProduct(1027, 24593, FtdiSerialDriver.class); // 0x6011: FT4232H
        customTable.addProduct(1027, 24596, FtdiSerialDriver.class); // 0x6014: FT232H
        customTable.addProduct(1027, 24597, FtdiSerialDriver.class); // 0x6015: FT230X, FT231X, FT234XD

        // 0x10C4 / 0xEA??: Silabs CP210x
        customTable.addProduct(4292, 60000, Cp21xxSerialDriver.class); // 0xea60: CP2102 and other CP210x single port devices
        customTable.addProduct(4292, 60016, Cp21xxSerialDriver.class); // 0xea70: CP2105
        customTable.addProduct(4292, 60017, Cp21xxSerialDriver.class); // 0xea71: CP2108

        // 0x067B / 0x23?3: Prolific PL2303x
//        customTable.addProduct(1659, 8963, ProlificSerialDriver.class); // 0x2303: PL2303HX, HXD, TA, ...
//        customTable.addProduct(1659, 9123, ProlificSerialDriver.class); // 0x23a3: PL2303GC
//        customTable.addProduct(1659, 9139, ProlificSerialDriver.class); // 0x23b3: PL2303GB
//        customTable.addProduct(1659, 9155, ProlificSerialDriver.class); // 0x23c3: PL2303GT
//        customTable.addProduct(1659, 9171, ProlificSerialDriver.class); // 0x23d3: PL2303GL
//        customTable.addProduct(1659, 9187, ProlificSerialDriver.class); // 0x23e3: PL2303GE
//        customTable.addProduct(1659, 9203, ProlificSerialDriver.class); // 0x23f3: PL2303GS

        // 0x1a86 / 0x?523: Qinheng CH34x
//        customTable.addProduct(6790, 21795, Ch34xSerialDriver.class); // 0x5523: CH341A
//        customTable.addProduct(6790, 29987, Ch34xSerialDriver.class); // 0x7523: CH340

        // CDC driver
        // customTable.addProduct(9025,      , driver)  // 0x2341 / ......: Arduino
        customTable.addProduct(5824, 1155, CdcAcmSerialDriver.class); // 0x16C0 / 0x0483: Teensyduino
        customTable.addProduct(1003, 8260, CdcAcmSerialDriver.class); // 0x03EB / 0x2044: Atmel Lufa
        customTable.addProduct(7855, 4, CdcAcmSerialDriver.class); // 0x1eaf / 0x0004: Leaflabs Maple
        customTable.addProduct(3368, 516, CdcAcmSerialDriver.class); // 0x0d28 / 0x0204: ARM mbed
        customTable.addProduct(1155, 22336, CdcAcmSerialDriver.class); // 0x0483 / 0x5740: ST CDC

        customTable.addProduct(12346, 16385, CdcAcmSerialDriver.class); // 0x303A / 0x4001: ESP32 (NeuroDyne EEG)

        return new UsbSerialProber(customTable);
    }

    public JSONArray devices() {
        List<DeviceItem> listItems = new ArrayList<>();
        UsbSerialProber usbProper = getProper();
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbProper.probeDevice(device);
            if (driver != null) {
                for (int port = 0; port < driver.getPorts().size(); port++)
                    listItems.add(new DeviceItem(device, port, driver));
            } else {
                listItems.add(new DeviceItem(device, 0, getDriverClass(device)));
            }
        }
        return  Utils.deviceListToJsonConvert(listItems);
    }

    public String echo(String value) {
        Log.i("Echo", "cj" + value);
        return value;
    }
}
