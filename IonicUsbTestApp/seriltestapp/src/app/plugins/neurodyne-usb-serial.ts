import {registerPlugin} from '@capacitor/core';

export interface NeurodyneUsbSerialPlugin {
    echo(): Promise<{ value: string }>;
}

const UsbSerial = registerPlugin<NeurodyneUsbSerialPlugin>('NeurodyneUsbSerial');

export default UsbSerial;