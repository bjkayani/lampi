@protocol BluetoothLampDelegate

-(void)onLampConnected;
-(void)onLampDisconnected;
-(void)onError:(NSString*)error;
-(void)onLoading:(NSString*)error;
-(void)onUpdatedHue:(float)hue andSaturation:(float)saturation;
-(void)onUpdatedBrightness:(float)brightness;
-(void)onUpdatedOnOff:(BOOL)onOff;

@end