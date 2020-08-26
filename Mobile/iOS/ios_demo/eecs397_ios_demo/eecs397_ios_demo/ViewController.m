//
//  ViewController.m
//  eecs397_ios_demo
//
//  Created by Nick Barendt on 3/7/17.
//  Copyright © 2017 Nick Barendt. All rights reserved.
//

#import "ViewController.h"


@interface ViewController ()

@property (nonatomic, strong) CBCentralManager *bluetoothManager;
@property (nonatomic, strong) CBPeripheral *devicePeripheral;
@property (nonatomic, strong) CBCharacteristic *numberCharacteristic;
@property (nonatomic) BOOL updatingPending;

@end

NSString *const DEVICE_NAME = @"LAMPI b827ebba0387";
NSString *const OUR_SERVICE_UUID = @"7a4bbfe6-999f-4717-b63a-066e06971f59";
NSString *const SOME_NUMBER_UUID = @"7a4b0001-999f-4717-b63a-066e06971f59";


@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    NSLog(@"slider: %@ \n label: %@", self.slider, self.label);
}

-(instancetype)initWithCoder:(NSCoder *)aDecoder {
    if(self = [super initWithCoder:aDecoder]) {
        self.bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    }
    return self;
}

-(IBAction)onSliderChanged:(UISlider*)sender {
    
    self.label.text = [NSString stringWithFormat:@"%f", self.slider.value];
    
    if( self.numberCharacteristic != nil ) {
        
        if(!self.updatingPending) {
            __weak typeof(self) weakself = self;
            
            self.updatingPending = YES;
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW,
                                         (int64_t)(0.1 * NSEC_PER_SEC)),
                           dispatch_get_main_queue(), ^{
                               uint8_t new_value = weakself.slider.value * 0XFF;
                               NSData *d = [NSData dataWithBytes: &new_value length:sizeof(new_value)];
                               [weakself.devicePeripheral writeValue:d forCharacteristic:weakself.numberCharacteristic type:CBCharacteristicWriteWithResponse];
                               weakself.updatingPending = NO;
                           });
        }
    }
}
- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    if ([central state] == CBManagerStatePoweredOn) {
        NSArray *services = @[[CBUUID UUIDWithString:OUR_SERVICE_UUID]];
        [self.bluetoothManager scanForPeripheralsWithServices:services options:nil];
    }
}

- (void)centralManager:(CBCentralManager *)central
 didDiscoverPeripheral:(CBPeripheral *)devicePeripheral
     advertisementData:(NSDictionary *)advertisementData
                  RSSI:(NSNumber *)RSSI {
    
    if([devicePeripheral.name isEqualToString:DEVICE_NAME]) {
        self.devicePeripheral = devicePeripheral;
        [self.bluetoothManager connectPeripheral:self.devicePeripheral options:nil];
        NSLog(@"Found %@", DEVICE_NAME);
    }
}

- (void)centralManager:(CBCentralManager *)central
  didConnectPeripheral:(CBPeripheral *)devicePeripheral {
    NSLog(@"Connected to peripheral %@", devicePeripheral);
    devicePeripheral.delegate = self;
    
    [devicePeripheral discoverServices:@[[CBUUID UUIDWithString:OUR_SERVICE_UUID]]];
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)devicePeripheral error:(NSError *)error {
    NSLog(@"Disconnected from peripheral %@", devicePeripheral);
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)devicePeripheral error:(NSError *)error {
    NSLog(@"Failed to connect to peripheral %@", devicePeripheral);
}

- (void)peripheral:(CBPeripheral *)devicePeripheral didDiscoverServices:(NSError *)error {
    for (CBService *service in devicePeripheral.services) {
        if([service.UUID isEqual:[CBUUID UUIDWithString:OUR_SERVICE_UUID]]) {
            NSLog(@"Found device service");
            [devicePeripheral discoverCharacteristics:nil forService:service];
        }
    }
}


-(void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    for(CBCharacteristic *characteristic in service.characteristics) {
        if([characteristic.UUID isEqual:[CBUUID UUIDWithString:SOME_NUMBER_UUID]]) {
            NSLog(@"Found characteristic with UUID %@", SOME_NUMBER_UUID);
            self.numberCharacteristic = characteristic;
            [self.devicePeripheral readValueForCharacteristic:characteristic];
            [self.devicePeripheral setNotifyValue:YES forCharacteristic:characteristic];
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    NSLog(@"didUpdateValueForCharacteristic error: %@", error);
    if(characteristic == self.numberCharacteristic) {
        NSData *data = [characteristic value];
        if(data) {
            NSLog(@"new value = %@", data);
            const unsigned char *bytes = [data bytes];
            self.slider.value = (double)bytes[0] / (double) UINT8_MAX;
            self.label.text = [NSString stringWithFormat:@"%f", self.slider.value];
        }
    }
    
}
@end
