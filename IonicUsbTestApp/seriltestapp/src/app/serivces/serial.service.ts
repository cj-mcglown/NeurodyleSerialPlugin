// import { Injectable, NgZone } from '@angular/core';
// // import { Serial, SerialOpenOptions } from  '@ionic-native/serial';
// declare let usbSerialPort: any;

// @Injectable({
//   providedIn: 'root'
// })
// export class SerialService {
//   public serialLog = 'empty';
//   public serialErrorLog = 'errors: none';
//   title: string = 'none';
//   timer: any = null;
//   receiveDataArray: any[] = [];
//   receiveData = '';
//   receiveLength = 0;
//   sendLength = 0;

//   view!: Uint8Array;
//   viewString: any;
//   viewString2: any;

//   restringed:any;

//   ddata:any;

//   constructor(private zone: NgZone) {}

//    /**
//      * open serial
//      *
//      * @memberof HomePage
//      */
//    async openSerialPort() {
//     const config = {
//       baudRate: 115200,
//       dataBits: 8,
//       stopBits: 1,
//       parity: 0,
//       dtr: true,
//       rts: true,
//       sleepOnPause: true
//   }
//     // First request permission
//     usbSerialPort.requestPermission(() => {
//         console.log('get permission success.');
//         usbSerialPort.getDevice((data: { name: string; }) => {
//             this.title = data.name;
//         });
//         // open serial
//         usbSerialPort.open(config, () => {
//             console.log('Serial connection opened');
//             this.serialLog = 'Open Serial - Prior';
//             // get open status
//             this.isOpen();
//             this.serialLog = 'Open Serial - Post isactive flag';
//             // read listener
//             usbSerialPort.readListener((data: Iterable<number>) => {
//                 this.serialLog = 'Open Serial - READING';
//                 clearTimeout(this.timer);
//                 const view = new Uint8Array(data);
//                 // this.view = new Uint8Array(data);
//                  this.ddata = data;
//                  //this.restringed = String.fromCharCode.apply(null, new Uint8Array(data));
//                 this.receiveDataArray.push(view);
//                 this.timer = setTimeout(() => {
//                     const now = new Date();
//                     const dateMs = now.getMilliseconds();
//                     this.zone.run(() => {
//                         // const date = `<span style="color: #2fdf75">${this.formatDate(now, 'hh:mm:ss')}.${dateMs} > </span>`;
//                         // const resultUint8Array = this.concatUint(Uint8Array, ...this.receiveDataArray);
//                         // if (!this.bytes2HexString(resultUint8Array)) {
//                         //     return;
//                         // }
//                         // this.receiveData += `
//                         //                     <div style="
//                         //                     -webkit-user-select: auto;
//                         //                     -moz-user-select: auto;
//                         //                     -ms-user-select: auto;
//                         //                     user-select: auto;">
//                         //                     ${date}${this.strDivision(this.bytes2HexString(resultUint8Array)!, 2)}
//                         //                     </div>
//                         //                 `;
//                         // this.receiveData += `<div style="margin-top:8px"></div>`;
//                         // this.receiveLength = this.bytes2HexString(resultUint8Array)!.length / 2;
//                         //this.scrollToBottom();
//                     });
//                 }, 500);
//             }, (err: any) => {
//                 console.log(`Read listener error: ${err}`);
//                 this.serialErrorLog = `${err}`;
//             });
//         });
//     }, (err: any) => {
//         console.log(`Get permission error: ${err}`);
//         this.serialErrorLog = `${err}`;
//         // if (this.openStatus) {
//         //     this.zone.run(() => {
//         //         this.openStatus = false;
//         //         this.title = this.translate.instant('SERIAL_DEVICE_TITLE');
//         //     });
//         // }
//         // this.presentToast(this.translate.instant('NO_DEVICE_CONNECTED'));
//     });
// }

//     /**
//    * Get open status
//    *
//    * @memberof HomePage
//    */
//     isOpen() {
//       usbSerialPort.isOpen((status: any) => {
//           console.log(`Serial open status: ${status}`);
//           this.serialLog = 'Open Serial - set to isOpen flag';
//           // this.zone.run(() => {
//           //     this.openStatus = status;
//           // });
//       });
//   }

  
  









//   Utf8ArrayToStr(array: string | any[]) {
//     var out, i, len, c;
//     var char2, char3;

//     out = "";
//     len = array.length;
//     i = 0;
//     while(i < len) {
//     c = array[i++];
//     switch(c >> 4)
//     { 
//       case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
//         // 0xxxxxxx
//         out += String.fromCharCode(c);
//         break;
//       case 12: case 13:
//         // 110x xxxx   10xx xxxx
//         char2 = array[i++];
//         out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
//         break;
//       case 14:
//         // 1110 xxxx  10xx xxxx  10xx xxxx
//         char2 = array[i++];
//         char3 = array[i++];
//         out += String.fromCharCode(((c & 0x0F) << 12) |
//                        ((char2 & 0x3F) << 6) |
//                        ((char3 & 0x3F) << 0));
//         break;
//     }
//     }

//     return out;
// }

//     /**
//    * Bytes to string
//    *
//    * @param {ArrayBuffer} buffer
//    * @returns
//    * @memberof Utils
//    */
// //     bytesToString(buffer: ArrayBuffer) {
// //       return String.fromCharCode.apply(null, new Uint8Array(buffer));
// //   }

//   /**
//    * String to bytes
//    *
//    * @param {string} string
//    * @returns
//    * @memberof Utils
//    */
//   stringToBytes(string: string) {
//       var array = new Uint8Array(string.length);
//       for (var i = 0, l = string.length; i < l; i++) {
//           array[i] = string.charCodeAt(i);
//       }
//       return array.buffer;
//   }

//   /**
//    * Bytes to hex string
//    *
//    * @param {Uint8Array} byte
//    * @returns
//    * @memberof Utils
//    */
// //   bytes2HexString(byte: Uint8Array) {
// //       const getBytes = (str: any) => {
// //           return str.charCodeAt(0)
// //       }
// //       let hex = "0123456789ABCDEF".split('').map(getBytes)
// //       if (!byte) return
// //       let buff = new Uint8Array(2 * byte.length);
// //       for (let i = 0; i < byte.length; i++) {
// //           buff[2 * i] = hex[(byte[i] >> 4) & 0x0f];
// //           buff[2 * i + 1] = hex[byte[i] & 0x0f];
// //       }
// //       return this.bytesToString(buff.buffer);
// //   }

//   /**
//    * Buffer to hex
//    *
//    * @param {*} buffer
//    * @returns
//    * @memberof Utils
//    */
//   bufToHex(buffer: ArrayBuffer) { // buffer is an ArrayBuffer
//       return [...new Uint8Array(buffer)]
//           .map(x => x.toString(16).padStart(2, '0'))
//           .join('');
//   }

//   /**
//    * Get string byte length
//    *
//    * @param {string} str
//    * @returns
//    * @memberof Utils
//    */
// //   getStringByteLength(str: string) {
// //       var byteLen = 0, len = str.length;
// //       if (str) {
// //           for (var i = 0; i < len; i++) {
// //               if (str.charCodeAt(i) > 255) {
// //                   byteLen += 2;
// //               }
// //               else {
// //                   byteLen++;
// //               }
// //           }
// //           return byteLen;
// //       }
// //   }

//   /**
//    * Split string by size
//    * use strDivision('ABCD',2), return 'AB CD'
//    * @param {string} str
//    * @param {number} size
//    * @returns
//    * @memberof Utils
//    */
//   strDivision(str: string, size: number) {
//       let result: any = ''
//       for (let i = 0; i < str.length; i++) {
//           result += str[i]
//           if ((i + 1) % size == 0) {
//               result += ' '
//           }
//       }
//       result = result.split(' ')
//       result = result.filter((i: any) => i)
//       return result.join(' ')
//   }

//   /**
//    * Merge multiple uint8Array
//    *
//    * @param {*} constructor
//    * @param {...any[]} arrays
//    * @returns
//    * @memberof Utils
//    */
//   concatUint(constructor: any, ...arrays: any[]) {
//       let totalLength = 0
//       for (let arr of arrays) {
//           totalLength += arr.length
//       }
//       let result = new constructor(totalLength)
//       let offset = 0
//       for (let arr of arrays) {
//           result.set(arr, offset)
//           offset += arr.length
//       }
//       return result
//   }

//   /**
//    * Format date
//    * 
//    * @param {*} date for example '2019-01-01 16:00:00' or new Date()
//    * @param {*} fmt for example 'yyyy-MM-dd hh:mm:ss'
//    * @returns
//    * @memberof Utils
//    */
// //   formatDate(date: any, fmt: string) {
// //       const padLeftZero = (str: string) => {
// //           return ('00' + str).substr(str.length)
// //       }
// //       // if date is string
// //       if (typeof date == 'string') {
// //           date = new Date(date)
// //       }
// //       if (/(y+)/.test(fmt)) {
// //           fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
// //       }
// //       let o = {
// //           'M+': date.getMonth() + 1,
// //           'd+': date.getDate(),
// //           'h+': date.getHours(),
// //           'm+': date.getMinutes(),
// //           's+': date.getSeconds()
// //       }
// //       for (let k in o) {
// //           if (new RegExp(`(${k})`).test(fmt)) {
// //               let str = o[k] + ''
// //               fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? str : padLeftZero(str))
// //           }
// //       }
// //       return fmt
// //   }

// }
