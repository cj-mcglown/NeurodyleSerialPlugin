package com.neurodyne.plugins.usbserial;

import android.hardware.usb.UsbDevice;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public interface Callback {
    void log(String TAG, String text);

    void connected (UsbDevice device);

    void usbDeviceAttached (UsbDevice device);
    void usbDeviceDetached(UsbDevice device);

    void receivedData(String Data);
    void receivedMsg(String msg); // brent - add simple message passing for error codes, statuses, etc

    void error(Error error);
}
