import { Component } from '@angular/core';
import { Platform } from '@ionic/angular';
import { ConnectedUsbSerialDeviceResponse, Device, NeurodyneUsbSerial, SetBrightnessOptions, UsbSerialOptions } from '../plugins/neurodyne_serial';
// import { SerialService } from '../serivces/serial.service';
//declare let NeurodyneUsbSerial: any;


@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page {
  brightness = 0.5;

  platformReady = false;

  constructor(
    private platform: Platform
    ) {
      this.platform.ready().then(() => {
        console.log('Plaform Ready', true);
        this.platformReady = true;
      })
    }

    echo = 'nope';
    trace = 'trace';
    devicesResponse: ConnectedUsbSerialDeviceResponse = {} as ConnectedUsbSerialDeviceResponse;

    device: Device = {} as Device;

    datavals = 'empty data vals: ';


  async getPermission() {
    try {
      console.log('getPermissions');
      debugger;
      const { value } = await NeurodyneUsbSerial.echo({value:'What up'});
      this.echo = value;
    } catch (error) {
      console.error(error);
    }
  }

  async getPermissionReal() {
    try {
      console.log('getPermissions');
      debugger;

      const value = await NeurodyneUsbSerial.connectedDevices();
      this.devicesResponse = value as ConnectedUsbSerialDeviceResponse;
      this.device = this.devicesResponse.devices[0].device;
      if(this.devicesResponse.devices[0]) {
      }
      if(this.devicesResponse) {
        const options = {
          baudRate: 115200,
          dataBits: 8,
          stopBits: 1,
          parity: 0,
          dtr: true,
          rts: true,
          deviceId: this.device.deviceId
      } as UsbSerialOptions ;
        const value = await NeurodyneUsbSerial.openSerial(options);
      }
    } catch (error) {
      console.error(error);
    }
  }

  async read() {


    
    try {
    this.datavals = (await NeurodyneUsbSerial.readSerial()).data;

    } catch (error) {
      console.error(error);
    }
  }

  async keepAwake() {
    await NeurodyneUsbSerial.keepAwake().then( () => {
      console.log("keeping Awake");
    });

  }

  async allowSleep() {
    await NeurodyneUsbSerial.allowSleep().then( () => {
      console.log("Allowing Awake");
    });

  }

  async isSupported() {
    await NeurodyneUsbSerial.isSupported().then( res => {
      console.log('isSupported Result', res);
    });

  }

  async isKeptAwake() {
    await NeurodyneUsbSerial.isKeptAwake().then( res => {
      console.log("isKepAwake Result", res);
    });

  }

  async getBrightness() {
    await NeurodyneUsbSerial.getBrightness().then( x => {
      alert("Brightness value is: " + x.brightness);
    });

  }

  async setBrightness(val: number) {
    let options: SetBrightnessOptions = {brightness: val};
    await NeurodyneUsbSerial.setBrightness(options).then( () => {
      alert("Brightness set to: " + val);
      this.brightness = val;

    });

  }
}
