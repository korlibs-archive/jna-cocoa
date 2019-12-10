import Cocoa

class AppDelegate: NSObject, NSApplicationDelegate {
    var newWindow: NSWindow?
    var controller: ViewController?

    func applicationDidFinishLaunching(aNotification: NSNotification) {
        newWindow = NSWindow(contentRect: NSMakeRect(10, 10, 300, 300), styleMask: .resizable, backing: .buffered, defer: false)

        controller = ViewController()
        let content = newWindow!.contentView! as NSView
        let view = controller!.view
        content.addSubview(view)

        newWindow!.makeKeyAndOrderFront(nil)
    }
}

class ViewController : NSViewController {
    override func loadView() {
        let view = NSView(frame: NSMakeRect(0,0,100,100))
        view.wantsLayer = true
        view.layer?.borderWidth = 2
        view.layer?.borderColor = NSColor.red.cgColor
        self.view = view
    }
}

let delegate = AppDelegate() //alloc main app's delegate class
NSApplication.shared.delegate = delegate //set as app's delegate

// Old versions:
// NSApplicationMain(C_ARGC, C_ARGV)
//NSApplicationMain(Process.argc, Process.unsafeArgv);  //start of run loop
NSApplicationMain(CommandLine.argc, CommandLine.unsafeArgv);  //start of run loop

