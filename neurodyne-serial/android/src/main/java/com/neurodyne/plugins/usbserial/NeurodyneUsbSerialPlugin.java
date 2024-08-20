package com.neurodyne.plugins.usbserial;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.Window;
import android.view.WindowManager;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

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

import java.nio.ByteBuffer;

import android.util.Log;

@CapacitorPlugin(name = "NeurodyneUsbSerial")
public class NeurodyneUsbSerialPlugin extends Plugin  implements Callback {
    private NeurodyneUsbSerial implementation;
    private final String TAG = NeurodyneUsbSerialPlugin.class.getSimpleName();

    private Boolean _isActive = false;

    @Override
    public void load() {
        super.load();
        implementation = new NeurodyneUsbSerial(getContext(), this);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        try {
            implementation.requestPermissions();
            call.resolve();
        } catch (Exception e) {
            call.reject(e.toString());
        }
    }

    @PluginMethod
    public void connectedDevices(PluginCall call) {
        try {
            JSObject jsObject = new JSObject();
            JSONArray devices = implementation.devices();
            jsObject.put("devices", devices);
            call.resolve(jsObject);
        } catch (Exception e) {
            call.reject(e.toString());
        }
    }


    @PluginMethod
    public void openSerial(PluginCall call) {
        try {
            UsbSerialOptions settings = new UsbSerialOptions();

            if (call.hasOption("deviceId"))
                settings.deviceId = call.getInt("deviceId");

            if (call.hasOption("portNum"))
                settings.portNum = call.getInt("portNum");

            if (call.hasOption("baudRate"))
                settings.baudRate = call.getInt("baudRate");

            if (call.hasOption("dataBits"))
                settings.dataBits = call.getInt("dataBits");

            if (call.hasOption("stopBits"))
                settings.stopBits = call.getInt("stopBits");

            if (call.hasOption("parity"))
                settings.parity = call.getInt("parity");

            if (call.hasOption("dtr"))
                settings.dtr = call.getBoolean("dtr");

            if (call.hasOption("rts"))
                settings.rts = call.getBoolean("rts");

            Boolean result = implementation.openSerial(settings);

            this._isActive = result;

            JSObject jsObject = new JSObject();
            jsObject.put("isOpen", result);
            call.resolve(jsObject);

            // call.resolve(new JSObject());
        } catch (Exception e) {
            call.reject(e.toString());
        }
    }

    @PluginMethod
    public void closeSerial(PluginCall call) {
        try {
            implementation.closeSerial();
            this._isActive = false;
            call.resolve(new JSObject());
        } catch (Exception e) {
            call.reject(e.toString());
        }
    }

    @PluginMethod
    public void readSerial(PluginCall call) {
        try {
            if (!this._isActive) { return; }

            JSObject jsObject = new JSObject();
            String result = implementation.readSerial();
            jsObject.put("data", result);
            call.resolve(jsObject);
        } catch (Exception e) {
            call.reject(e.toString());
        }
    }

    @PluginMethod
    public void writeSerial(PluginCall call) {
        try {
            if (!this._isActive) { return; }

            String data = call.hasOption("data") ? call.getString("data") : "";
            implementation.writeSerial(data);
            call.resolve(new JSObject());
        } catch (Exception e) {
            call.reject(e.toString());
        }
    }


    @Override
    public void log(String TAG, String text) {
        if (!this._isActive) { return; }

        JSObject ret = new JSObject();
        ret.put("text", text);
        ret.put("tag", TAG);
        Log.i("PluginListener", "log => " + text);
        notifyListeners("log", ret);
    }

    @Override
    public void connected(UsbDevice device) {
        if (!this._isActive) { return; }

        JSObject ret = new JSObject();
        if (device != null) {
            ret.put("pid", device.getProductId());
            ret.put("vid", device.getVendorId());
            ret.put("did", device.getDeviceId());
        }
        Log.i("PluginListener", "connected => ");
        notifyListeners("connected", ret);
    }

    @Override
    public void usbDeviceAttached(UsbDevice device) {
        if (!this._isActive) { return; }

        JSObject ret = new JSObject();
        if (device != null) {
            ret.put("pid", device.getProductId());
            ret.put("vid", device.getVendorId());
            ret.put("did", device.getDeviceId());
        }
        Log.i("PluginListener", "usbDeviceAttached => ");
        notifyListeners("attached", ret);
    }

    @Override
    public void usbDeviceDetached(UsbDevice device) {
        if (!this._isActive) { return; }

        JSObject ret = new JSObject();
        if (device != null) {
            ret.put("pid", device.getProductId());
            ret.put("vid", device.getVendorId());
            ret.put("did", device.getDeviceId());
        }
        Log.i("PluginListener", "usbDeviceDetached => ");
        notifyListeners("detached", ret);
    }

    @Override
    public void receivedData(String Data) {
        if (!this._isActive) { return; }

        JSObject ret = new JSObject();
        ret.put("data", Data);
        notifyListeners("data", ret);
    }

    @Override
    public void receivedMsg(String msg) {
        if (!this._isActive) { return; }

        JSObject ret = new JSObject();
        ret.put("info", msg);
        Log.i("PluginListener", "msg => " + msg);
        notifyListeners("info", ret);
    }

    @Override
    public void error(Error error) {
        if (!this._isActive) { return; }
        
        JSObject ret = new JSObject();
        ret.put("error", error.toString());
        Log.i("PluginListener", "error => " + error.toString());
        notifyListeners("error", ret);
    }

//    private NeurodyneUsbSerial implementation = new NeurodyneUsbSerial();

    @PluginMethod
    public void keepAwake(final PluginCall call) {
        getBridge()
                .executeOnMainThread(
                        () -> {
                            Window window = getActivity().getWindow();
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            call.resolve();
                        }
                );
    }

    @PluginMethod
    public void allowSleep(final PluginCall call) {
        getBridge()
                .executeOnMainThread(
                        () -> {
                            Window window = getActivity().getWindow();
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            call.resolve();
                        }
                );
    }

    @PluginMethod
    public void isSupported(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("isSupported", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void isKeptAwake(final PluginCall call) {
        getBridge()
                .executeOnMainThread(
                        () -> {
                            // use the "bitwise and" operator to check if FLAG_KEEP_SCREEN_ON is on or off
                            // credits: https://stackoverflow.com/a/24214209/9979122
                            int flags = getActivity().getWindow().getAttributes().flags;
                            boolean isKeptAwake = (flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0;

                            JSObject ret = new JSObject();
                            ret.put("isKeptAwake", isKeptAwake);
                            call.resolve(ret);
                        }
                );
    }

    // Screen Brightness
    @PluginMethod
    public void setBrightness(PluginCall call) {
        Float brightness = call.getFloat("brightness");
        Activity activity = getActivity();
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();

        activity.runOnUiThread(
                () -> {
                    layoutParams.screenBrightness = brightness;
                    activity.getWindow().setAttributes(layoutParams);
                    call.resolve();
                }
        );
    }

    @PluginMethod
    public void getBrightness(PluginCall call) {
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        JSObject ret = new JSObject();
        call.resolve(
                new JSObject() {
                    {
                        put("brightness", layoutParams.screenBrightness);
                    }
                }
        );
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }


}
