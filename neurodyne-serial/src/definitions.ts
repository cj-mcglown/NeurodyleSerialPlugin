export interface NeurodyneUsbSerialPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
