import Foundation
import AppKit

class MyWindowDelegate : NSObject, NSWindowDelegate {
    func windowShouldClose(_ sender: NSWindow) -> Bool {
        NSApplication.shared.stop(self)
        return true
    }
}

class MyAppDelegate : NSObject, NSApplicationDelegate {

    private let window: NSWindow
    //let contentText: NSText
    var canClick = true
    let windowDelegate = MyWindowDelegate()

    override init() {
        let mainDisplayRect = NSScreen.main!.frame
        let windowRect =
            NSMakeRect(
                mainDisplayRect.origin.x + mainDisplayRect.size.width * 0.25,
                mainDisplayRect.origin.y + mainDisplayRect.size.height * 0.25,
                mainDisplayRect.size.width * 0.5,
                mainDisplayRect.size.height * 0.5
            )
        

        //let windowStyle: NSWindow.StyleMask = NSWindow.StyleMask.titled | NSWindow.StyleMask.miniaturizable | NSWindow.StyleMask.closable | NSWindow.StyleMask.resizable
        let windowStyle = NSWindow.StyleMask.init(arrayLiteral: [NSWindow.StyleMask.titled, NSWindow.StyleMask.miniaturizable, NSWindow.StyleMask.closable, NSWindow.StyleMask.resizable])
        
        window = NSWindow(contentRect: windowRect, styleMask: windowStyle, backing: NSWindow.BackingStoreType.buffered, defer: false)
        window.title = "Title"
        window.isOpaque = true
        window.hasShadow = true
        //window.preferredBackingLocation = NSWindow.BackingLocation.videoMemory
        window.hidesOnDeactivate = false
        window.backgroundColor = NSColor.gray
        window.isReleasedWhenClosed = false
        window.delegate = windowDelegate

        /*
        let buttonPress = NSButton(frame: NSMakeRect(10.0, 10.0, 100.0, 40.0))
        buttonPress.title = "Click"
        //buttonPress.target = controller
        buttonPress.action = NSSelectorFromString("onClick")
        window.contentView!.addSubview(buttonPress)
        let buttonQuit = NSButton(frame: NSMakeRect(120.0, 10.0, 100.0, 40.0))
        buttonQuit.title = "Quit"
        //buttonQuit.target = controller
        buttonQuit.action = NSSelectorFromString("onQuit")
        window.contentView!.addSubview(buttonQuit)

        let buttonRequest = NSButton(frame: NSMakeRect(230.0, 10.0, 100.0, 40.0))
        buttonRequest.title = "Request"
        //buttonRequest.target = controller
        buttonRequest.action = NSSelectorFromString("onRequest")
        window.contentView!.addSubview(buttonRequest)

        let contentText = NSText(frame: NSMakeRect(10.0, 80.0, 600.0, 350.0))
        contentText.string = "Press 'Click' to start fetching"
        contentText.isVerticallyResizable = false
        contentText.isHorizontallyResizable = false

        window.contentView!.addSubview(contentText)
        */
        
        //window.setIsVisible(true)
    }
    
    func applicationWillFinishLaunching(_ notification: Notification) {
        window.makeKeyAndOrderFront(self)
    }
}


let appDelegate = MyAppDelegate()

autoreleasepool {
    let app = NSApplication.shared
    app.delegate = appDelegate
    app.setActivationPolicy(NSApplication.ActivationPolicy.regular)
    app.activate(ignoringOtherApps: true)

    app.run()
}

