// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NeurodyneSerial",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "NeurodyneSerial",
            targets: ["NeurodyneUsbSerialPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "NeurodyneUsbSerialPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NeurodyneUsbSerialPlugin"),
        .testTarget(
            name: "NeurodyneUsbSerialPluginTests",
            dependencies: ["NeurodyneUsbSerialPlugin"],
            path: "ios/Tests/NeurodyneUsbSerialPluginTests")
    ]
)