import { Component } from '@angular/core';
import { Platform } from '@ionic/angular';
import { ConnectedUsbSerialDeviceResponse, Device, NeurodyneUsbSerial, UsbSerialOptions } from '../plugins/neurodyne_serial';
// import { SerialService } from '../serivces/serial.service';
//declare let NeurodyneUsbSerial: any;


@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page {

  platformReady = false;

  constructor(
    // public serialService: SerialService
    private platform: Platform
    ) {
     
      console.log('contructor');
      debugger;
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

  readSerial() {
    //usbSerialPort.requestPermission(() => {
      //         console.log('get permission success.');
      //         usbSerialPort.getDevice((data: { name: string; }) => {
      //             this.title = data.name;
      //         });
      // usbSerial.echo((x:string) => {
      // console.log('here');
      // console.log(x);
      // this.echo = x;

   // });

  }

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
      // const { value } = await NeurodyneUsbSerial.openSerial();

     

    this.trace = this.trace + ' => start'
      // await NeurodyneUsbSerial.openSerial(options)
      const value = await NeurodyneUsbSerial.connectedDevices();
      this.trace = this.trace + ' => after connectedDevices'
      this.devicesResponse = value as ConnectedUsbSerialDeviceResponse;
      this.device = this.devicesResponse.devices[0].device;
      this.trace = this.trace + ` => pre found device ${this.device.deviceId}`
      if(this.devicesResponse.devices[0]) {
        this.trace = this.trace + ` => found device ${this.device.deviceId}`
      }
      this.trace = this.trace + ` => prep to open${this.device.deviceId}`
      if(this.devicesResponse) {
        this.trace = this.trace + ' => after if'
        const options = {
          baudRate: 115200,
          dataBits: 8,
          stopBits: 1,
          parity: 0,
          dtr: true,
          rts: true,
          deviceId: this.device.deviceId
      } as UsbSerialOptions ;

        this.trace = this.trace + ' => before openserial'
        const value = await NeurodyneUsbSerial.openSerial(options);

        this.trace = this.trace + ' => after openserial'
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
    // this.serialService.openSerialPort();
  
}
