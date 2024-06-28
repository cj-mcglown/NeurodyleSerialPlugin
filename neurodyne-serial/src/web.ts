import { WebPlugin } from '@capacitor/core';

import type { NeurodyneUsbSerialPlugin } from './definitions';

export class NeurodyneUsbSerialWeb extends WebPlugin implements NeurodyneUsbSerialPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
