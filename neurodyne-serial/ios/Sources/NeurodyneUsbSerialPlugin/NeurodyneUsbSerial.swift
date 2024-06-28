import Foundation

@objc public class NeurodyneUsbSerial: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
