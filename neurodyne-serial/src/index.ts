import { registerPlugin } from '@capacitor/core';

import type { NeurodyneUsbSerialPlugin } from './definitions';

const NeurodyneUsbSerial = registerPlugin<NeurodyneUsbSerialPlugin>('NeurodyneUsbSerial', {
  web: () => import('./web').then(m => new m.NeurodyneUsbSerialWeb()),
});

export * from './definitions';
export { NeurodyneUsbSerial };
