#import <UIKit/UIKit.h>
#import "BluetoothLampService.h"

NSString *const DEVICE_ID = @"LAMPI b827eba72a7f";

NSString *const LAMPI_SERVICE_UUID = @"0001a7d3-d8a4-4fea-8174-1736e808c066";
NSString *const HSV_UUID = @"0002a7d3-d8a4-4fea-8174-1736e808c066";
NSString *const BRIGHTNESS_UUID = @"0003a7d3-d8a4-4fea-8174-1736e808c066";
NSString *const ON_OFF_UUID = @"0004a7d3-d8a4-4fea-8174-1736e808c066";

@interface BluetoothLampService()

@property (nonatomic, strong) id<BluetoothLampDelegate> delegate;
@property (nonatomic, strong) CBCentralManager *bluetoothManager;
@property (nonatomic, strong) CBPeripheral *lampPeripheral;

@property (nonatomic, strong) CBService *lampService;
@property (nonatomic, strong) CBCharacteristic *onOffCharacteristic;
@property (nonatomic, strong) CBCharacteristic *hsvCharacteristic;
@property (nonatomic, strong) CBCharacteristic *brightnessCharacteristic;

@property (nonatomic) BOOL shouldConnect;

@end

@implementation BluetoothLampService

-(instancetype)initWithDelegate:(id<BluetoothLampDelegate>)delegate {
    if(self = [super init]) {
        self.delegate = delegate;
        self.bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    }
    return self;
}

-(void)startScan {
    self.shouldConnect = YES;
    [self startScanningIfEnabled];
}

-(void)stopScan {
    self.shouldConnect = NO;
    [self.bluetoothManager stopScan];
}

-(UIColor*)getCurrentHueAndSaturation {
    return nil;
}

-(void)changeHue:(float)hue andSaturation:(float)saturation {
    if(self.hsvCharacteristic == nil)
        return;
    
    uint32_t hsv = 0;
    unsigned char hueInt = (unsigned char)(hue * 255.0);
    unsigned char satInt = (unsigned char)(saturation * 255.0);
    unsigned char valueInt = 255;
    
    hsv = hueInt;
    hsv += satInt << 8;
    hsv += valueInt << 16;
    
    NSData *value = [NSData dataWithBytes:&hsv length:3];
    [self.lampPeripheral writeValue:value forCharacteristic:self.hsvCharacteristic type:CBCharacteristicWriteWithResponse];
}

-(void)changeOnOff:(BOOL)isOn {
    if(self.onOffCharacteristic == nil)
        return;
    
    NSData *value = [NSData dataWithBytes:&isOn length:sizeof(isOn)];
    [self.lampPeripheral writeValue:value forCharacteristic:self.onOffCharacteristic type:CBCharacteristicWriteWithResponse];
}

-(void)changeBrightness:(float)brightness {
    if(self.brightnessCharacteristic == nil)
        return;
    
    unsigned char brightnessChar = (unsigned char)(brightness * 255.0);
    NSData *value = [NSData dataWithBytes:&brightnessChar length:sizeof(brightnessChar)];
    [self.lampPeripheral writeValue:value forCharacteristic:self.brightnessCharacteristic type:CBCharacteristicWriteWithResponse];
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    if ([central state] == CBManagerStatePoweredOn) {
        [self startScanningIfEnabled];
    } else {
        [self.delegate onError:@"Could not access Bluetooth. Ensure it is enabled."];
    }
}

- (void)startScanningIfEnabled {
    if(self.shouldConnect && self.bluetoothManager.state == CBManagerStatePoweredOn) {
        [self.delegate onLoading:@"Searching for lamp..."];
        NSArray *services = @[[CBUUID UUIDWithString:LAMPI_SERVICE_UUID]];
        [self.bluetoothManager scanForPeripheralsWithServices:services options:nil];
    }
}

- (void)centralManager:(CBCentralManager *)central
 didDiscoverPeripheral:(CBPeripheral *)lampPeripheral
     advertisementData:(NSDictionary *)advertisementData
                  RSSI:(NSNumber *)RSSI {
    
    if([lampPeripheral.name isEqualToString:DEVICE_ID]) {
        self.lampPeripheral = lampPeripheral;
        self.lampPeripheral.delegate = self;
        [self.bluetoothManager connectPeripheral:self.lampPeripheral options:nil];
    }
}

- (void)centralManager:(CBCentralManager *)central
  didConnectPeripheral:(CBPeripheral *)lampPeripheral {
    NSLog(@"Peripheral connected");
    [self.delegate onLoading:@"Found lamp! Reading..."];
    lampPeripheral.delegate = self;
    if(lampPeripheral.services.count > 0) {
        for (CBService *service in lampPeripheral.services) {
            if([service.UUID isEqual:[CBUUID UUIDWithString:LAMPI_SERVICE_UUID]]) {
                self.lampService = service;
            }
        }
        
        if(self.lampService != nil) {
            [lampPeripheral discoverCharacteristics:nil forService:self.lampService];
        }
    } else {
        [lampPeripheral discoverServices:@[[CBUUID UUIDWithString:LAMPI_SERVICE_UUID]]];
    }
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)lampPeripheral error:(NSError *)error {
    [self.delegate onLampDisconnected];
    [self.delegate onLoading:@"Disconnected! Searching..."];
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)lampPeripheral error:(NSError *)error {
    [self.delegate onLoading:@"Failed to connect! Searching..."];
}

- (void)peripheral:(CBPeripheral *)lampPeripheral didDiscoverServices:(NSError *)error {
    for (CBService *service in lampPeripheral.services) {
        if([service.UUID isEqual:[CBUUID UUIDWithString:LAMPI_SERVICE_UUID]]) {
            self.lampService = service;
        }
    }
    
    if(self.lampService != nil) {
        [lampPeripheral discoverCharacteristics:nil forService:self.lampService];
    }
}


-(void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    for(CBCharacteristic *characteristic in service.characteristics) {
        if([characteristic.UUID isEqual:[CBUUID UUIDWithString:HSV_UUID]]) {
            self.hsvCharacteristic = characteristic;
            
            [self.lampPeripheral readValueForCharacteristic:self.hsvCharacteristic];
            
            [self.lampPeripheral setNotifyValue:YES forCharacteristic:self.hsvCharacteristic];
            
        } else if([characteristic.UUID isEqual:[CBUUID UUIDWithString:BRIGHTNESS_UUID]]) {
            self.brightnessCharacteristic = characteristic;
            
            [self.lampPeripheral readValueForCharacteristic:self.brightnessCharacteristic];
            
            [self.lampPeripheral setNotifyValue:YES forCharacteristic:self.brightnessCharacteristic];
            
        } else if([characteristic.UUID isEqual:[CBUUID UUIDWithString:ON_OFF_UUID]]) {
            self.onOffCharacteristic = characteristic;
            
            [self.lampPeripheral readValueForCharacteristic:self.onOffCharacteristic];
            
            [self.lampPeripheral setNotifyValue:YES forCharacteristic:self.onOffCharacteristic];
            
        }
    }
    
    if(self.hsvCharacteristic != nil && self.brightnessCharacteristic != nil && self.onOffCharacteristic != nil) {
        [self.delegate onLampConnected];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral
didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic
             error:(NSError *)error {
    
    if(characteristic.value == nil) {
        return;
    }
    
    if(characteristic == self.hsvCharacteristic) {
        NSData *data = self.hsvCharacteristic.value;
        float fHue = [self parseHue:data];
        float fSat = [self parseSaturation:data];
        [self.delegate onUpdatedHue:fHue andSaturation:fSat];
    } else if(characteristic == self.brightnessCharacteristic) {
        NSData *data = self.brightnessCharacteristic.value;
        float fBright = [self parseBrightness:data];
        [self.delegate onUpdatedBrightness:fBright];
    } else if(characteristic == self.onOffCharacteristic) {
        NSData *data = self.onOffCharacteristic.value;
        BOOL onOff = [self parseOnOff:data];
        [self.delegate onUpdatedOnOff:onOff];
    }
}

-(float)parseHue:(NSData*)data {
    const unsigned char* bytes = [data bytes];
    unsigned char hue = bytes[0];
    return ((float)hue) / 255.0f;
}

-(float)parseSaturation:(NSData*)data {
    const unsigned char* bytes = [data bytes];
    unsigned char saturation = bytes[1];
    return ((float)saturation) / 255.0f;
}

-(float)parseBrightness:(NSData*)data {
    const unsigned char* bytes = [data bytes];
    unsigned char brightness = bytes[0];
    return ((float)brightness) / 255.0f;
}

-(BOOL)parseOnOff:(NSData*)data {
    const unsigned char* bytes = [data bytes];
    return bytes[0];
}

@end
