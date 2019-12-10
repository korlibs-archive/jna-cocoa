// Must run with: -XstartOnFirstThread
fun main(args: Array<String>) {
    // https://indiestack.com/2016/12/touch-bar-crash-protection/
    //[[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"NSFunctionBarAPIEnabled"]];
    NSClass("NSUserDefaults").msgSend("standardUserDefaults").msgSend(
        "registerDefaults:",
        NSClass("NSDictionary").msgSend(
            "dictionaryWithObject:forKey:",
            NSClass("NSNumber").msgSend("numberWithBool:", 0),
            NSString("NSFunctionBarAPIEnabled")
        )
    )

    val isMainThread = NSClass("NSThread").msgSend("isMainThread") != 0L
    if (!isMainThread) {
        error("Can't use this. Since we are not in the main thread!")
    }

    val autoreleasePool = NSClass("NSAutoreleasePool").alloc().msgSend("init")

    val app = NSClass("NSApplication").msgSend("sharedApplication")
    val sharedApp = app

    app.msgSend("setActivationPolicy:", 0)
    // ObjectiveC.objc_lookUpClass("NSApplication")
    val AppDelegateClass = AllocateClassAndRegister("AppDelegate", "NSObject", "NSApplicationDelegate") {
        addMethod("applicationShouldTerminate:", applicationShouldTerminateCallback, "@:@")
    }

    //println("AppDelegateClass: $AppDelegateClass")
    //println("NSApplicationDelegate: $NSApplicationDelegate")
    //println(ObjectiveC.class_conformsToProtocol(AppDelegateClass, NSApplicationDelegate))

    val appDelegate = AppDelegateClass.alloc().msgSend("init")
    app.msgSend("setDelegate:", appDelegate)
    app.msgSend("finishLaunching")

    val processName = NSString(NSClass("NSProcessInfo").msgSend("processInfo").msgSend("processName"))

    app.msgSend("setMainMenu:", NSMenu {
        addItem(NSMenuItem {
            setSubmenu(NSMenu {
                addItem(NSMenuItem("Quit $processName", "terminate:", "q"))
            })
        })
    }.id)

    val rect = NSRect(0, 0, 500, 500)
    val MyNsWindow = AllocateClassAndRegister("MyNSWindow", "NSWindow")

    val window = MyNsWindow.alloc().msgSend(
        "initWithContentRect:styleMask:backing:defer:",
        rect,
        NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskMiniaturizable or NSWindowStyleMaskResizable or NSWindowStyleMaskResizable,
        NSBackingStoreBuffered,
        false
    )

    //window.msgSend("styleMask", window.msgSend("styleMask").toInt() or NSWindowStyleMaskFullScreen)

    window.msgSend("setReleasedWhenClosed:", 0L)

    window.msgSend("cascadeTopLeftFromPoint:", NSPoint(20, 20))
    window.msgSend("setTitle:", NSString("sup from Java"))

    val pixelFormat = NSClass("NSOpenGLPixelFormat").alloc().msgSend(
        "initWithAttributes:", intArrayOf(
            8, 24,
            11, 8,
            5,
            73,
            72,
            55, 1,
            56, 4,
            //99, 0x1000, // or 0x3200
            99, 0x3200,
            0
        )
    )
    val openGLContext = NSClass("NSOpenGLContext").alloc().msgSend("initWithFormat:shareContext:", pixelFormat, null)
    val contentView = window.msgSend("contentView")
    openGLContext.msgSend("setView:", contentView)
    println("contentView: $contentView")
    contentView.msgSend("setWantsBestResolutionOpenGLSurface:", true)

    //val openglView = NSClass("NSOpenGLView").alloc().msgSend("initWithFrame:pixelFormat:", MyNativeNSRect.ByValue(0, 0, 512, 512), pixelFormat)
    //val openGLContext = openglView.msgSend("openGLContext")
    //window.msgSend("contentView", openglView)
    //val contentView = window.msgSend("contentView")

    println("pixelFormat: $pixelFormat")
    println("openGLContext: $openGLContext")

    window.msgSend("setAcceptsMouseMovedEvents:", true)
    window.msgSend("setBackgroundColor:", NSClass("NSColor").msgSend("blackColor"))
    window.msgSend("makeKeyAndOrderFront:", app)
    window.msgSend("center")

    app.msgSend("activateIgnoringOtherApps:", true)

    window.msgSend("makeKeyWindow")
    window.msgSend("setIsVisible:", true)

    //contentView.msgSend("fullScreenEnable")

    //val NSApp = Foundation.NATIVE.getGlobalVariableAddress("NSApp")
    val NSDefaultRunLoopMode = Foundation.NATIVE.getGlobalVariableAddress("NSDefaultRunLoopMode")
    //val NSDefaultRunLoopMode = Foundation.NATIVE.getGlobalVariableAddress("NSDefaultRunLoopMode")
    println("NSDefaultRunLoopMode: $NSDefaultRunLoopMode")


    fun renderOpengl() {
        val rect = MyNativeNSRect()
        window.msgSend_stret(rect, "frame")

        openGLContext.msgSend("makeCurrentContext")
        GL.glViewport(0, 0, rect.width.toInt(), rect.height.toInt())
        GL.glClearColor(.3f, .7f, 1f, 1f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        openGLContext.msgSend("flushBuffer")
    }

    val mouseEvent = ObjcCallbackVoid { self, _sel, sender ->
        val point = sender.msgSendNSPoint("locationInWindow")
        val buttonNumber = sender.msgSend("buttonNumber")
        val clickCount = sender.msgSend("clickCount")

        val rect = MyNativeNSRect()
        contentView.msgSend_stret(rect, "frame")

        val rect2 = MyNativeNSRect()
        window.msgSend_stret(rect2, "frame")

        val rect3 = MyNativeNSRect()
        window.msgSend_stret(rect3, "contentRectForFrameRect:", rect2)

        val dims = intArrayOf(720, 480)
        GL.CGLSetParameter(openGLContext, 304, dims)
        GL.CGLEnable(openGLContext, 304)

        val point2 = NSPoint(point.x, rect.height - point.y)

        //val res = NSClass("NSEvent").id.msgSend_stret(data, "mouseLocation")

        val selName = ObjectiveC.sel_getName(_sel)

        println("MOUSE EVENT ($selName) from NSWindow! $point2 : $buttonNumber : $clickCount")
    }

    ObjectiveC.class_addMethod(MyNsWindow, sel("mouseEntered:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("mouseExited:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("mouseDragged:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("mouseMoved:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("mouseDown:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("mouseUp:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("rightMouseDragged:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("rightMouseMoved:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("rightMouseDown:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("rightMouseUp:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("otherMouseDragged:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("otherMouseMoved:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("otherMouseDown:"), mouseEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("otherMouseUp:"), mouseEvent, "v@:@")

    val keyEvent = ObjcCallbackVoid { self, _sel, sender ->
        val selName = ObjectiveC.sel_getName(_sel)
        val characters = NSString(sender.msgSend("characters")).toString()
        val charactersIgnoringModifiers = NSString(sender.msgSend("charactersIgnoringModifiers")).toString()
        val char = charactersIgnoringModifiers.getOrNull(0) ?: '\u0000'
        val keyCode = sender.msgSend("keyCode").toInt()

        val key = KeyCodesToKeys[keyCode] ?: CharToKeys[char] ?: Key.UNKNOWN

        println("keyDown: $selName : $characters : ${char.toInt()} : $charactersIgnoringModifiers : $keyCode : $key")
    }

    ObjectiveC.class_addMethod(MyNsWindow, sel("keyDown:"), keyEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("keyUp:"), keyEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("keyPress:"), keyEvent, "v@:@")
    ObjectiveC.class_addMethod(MyNsWindow, sel("flagsChanged:"), ObjcCallbackVoid { self, _sel, sender ->
        val modifierFlags = sender.msgSend("modifierFlags")
        println("flags changed! : $modifierFlags")
    }, "v@:@")
    window.msgSend("setAcceptsMouseMovedEvents:", true)

    /*
    val eventHandler = object : ObjcCallback {
        override fun invoke(self: Long, _sel: Long, sender: Long): Long {
            val point = NSPoint()

            val senderName = ObjectiveC.class_getName(ObjectiveC.object_getClass(sender))
            //val res = sender.msgSend_stret(data, "mouseLocation:")
            //println("Mouse moved! $self, $_sel, $sender : $senderName : ${ObjectiveC.sel_getName(_sel)} : $res : $point : $data : ${sender.msgSend("buttonNumber")} : ${sender.msgSend("clickCount")}")

            renderOpengl()
            return 0L
        }
    }
val MyResponderClass = AllocateClass("MyResponder", "NSObject", "NSResponder")
//ObjectiveC.class_addMethod(MyResponderClass, sel("mouseDragged:"), eventHandler, "v@:@")
//ObjectiveC.class_addMethod(MyResponderClass, sel("mouseUp:"), eventHandler, "v@:@")
//ObjectiveC.class_addMethod(MyResponderClass, sel("mouseDown:"), eventHandler , "v@:@")
ObjectiveC.class_addMethod(MyResponderClass, sel("mouseMoved:"), eventHandler, "v@:@")
val Responder = MyResponderClass.alloc().msgSend("init")
window.msgSend("setNextResponder:", Responder)
*/

    val WindowDelegate = AllocateClassAndRegister("WindowDelegate", "NSObject", "NSWindowDelegate") {
        val NSWindowDelegate = ObjectiveC.objc_getProtocol("NSWindowDelegate")
        println("NSWindowDelegate: $NSWindowDelegate")
        addMethod("windowWillClose:", windowWillClose, "v@:@")
        addMethod("windowDidExpose:", ObjcCallbackVoid { self, _sel, notification ->
            //println("windowDidExpose")
            renderOpengl()
        }, "v@:@")
        addMethod("windowDidUpdate:", ObjcCallbackVoid { self, _sel, notification ->
            //println("windowDidUpdate")
            renderOpengl()
        }, "v@:@")
        addMethod("windowDidResize:", ObjcCallbackVoid { self, _sel, notification ->
            val rect = MyNativeNSRect()
            window.msgSend_stret(rect, "frame")
            openGLContext.msgSend("clearDrawable")
            contentView.msgSend("setBoundsSize:", MyNativeNSPoint.ByValue(rect.width, rect.height))
            openGLContext.msgSend("setView:", contentView)
            renderOpengl()
        }, "v@:@")
    }

    val Delegate = WindowDelegate.alloc().msgSend("init")
    window.msgSend("setDelegate:", Delegate)

    app.msgSend("run")

    autoreleasePool.msgSend("drain")
}

internal val KeyCodesToKeys = mapOf(
    0x24 to Key.ENTER,
    0x4C to Key.ENTER,
    0x30 to Key.TAB,
    0x31 to Key.SPACE,
    0x33 to Key.DELETE,
    0x35 to Key.ESCAPE,
    0x37 to Key.META,
    0x38 to Key.LEFT_SHIFT,
    0x39 to Key.CAPS_LOCK,
    0x3A to Key.LEFT_ALT,
    0x3B to Key.LEFT_CONTROL,
    0x3C to Key.RIGHT_SHIFT,
    0x3D to Key.RIGHT_ALT,
    0x3E to Key.RIGHT_CONTROL,
    0x7B to Key.LEFT,
    0x7C to Key.RIGHT,
    0x7D to Key.DOWN,
    0x7E to Key.UP,
    0x48 to Key.VOLUME_UP,
    0x49 to Key.VOLUME_DOWN,
    0x4A to Key.MUTE,
    0x72 to Key.HELP,
    0x73 to Key.HOME,
    0x74 to Key.PAGE_UP,
    0x75 to Key.DELETE,
    0x77 to Key.END,
    0x79 to Key.PAGE_DOWN,
    0x3F to Key.FUNCTION,
    0x7A to Key.F1,
    0x78 to Key.F2,
    0x76 to Key.F4,
    0x60 to Key.F5,
    0x61 to Key.F6,
    0x62 to Key.F7,
    0x63 to Key.F3,
    0x64 to Key.F8,
    0x65 to Key.F9,
    0x6D to Key.F10,
    0x67 to Key.F11,
    0x6F to Key.F12,
    0x69 to Key.F13,
    0x6B to Key.F14,
    0x71 to Key.F15,
    0x6A to Key.F16,
    0x40 to Key.F17,
    0x4F to Key.F18,
    0x50 to Key.F19,
    0x5A to Key.F20
)

internal val CharToKeys = mapOf(
    'a' to Key.A, 'A' to Key.A,
    'b' to Key.B, 'B' to Key.B,
    'c' to Key.C, 'C' to Key.C,
    'd' to Key.D, 'D' to Key.D,
    'e' to Key.E, 'E' to Key.E,
    'f' to Key.F, 'F' to Key.F,
    'g' to Key.G, 'G' to Key.G,
    'h' to Key.H, 'H' to Key.H,
    'i' to Key.I, 'I' to Key.I,
    'j' to Key.J, 'J' to Key.J,
    'k' to Key.K, 'K' to Key.K,
    'l' to Key.L, 'L' to Key.L,
    'm' to Key.M, 'M' to Key.M,
    'n' to Key.N, 'N' to Key.N,
    'o' to Key.O, 'O' to Key.O,
    'p' to Key.P, 'P' to Key.P,
    'q' to Key.Q, 'Q' to Key.Q,
    'r' to Key.R, 'R' to Key.R,
    's' to Key.S, 'S' to Key.S,
    't' to Key.T, 'T' to Key.T,
    'u' to Key.U, 'U' to Key.U,
    'v' to Key.V, 'V' to Key.V,
    'w' to Key.W, 'W' to Key.W,
    'x' to Key.X, 'X' to Key.X,
    'y' to Key.Y, 'Y' to Key.Y,
    'z' to Key.Z, 'Z' to Key.Z,
    '0' to Key.N0, '1' to Key.N1, '2' to Key.N2, '3' to Key.N3, '4' to Key.N4,
    '5' to Key.N5, '6' to Key.N6, '7' to Key.N7, '8' to Key.N8, '9' to Key.N9
)

enum class Key {
    SPACE, APOSTROPHE, COMMA, MINUS, PERIOD, SLASH,
    N0, N1, N2, N3, N4, N5, N6, N7, N8, N9,
    SEMICOLON, EQUAL,
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
    LEFT_BRACKET, BACKSLASH, RIGHT_BRACKET, GRAVE_ACCENT,
    WORLD_1, WORLD_2,
    ESCAPE,
    META,
    ENTER, TAB, BACKSPACE, INSERT, DELETE,
    RIGHT, LEFT, DOWN, UP,
    PAGE_UP, PAGE_DOWN, FUNCTION, HELP, MUTE, VOLUME_DOWN, VOLUME_UP,
    HOME, END,
    CAPS_LOCK, SCROLL_LOCK, NUM_LOCK,
    PRINT_SCREEN, PAUSE,
    F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
    F13, F14, F15, F16, F17, F18, F19, F20, F21, F22, F23, F24, F25,
    KP_0, KP_1, KP_2, KP_3, KP_4, KP_5, KP_6, KP_7, KP_8, KP_9,
    KP_DECIMAL, KP_DIVIDE, KP_MULTIPLY,
    KP_SUBTRACT, KP_ADD, KP_ENTER, KP_EQUAL, KP_SEPARATOR,
    LEFT_SHIFT, LEFT_CONTROL, LEFT_ALT, LEFT_SUPER,
    RIGHT_SHIFT, RIGHT_CONTROL, RIGHT_ALT, RIGHT_SUPER,
    MENU,

    BACKQUOTE, QUOTE,

    KP_UP, KP_DOWN, KP_LEFT, KP_RIGHT,

    UNDERLINE, SELECT_KEY,

    CANCEL, CLEAR,

    OPEN_BRACKET, CLOSE_BRACKET,

    UNDEFINED,

    UNKNOWN;

    companion object {
        val MAX = UNKNOWN.ordinal + 1

        val NUMPAD0 = N0
        val NUMPAD1 = N1
        val NUMPAD2 = N2
        val NUMPAD3 = N3
        val NUMPAD4 = N4
        val NUMPAD5 = N5
        val NUMPAD6 = N6
        val NUMPAD7 = N7
        val NUMPAD8 = N8
        val NUMPAD9 = N9
    }
}

// -XstartOnFirstThread
fun main2(args: Array<String>) {
    val pool = NSClass("NSAutoreleasePool").alloc().msgSend("init")
    val sharedApp = NSClass("NSApplication").msgSend("sharedApplication")

    //val rect = Foundation.NSMakeRect(CGFloat(0), CGFloat(0), CGFloat(500), CGFloat(500))
    val frame = NSRect(0, 0, 500, 500)
    val window = NSClass("NSWindow").alloc().msgSend("initWithContentRect:styleMask:backing:defer:", frame, 0, 0, 0)
    window.msgSend("setBackgroundColor:", NSClass("NSColor").msgSend("blueColor"))
    window.msgSend("makeKeyAndOrderFront:", sharedApp)

    sharedApp.msgSend("run")

    //println(rect)
    /*
    val frame = NSMakeRect(0, 0, 500, 500);
    NSWindow* window  = [[[NSWindow alloc] initWithContentRect:frame
        styleMask:NSBorderlessWindowMask
    backing:NSBackingStoreBuffered
    defer:NO] autorelease];
    [window setBackgroundColor:[NSColor blueColor]];
    [window makeKeyAndOrderFront:NSApp];

    //AppDelegate *appDelegate = [[AppDelegate alloc] init];
    //[NSApp setDelegate:appDelegate];
    [NSApp run];
    [pool release];
     */
}

val NSWindowStyleMaskTitled = 1 shl 0
val NSWindowStyleMaskClosable = 1 shl 1
val NSWindowStyleMaskMiniaturizable = 1 shl 2
val NSWindowStyleMaskResizable = 1 shl 3
val NSWindowStyleMaskFullScreen = 1 shl 14
val NSWindowStyleMaskFullSizeContentView = 1 shl 15

val NSBackingStoreBuffered = 2

inline class NSMenuItem(val id: Long) {
    constructor() : this(NSClass("NSMenuItem").alloc().msgSend("init"))
    constructor(text: String, sel: String, keyEquivalent: String) : this(
        NSClass("NSMenuItem").alloc().msgSend(
            "initWithTitle:action:keyEquivalent:",
            NSString(text).id,
            sel(sel),
            NSString(keyEquivalent).id
        ).autorelease()
    )

    companion object {
        operator fun invoke(callback: NSMenuItem.() -> Unit) = NSMenuItem().apply(callback)
    }

    fun setSubmenu(menu: NSMenu) {
        id.msgSend("setSubmenu:", menu.id)
    }
}

inline class NSMenu(val id: Long) {
    constructor() : this(NSClass("NSMenu").alloc().msgSend("init"))

    companion object {
        operator fun invoke(callback: NSMenu.() -> Unit) = NSMenu().apply(callback)
    }

    fun addItem(menuItem: NSMenuItem) {
        id.msgSend("addItem:", menuItem.id)
    }
}