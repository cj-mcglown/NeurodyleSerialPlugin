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

  // async getPermission() {
  //   try {
  //     console.log('getPermissions');
  //     debugger;
  //     const { value } = await NeurodyneUsbSerial.echo({value:'What up'});
  //     this.echo = value;
  //   } catch (error) {
  //     console.error(error);
  //   }
  // }
  // async requestPermission() {
  //   try {
  //     console.log('getPermissions');
  //     debugger;
  //     // const { value } = await NeurodyneUsbSerial.openSerial();
  
     
  
  //   this.trace = this.trace + ' => start'
  //     // await NeurodyneUsbSerial.openSerial(options)
  //     const value = await NeurodyneUsbSerial.connectedDevices();
  //     this.trace = this.trace + ' => after connectedDevices'
  //     this.devicesResponse = value as ConnectedUsbSerialDeviceResponse;
  //     this.device = this.devicesResponse.devices[0].device;
  //     this.trace = this.trace + ` => pre found device ${this.device.deviceId}`
  //     if(this.devicesResponse.devices[0]) {
  //       this.trace = this.trace + ` => found device ${this.device.deviceId}`
  //     }
  //     this.trace = this.trace + ` => prep to open${this.device.deviceId}`
  //     if(this.devicesResponse) {
  //       this.trace = this.trace + ' => after if'
  //       const options = {
  //         baudRate: 115200,
  //         dataBits: 8,
  //         stopBits: 1,
  //         parity: 0,
  //         dtr: true,
  //         rts: true,
  //         deviceId: this.device.deviceId
  //     } as UsbSerialOptions ;
  
  //       this.trace = this.trace + ' => before openserial'
  //       const value = await NeurodyneUsbSerial.requestPermission(options);
  
  //       this.trace = this.trace + ' => after openserial'
  //     }
  //   } catch (error) {
  //     console.error(error);
  //   }
  // }
  

  async openSerial() {
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

  strCallback = (data: { data: string }) : void => {
    this.datavals = data.data;
    // var uint8array = new TextEncoder().encode("someString");
    // var string = new TextDecoder().decode(uint8array);
    

    if(data.data.startsWith("DATA")){
      
      var uint8array = new TextEncoder().encode(this.datavals);
      var restringed = String.fromCharCode.apply(null, Array.from(uint8array));
      alert(restringed);
    }
   
};

      // NeurodyneUsbSerial.addListener('data',this.strCallback);


      async read() {


    
        try {
          
    
         NeurodyneUsbSerial.addListener('data',this.strCallback);
    
        } catch (error) {
          console.error(error);
        }
      }

  async readSingle() {


    
    try {
      

     this.datavals = (await NeurodyneUsbSerial.readSerial()).data;

    } catch (error) {
      console.error(error);
    }
  }












  hasData = false;
  channelCount = 32; 
  headerBytes = 4;
  bytesPerChannel = 2;
  msgFrameSize = 2 * (this.headerBytes + (this.channelCount * this.bytesPerChannel)); // 2 char/byte => 4-byte header, channelCount x 2-byte data per channel
  data: any= [];
  
  parseSignal(msgData: string | any[]) {
    const result = [];

    if (msgData.length % this.msgFrameSize === 0) { // make sure the msg contains the correct frame size

      this.hasData = true;

        let msgCount = msgData.length / this.msgFrameSize;
        // console.log(`Reading ${msgCount} data messages...`);

        for (let i = 0; i < msgCount; i++) {
            let frame = msgData.slice(i * this.msgFrameSize, i * this.msgFrameSize + this.msgFrameSize);
            let header = frame.slice(0, 8) as string;
            let frameData = frame.slice(8);
            // console.log(`[+] HEADER = ${header}`);
            // console.log(`[+] DATA = ${frame.slice(8)}`);

            let headerId = this.decodeString(header);
            if (headerId === 'DATA') {
                let valSize = (2 * this.bytesPerChannel);

                // let channelsCount = frameData.length / 4;
                if (frameData.length % 4 === 0) {
                    // NOTE: despite valSize here, the below is hard-coded to use 2-byte numbers, little endian
                    for (let i = 0; i < frameData.length; i += valSize) {
                        let hexVal = frameData.slice(i, i + valSize); // 80f2
                        let val = `${hexVal[2]}${hexVal[3]}${hexVal[0]}${hexVal[1]}`; // f280
                        result.push(this.decodeNumber(val)); // f280 => -3456 (2's compliment)
                    }
                    // console.log(`data receied (${vals.length}) = `, JSON.stringify(vals)); // .join(','));
                }

                if (frameData.length / 4 === 32 && this.data.length === 4) {
                    this.data[0].push(result[0]);
                    this.data[1].push(result[8]);
                    this.data[2].push(result[16]);
                    this.data[3].push(result[24]);
                }
                else {
                    for (let d = 0; d < this.data.length; d++) {
                        this.data[d].push(result[d]);
                    }
                }

                // for (let d = 0; d < channelCount; d++) {
                //     let valHex = frameData.slice(d * valSize, d * valSize + valSize); // 80f200e580d700ca80bc00af80a10094380f701ea82de03c184c505b886ac079a4f648edece390da34d1d8c77cbe20b52e0f5c1e8a2db83ce64b145b426a7079
                //     if (valHex.length % 4 === 0) {
                //         // let vals: number[] =  [];
                //         for (let i=0; i<valHex.length; i+=4) {
                //             let hexVal = valHex.slice(i, i+4); // 80f2
                //             let val = `${hexVal[2]}${hexVal[3]}${hexVal[0]}${hexVal[1]}`; // f280
                //             result.push(decodeNumber(val)); // f280 => -3456 (2's compliment)
                //         }
                //         // console.log(`data receied (${vals.length}) = `, JSON.stringify(vals)); // .join(','));
                //     }
                //     // // this._data[d].push(val);
                //     // result.push(val);
                // }
            }
        }

    } else {
        console.log(`Unknown Data Msg: `, msgData);
    }

    return result;

}


   decodeString(hex: string) {
    let s = hex.split(/(\w\w)/g)
        .filter(p => !!p)
        .map(c => String.fromCharCode(parseInt(c, 16)))
        .join("");
    return s;
}
decodeNumber(hex: string) {
  if (hex.length % 2 != 0) { hex = "0" + hex; }
  var num = parseInt(hex, 16);
  var maxVal = Math.pow(2, hex.length / 2 * 8);
  if (num > maxVal / 2 - 1) { // handle negative numbers
      num = num - maxVal
  }
  return num;
}

  
}
