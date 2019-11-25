import com.sun.jna.*

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

fun main(args: Array<String>) {
    val autoreleasePool = NSClass("NSAutoreleasePool").alloc().msgSend("init")

    val app = NSClass("NSApplication").msgSend("sharedApplication")
    val sharedApp = app

    if (true) {
        app.msgSend("setActivationPolicy:", 0)
        val AppDelegateClass = ObjectiveC.objc_allocateClassPair(NSObject.OBJ_CLASS, "AppDelegate", 0)
        val NSApplicationDelegate = ObjectiveC.objc_getProtocol("NSApplicationDelegate")
        ObjectiveC.class_addProtocol(AppDelegateClass, NSApplicationDelegate)
        ObjectiveC.class_addMethod(AppDelegateClass, sel("applicationShouldTerminate:"), applicationShouldTerminateCallback, "@:@");
        val appDelegate = AppDelegateClass.alloc().msgSend("init")
        app.msgSend("setDelegate:", appDelegate)
        app.msgSend("finishLaunching")

        val menubar = NSClass("NSMenu").alloc().msgSend("init")
        val appMenuItem = NSClass("NSMenuItem").alloc().msgSend("init")
        menubar.msgSend("addItem:", appMenuItem)
        app.msgSend("setMainMenu:", menubar)

        ///////////////////

        val processName = NSString(NSClass("NSProcessInfo").msgSend("processInfo").msgSend("processName"))

        var a: NSRect

        val appMenu = NSClass("NSMenu").alloc().msgSend("init")
        val quitMenuItem = NSClass("NSMenuItem").alloc()
            .msgSend("initWithTitle:action:keyEquivalent:", NSString("Quit $processName").id, sel("terminate:"), NSString("q").id)
        quitMenuItem.msgSend("autorelease")
        appMenu.msgSend("addItem:", quitMenuItem)
        appMenuItem.msgSend("setSubmenu:", appMenu)

        val rect = NSRect(0, 0, 500, 500)
        val MyNsWindow = AllocateClass("MyNSWindow", "NSWindow")

        val window = MyNsWindow.alloc().msgSend("initWithContentRect:styleMask:backing:defer:", rect, NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskMiniaturizable or NSWindowStyleMaskResizable or NSWindowStyleMaskResizable or NSWindowStyleMaskFullSizeContentView, 2, 0)

        //window.msgSend("styleMask", window.msgSend("styleMask").toInt() or NSWindowStyleMaskFullScreen)

        window.msgSend("setReleasedWhenClosed:", 0L)

        val contentView = window.msgSend("contentView")
        println("contentView: $contentView")

        contentView.msgSend("setWantsBestResolutionOpenGLSurface:", true)

        window.msgSend("cascadeTopLeftFromPoint:", NSPoint(20, 20))
        window.msgSend("setTitle:", NSString("sup from Java"))

        val glAttributes = intArrayOf(
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

        val pixelFormat = NSClass("NSOpenGLPixelFormat").alloc().msgSend("initWithAttributes:", glAttributes)
        val openGLContext = NSClass("NSOpenGLContext").alloc().msgSend("initWithFormat:shareContext:", pixelFormat, null)
        println("pixelFormat: $pixelFormat")
        println("openGLContext: $openGLContext")
        openGLContext.msgSend("setView:", contentView)
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
            contentView.msgSend_stret(rect, "bounds")
            println("RECT: $rect")
            openGLContext.msgSend("makeCurrentContext")
            GL.glViewport(0, 0, rect.width.toInt(), rect.height.toInt())
            GL.glClearColor(.3f, .7f, 1f, 1f)
            GL.glClear(GL.GL_COLOR_BUFFER_BIT)
            openGLContext.msgSend("flushBuffer")

        }

        ObjectiveC.class_addMethod(MyNsWindow, sel("mouseMoved:"), ObjcCallback { self, _sel, sender ->
            val data = MyNativeNSRect()
            val res = sender.msgSend_stret(data.pointer, "locationInWindow")
            //val res = NSClass("NSEvent").id.msgSend_stret(data, "mouseLocation")

            println("MOUSE MOVED from NSWindow! $data")
            renderOpengl()
            0L
        }, "v@:@")


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

        /*
        val MyResponderClass = AllocateClass("MyResponder", "NSObject", "NSResponder")
        //ObjectiveC.class_addMethod(MyResponderClass, sel("mouseDragged:"), eventHandler, "v@:@")
        //ObjectiveC.class_addMethod(MyResponderClass, sel("mouseUp:"), eventHandler, "v@:@")
        //ObjectiveC.class_addMethod(MyResponderClass, sel("mouseDown:"), eventHandler , "v@:@")
        ObjectiveC.class_addMethod(MyResponderClass, sel("mouseMoved:"), eventHandler, "v@:@")
        val Responder = MyResponderClass.alloc().msgSend("init")
        window.msgSend("setNextResponder:", Responder)
        */

        val WindowDelegate = AllocateClass("WindowDelegate", "NSObject", "NSWindowDelegate")
        ObjectiveC.class_addMethod(WindowDelegate, sel("windowWillClose:"), windowWillClose, "v@:@")
        ObjectiveC.class_addMethod(WindowDelegate, sel("windowDidResize:"), ObjcCallback { self, _sel, notification ->
            println("windowDidResize")
            renderOpengl()
            0L
        }, "v@:@")

        val Delegate = WindowDelegate.alloc().msgSend("init")
        window.msgSend("setDelegate:", Delegate)


        //println("NSDefaultRunLoopMode: ${ObjectiveC.NATIVE.getGlobalVariableAddress("NSDefaultRunLoopMode")}")

        //app.msgSend("run")

        /*
        while (true) {
            val distantPast = NSClass("NSDate").msgSend("distantPast")
            val event = app.msgSend("nextEventMatchingMask:untilDate:inMode:dequeue:", -1L, distantPast, NSDefaultRunLoopMode.getLong(0L), true)

            if (event != 0L) {
                //println("event: $event")
                println("event: $event")
            }

            openGLContext.msgSend("update")
            openGLContext.msgSend("makeCurrentContext")
            val rect = NSRect()

            //println(rect.pointer)
            contentView.msgSend_stret(rect.pointer, "frame")
            //val rect = contentView.msgSend("frame")
            //println("rect: $rect")

            app.msgSend("sendEvent:", event)
            app.msgSend("updateWindows")
            openGLContext.msgSend("flushBuffer")
            //id event = ((id (*)(id, SEL, NSUInteger, id, id, BOOL))objc_msgSend)(NSApp, nextEventMatchingMaskSel, NSUIntegerMax, distantPast, NSDefaultRunLoopMode, YES);
        }
        */

        //app.msgSend("updateWindows")
    }


    app.msgSend("run")

    autoreleasePool.msgSend("drain")
}

//inline class ID(val id: Long)
typealias ID = Long

annotation class NativeName(val name: String) {
    companion object {
        val OPTIONS = mapOf(
            Library.OPTION_FUNCTION_MAPPER to FunctionMapper { _, method ->
                method.getAnnotation(NativeName::class.java)?.name ?: method.name
            }
        )
    }
}

typealias NSRectPtr = Pointer

inline fun <reified T : Library> NativeLoad(name: String) = Native.load(name, T::class.java, NativeName.OPTIONS) as T

interface GL : Library {
    fun glViewport(x: Int, y: Int, width: Int, height: Int)
    fun glClearColor(r: Float, g: Float, b: Float, a: Float)
    fun glClear(flags: Int)
    companion object : GL by NativeLoad("OpenGL") {
        const val GL_COLOR_BUFFER_BIT = 0x00004000
        val NATIVE = NativeLibrary.getInstance("OpenGL")
    }

}

interface ObjectiveC : Library {
    fun objc_getClass(name: String): Long
    fun objc_getProtocol(name: String): Long

    fun class_addProtocol(a: Long, b: Long): Long

    fun objc_msgSend(vararg args: Any?): Long
    fun objc_msgSend_stret(structPtr: Any?, vararg args: Any?): Unit
    /*
    fun objc_msgSend(a: Long, b: Long): Long
    fun objc_msgSend(a: Long, b: Long, c: Long): Long
    fun objc_msgSend(a: Long, b: Long, c: String): Long
    fun objc_msgSend(a: Long, b: Long, c: ByteArray, d: Int, e: Int): Long
    fun objc_msgSend(a: Long, b: Long, c: ByteArray, len: Int): Long
    fun objc_msgSend(a: Long, b: Long, c: CharArray, len: Int): Long
     */
    fun sel_registerName(name: String): Long

    fun sel_getName(sel: Long): String
    fun objc_allocateClassPair(clazz: Long, name: String, extraBytes: Int): Long
    fun object_getIvar(obj: Long, ivar: Long): Long

    fun class_getInstanceVariable(clazz: ID, name: String): ID
    fun class_getProperty(clazz: ID, name: String): ID

    fun class_addMethod(cls: Long, name: Long, imp: Callback, types: String): Long

    fun object_getClass(obj: ID): ID
    fun class_getName(clazz: ID): String

    fun property_getName(prop: ID): String
    fun property_getAttributes(prop: ID): String

    companion object : ObjectiveC by NativeLoad("objc") {
        val NATIVE = NativeLibrary.getInstance("objc")
    }
}

fun AllocateClass(name: String, base: String, vararg protocols: String): Long {
    val clazz = ObjectiveC.objc_allocateClassPair(ObjectiveC.objc_getClass(base), name, 0)
    for (protocol in protocols) {
        ObjectiveC.class_addProtocol(clazz, ObjectiveC.objc_getProtocol(protocol))
    }
    return clazz
}

interface Foundation : Library {
    fun NSLog(msg: Long): Unit
    fun NSMakeRect(x: CGFloat, y: CGFloat, w: CGFloat, h: CGFloat): NSRect

    //companion object : Foundation by Native.load("/System/Library/Frameworks/Foundation.framework/Versions/C/Foundation", Foundation::class.java) as Foundation
    companion object : Foundation by Native.load("Foundation", Foundation::class.java, NativeName.OPTIONS) as Foundation {
        val NATIVE = NativeLibrary.getInstance("Foundation")
    }
}

interface Cocoa : Library {
    companion object : Cocoa by Native.load("Cocoa", Cocoa::class.java, NativeName.OPTIONS) as Cocoa {
        val NATIVE = NativeLibrary.getInstance("Cocoa")
    }
}

interface AppKit : Library {
    companion object : AppKit by Native.load("AppKit", AppKit::class.java, NativeName.OPTIONS) as AppKit {
        val NATIVE = NativeLibrary.getInstance("AppKit")
        val NSApp = NATIVE.getGlobalVariableAddress("NSApp").getLong(0L)
    }
}

fun Foundation.NSLog(msg: NSString) = NSLog(msg.id)
fun Foundation.NSLog(msg: String) = NSLog(NSString(msg))

fun sel(name: String) = ObjectiveC.sel_registerName(name)
fun Long.msgSend(sel: String, vararg args: Any?): Long = ObjectiveC.objc_msgSend(this, sel(sel), *args)
fun Long.msgSend_stret(output: Any?, sel: String, vararg args: Any?): Unit = ObjectiveC.objc_msgSend_stret(output, this, sel(sel), *args)
operator fun Long.invoke(sel: String, vararg args: Any?): Long = ObjectiveC.objc_msgSend(this, sel(sel), *args)

open class NSObject(val id: Long) : IntegerType(8, id, false), NativeMapped {
    fun msgSend(sel: String, vararg args: Any?): Long = ObjectiveC.objc_msgSend(id, sel(sel), *args)
    fun alloc(): Long = msgSend("alloc")

    companion object : NSClass("NSObject") {
    }

    override fun toByte(): Byte = id.toByte()
    override fun toChar(): Char = id.toChar()
    override fun toShort(): Short = id.toShort()
    override fun toInt(): Int = id.toInt()
    override fun toLong(): Long = id

    override fun toNative(): Any = this.id

    override fun fromNative(nativeValue: Any, context: FromNativeContext?): Any = NSObject((nativeValue as Number).toLong())
    override fun nativeType(): Class<*> = Long::class.javaPrimitiveType!!
}

open class NSString(id: Long) : NSObject(id) {
    constructor() : this("")
    constructor(str: String) : this(OBJ_CLASS.msgSend("alloc").msgSend("initWithCharacters:length:", str.toCharArray(), str.length))

    //val length: Int get() = ObjectiveC.object_getIvar(this.id, LENGTH_ivar).toInt()
    val length: Int get() = this.msgSend("length").toInt()

    val cString: String
        get() {
            val length = this.length
            val ba = ByteArray(length + 1)
            msgSend("getCString:maxLength:encoding:", ba, length + 1, 4)
            val str = ba.toString(Charsets.UTF_8)
            return str.substring(0, str.length - 1)
        }

    override fun toString(): String = cString

    companion object : NSClass("NSString") {
        val LENGTH_ivar = ObjectiveC.class_getProperty(OBJ_CLASS, "length")
    }
}

open class NSClass(val name: String) : NSObject(ObjectiveC.objc_getClass(name)) {
    val OBJ_CLASS = id
}

class NSApplication(id: Long) : NSObject(id) {
    fun setActivationPolicy(value: Int) = id.msgSend("setActivationPolicy:", value.toLong())

    companion object : NSClass("NSApplication") {
        fun sharedApplication(): NSApplication = NSApplication(OBJ_CLASS.msgSend("sharedApplication"))
    }
}

class NSWindow(id: Long) : NSObject(id) {
    companion object : NSClass("NSWindow") {
        operator fun invoke() {
            val res = OBJ_CLASS.msgSend("alloc").msgSend("init(contentRect:styleMask:backing:defer:)")
        }

        fun sharedApplication(): NSApplication = NSApplication(OBJ_CLASS.msgSend("sharedApplication"))
    }
}

interface ApplicationShouldTerminateCallback : Callback {
    operator fun invoke(self: Long, _sel: Long, sender: Long): Long
}

var running = true

val applicationShouldTerminateCallback = object : ApplicationShouldTerminateCallback {
    override fun invoke(self: Long, _sel: Long, sender: Long): Long {
        println("applicationShouldTerminateCallback")
        running = false
        System.exit(0)
        return 0L
    }
}

interface ObjcCallback : Callback {
    operator fun invoke(self: Long, _sel: Long, sender: Long): Long
}

fun ObjcCallback(callback: (self: Long, _sel: Long, sender: Long) -> Long): ObjcCallback {
    return object : ObjcCallback {
        override fun invoke(self: Long, _sel: Long, sender: Long): Long = callback(self, _sel, sender)
    }
}

interface WindowWillCloseCallback : Callback {
    operator fun invoke(self: Long, _sel: Long, sender: Long): Long
}

val windowWillClose = object : WindowWillCloseCallback {
    override fun invoke(self: Long, _sel: Long, sender: Long): Long {
        running = false
        System.exit(0)
        return 0L
    }
}

fun Long.alloc(): Long = this.msgSend("alloc")
fun Long.autorelease(): Long = this.apply { this.msgSend("autorelease") }
fun <T : NSObject> T.autorelease(): T = this.apply { this.msgSend("autorelease") }

@Structure.FieldOrder("value")
class CGFloat(val value: Double) : Number(), NativeMapped {
    constructor() : this(0.0)
    constructor(value: Float) : this(value.toDouble())
    constructor(value: Number) : this(value.toDouble())

    companion object {
        @JvmStatic
        val SIZE = Native.LONG_SIZE
    }

    override fun toByte(): Byte = value.toByte()
    override fun toChar(): Char = value.toChar()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toShort()
    override fun nativeType(): Class<*> = when (SIZE) {
        4 -> Float::class.java
        8 -> Double::class.java
        else -> TODO()
    }

    override fun toNative(): Any = when (SIZE) {
        4 -> this.toFloat()
        8 -> this.toDouble()
        else -> TODO()
    }

    override fun fromNative(nativeValue: Any, context: FromNativeContext?): Any = CGFloat((nativeValue as Number).toDouble())

    override fun toString(): String = "$value"
}

@Structure.FieldOrder("x", "y")
public class NSPoint(@JvmField var x: CGFloat, @JvmField var y: CGFloat) : Structure(), Structure.ByValue {
    constructor(x: Double, y: Double) : this(CGFloat(x), CGFloat(y))
    constructor() : this(0.0, 0.0)

    override fun toString(): String = "($x, $y)"

    companion object {
        inline operator fun invoke(x: Number, y: Number) = NSPoint(x.toDouble(), y.toDouble())
    }
}

@Structure.FieldOrder("width", "height")
public class NSSize(@JvmField var width: CGFloat, @JvmField var height: CGFloat) : Structure(), Structure.ByValue {
    constructor(width: Double, height: Double) : this(CGFloat(width), CGFloat(height))
    constructor(width: Number, height: Number) : this(CGFloat(width), CGFloat(height))
    constructor() : this(0.0, 0.0)

    override fun toString(): String = "($width, $height)"

    companion object {
        inline operator fun invoke(width: Number, height: Number) = NSSize(width.toDouble(), height.toDouble())
    }
}

@Structure.FieldOrder("origin", "size")
public class NSRect(
    @JvmField var origin: NSPoint,
    @JvmField var size: NSSize
) : Structure(), Structure.ByValue {
    constructor() : this(NSPoint(), NSSize()) {
        allocateMemory()
        autoWrite()
    }
    constructor(x: Number, y: Number, width: Number, height: Number) : this(NSPoint(x, y), NSSize(width, height))

    override fun toString(): String = "NSRect($origin, $size)"
}

public class NativeNSRect {
    private var pointer: Pointer

    constructor() {
        val memory = Native.malloc(32);
        pointer = Pointer(memory);
    }

    fun free() {
        Native.free(Pointer.nativeValue(pointer));
    }

    fun getPointer(): Pointer {
        return pointer;
    }

    var a: Int get() = pointer.getInt(0L); set(value) = run { pointer.setInt(0L, value) }
    var b: Int get() = pointer.getInt(4L); set(value) = run { pointer.setInt(4L, value) }
    var c: Int get() = pointer.getInt(8L); set(value) = run { pointer.setInt(8L, value) }
    var d: Int get() = pointer.getInt(12L); set(value) = run { pointer.setInt(12L, value) }
    var e: Int get() = pointer.getInt(16L); set(value) = run { pointer.setInt(16L, value) }
    var f: Int get() = pointer.getInt(20L); set(value) = run { pointer.setInt(20L, value) }
    var g: Int get() = pointer.getInt(24L); set(value) = run { pointer.setInt(24L, value) }
    var h: Int get() = pointer.getInt(28L); set(value) = run { pointer.setInt(28L, value) }

    override fun toString(): String = "NativeNSRect($a, $b, $c, $d, $e, $f, $g, $h)"
}

@Structure.FieldOrder("x", "y", "width", "height")
open class MyNativeNSRect : Structure {
    @JvmField var x: Double = 0.0
    @JvmField var y: Double = 0.0
    @JvmField var width: Double = 0.0
    @JvmField var height: Double = 0.0

    constructor() {
        allocateMemory()
        autoWrite()
    }

    class ByReference : MyNativeNSRect(), Structure.ByReference
    class ByValue : MyNativeNSRect(), Structure.ByValue

    override fun toString(): String = "NSRect($x, $y, $width, $height)"
}

@Structure.FieldOrder("x", "y")
open class MyNativeNSPoint : Structure {
    @JvmField var x: Double = 0.0
    @JvmField var y: Double = 0.0

    constructor() {
        allocateMemory()
        autoWrite()
    }

    class ByReference : MyNativeNSPoint(), Structure.ByReference
    class ByValue : MyNativeNSPoint(), Structure.ByValue

    override fun toString(): String = "NSPoint($x, $y)"
}