package io.ionic.starter;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;
import com.neurodyne.plugins.usbserial.NeurodyneUsbSerialPlugin;

public class MainActivity extends BridgeActivity {
  @Override
  protected  void  onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    registerPlugin(NeurodyneUsbSerialPlugin.class);
  }

}
