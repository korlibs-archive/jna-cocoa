// gcc main.m -framework Cocoa

#import <Foundation/Foundation.h>
#import <Cocoa/Cocoa.h>
//#import "main.h"

@interface MyWindowDelegate : NSWindowController<NSWindowDelegate>
@end

@implementation MyWindowDelegate
    - (void)windowDidLoad {
        [super windowDidLoad];
        // Implement this method to handle any initialization after your window controller’s window has been loaded from its nib file.
    }
    - (BOOL)windowShouldClose:(id)sender {
        //[NSApp hide:nil];
        //return NO;
        return YES;
    }
@end

@interface MyAppDelegate : NSWindowController<NSApplicationDelegate>
@end

MyWindowDelegate *myWindowDelegate;
MyAppDelegate *myAppDelegate;

@implementation MyAppDelegate
    - (void)applicationWillFinishLaunching:(NSNotification* )notification {
        
        myWindowDelegate = [[MyAppDelegate alloc] init];
        
        NSWindow *window = [
            [NSWindow alloc]
            initWithContentRect: NSMakeRect(300, 300, 300, 300)
            styleMask: (NSWindowStyleMaskTitled | NSWindowStyleMaskMiniaturizable | NSWindowStyleMaskClosable | NSWindowStyleMaskResizable)
            backing:NSBackingStoreBuffered
            defer:true
        ];
        
        [window setTitle: @"Title"];
        [window setOpaque: YES];
        [window setHasShadow:YES];
        [window setHidesOnDeactivate:false];
        [window setBackgroundColor:[NSColor grayColor]];
        [window setReleasedWhenClosed:YES];
        [window setDelegate: myWindowDelegate];
        [window makeKeyAndOrderFront:self];
    }
@end

int main(int argc, const char * argv[]) {
    @autoreleasepool {
        NSApplication *app = [NSApplication sharedApplication];
        
        myAppDelegate = [[MyAppDelegate alloc] init];
        
        app.delegate = myAppDelegate;
        [app setActivationPolicy: NSApplicationActivationPolicyRegular];
        [app activateIgnoringOtherApps: true];

        [app run];

        /*
        NSRect rect = [[NSScreen mainScreen] visibleFrame];
        NSString* frameAsString = NSStringFromRect(rect);
        NSLog(@"mainScreen frame = %@", frameAsString);
        NSLog(@"Hello, World!");
        */
    }
    return 0;
}

