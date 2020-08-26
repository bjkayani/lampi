#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "BluetoothLampDelegate.h"


@interface BluetoothLampService : NSObject<CBCentralManagerDelegate, CBPeripheralDelegate>

-(instancetype)initWithDelegate:(id<BluetoothLampDelegate>)delegate;
-(void)startScan;
-(void)stopScan;
-(void)changeHue:(float)hue andSaturation:(float)saturation;
-(void)changeOnOff:(BOOL)isOn;
-(void)changeBrightness:(float)brightness;

@end
