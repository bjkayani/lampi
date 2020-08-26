#import <UIKit/UIKit.h>


IB_DESIGNABLE
@interface GradientSlider : UIControl

@property (nonatomic, strong, setter=setMinColor:) IBInspectable UIColor* minColor;
@property (nonatomic, strong, setter=setMaxColor:) IBInspectable UIColor* maxColor;
@property (nonatomic, strong) IBInspectable UIColor* thumbColor;
@property (nonatomic, strong) IBInspectable UIColor* trackBorderColor;
@property (nonatomic) IBInspectable CGFloat trackBorderWidth;
@property (nonatomic, setter=setThumbSize:) IBInspectable CGFloat thumbSize;
@property (nonatomic, setter=setHasRainbow:) IBInspectable BOOL hasRainbow;
@property (nonatomic) BOOL continuous;
@property (nonatomic, setter=setValue:) IBInspectable CGFloat value;
@property (nonatomic) IBInspectable CGFloat minimumValue;
@property (nonatomic) IBInspectable CGFloat maximumValue;
@property (nonatomic, strong, setter=setMinimumValueImage:) IBInspectable UIImage* minimumValueImage;
@property (nonatomic, strong, setter=setMaximumValueImage:) IBInspectable UIImage* maximumValueImage;
@property (nonatomic, strong, setter=setThumbIcon:) IBInspectable UIImage* thumbIcon;
@property (nonatomic, setter=setThickness:) IBInspectable CGFloat thickness;

-(void)setGradientForHueWithSaturation:(CGFloat)saturation andBrightness:(CGFloat)brightness;
-(void)setGradientForSaturationWithHue:(CGFloat)hue andBrightness:(CGFloat)brightness;
-(void)setGradientForBrightnessWithHue:(CGFloat)hue andSaturation:(CGFloat)saturation;
-(void)setGradientForRedWithGreen:(CGFloat)green andBlue:(CGFloat)blue;
-(void)setGradientForGreenWithRed:(CGFloat)red andBlue:(CGFloat)blue;
-(void)setGradientForBlueWithRed:(CGFloat)red andGreen:(CGFloat)green;

@end