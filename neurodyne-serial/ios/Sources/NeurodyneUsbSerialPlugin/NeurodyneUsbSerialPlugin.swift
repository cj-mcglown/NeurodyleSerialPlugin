import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(NeurodyneUsbSerialPlugin)
public class NeurodyneUsbSerialPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "NeurodyneUsbSerialPlugin"
    public let jsName = "NeurodyneUsbSerial"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = NeurodyneUsbSerial()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
