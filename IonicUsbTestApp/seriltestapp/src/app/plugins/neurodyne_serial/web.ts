import { WebPlugin } from '@capacitor/core';

import type { UsbSerialOptions, NeurodyneUsbSerialPlugin, IsKeptAwakeResult, IsSupportedResult, GetBrightnessReturnValue, SetBrightnessOptions } from './definitions';

export class NeurodyneUsbSerialWeb extends WebPlugin implements NeurodyneUsbSerialPlugin {
  private wakeLock: WakeLockSentinel | null = null;
  private readonly _isSupported = 'wakeLock' in navigator;

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
  connectedDevices(): Promise<{ devices: [] }> {
    throw new Error('Method not implemented.');
  }
  openSerial(options: UsbSerialOptions): Promise<void> {
    throw new Error('Method not implemented: ' + JSON.stringify(options));
  }
  closeSerial(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  readSerial(): Promise<{ data: string }> {
    throw new Error('Method not implemented.');
  }
  writeSerial(options: { data: string }): Promise<void> {
    throw new Error('Method not implemented: ' + JSON.stringify(options));
  }
  override addListener(
    eventName: 'log' | 'connected' | 'attached' | 'detached' | 'data' | 'error',
    listenerFunc: (data: any) => void
  ) {
    listenerFunc({});
    return Promise.reject(`Method '${eventName}' not implemented.`) as any;
  }

  setBrightness(options: SetBrightnessOptions): Promise<void> {
    throw new Error('Method not implemented: ' + JSON.stringify(options));
  }
  getBrightness(): Promise<GetBrightnessReturnValue>{
    throw new Error('Method not implemented: ');
  }

  public async keepAwake(): Promise<void> {
    if (!this._isSupported) {
      this.throwUnsupportedError();
    }
    if (this.wakeLock) {
      await this.allowSleep();
    }
    this.wakeLock = await navigator.wakeLock.request('screen');
  }

  public async allowSleep(): Promise<void> {
    if (!this._isSupported) {
      this.throwUnsupportedError();
    }
    this.wakeLock?.release();
    this.wakeLock = null;
  }

  public async isSupported(): Promise<IsSupportedResult> {
    const result = {
      isSupported: this._isSupported,
    };
    return result;
  }

  public async isKeptAwake(): Promise<IsKeptAwakeResult> {
    if (!this._isSupported) {
      this.throwUnsupportedError();
    }
    const result = {
      isKeptAwake: !!this.wakeLock,
    };
    return result;
  }

  private throwUnsupportedError(): never {
    throw this.unavailable(
      'Screen Wake Lock API not available in this browser.',
    );
  } 
}




