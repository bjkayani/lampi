#import "GradientSlider.h"


@interface GradientSlider() {
    CALayer* _minTrackImageLayer;
    CALayer* _maxTrackImageLayer;
    CAGradientLayer* _trackLayer;
    CALayer* _thumbLayer;
    CALayer* _thumbIconLayer;
}

@end

@implementation GradientSlider

static float defaultThickness = 2.0;
static float defaultThumbSize = 28.0;

-(instancetype)init {
    if(self = [super init]) {
        [self initialize];
        [self commonSetup];
    }
    return self;
}

-(instancetype)initWithFrame:(CGRect)frame {
    if(self = [super initWithFrame:frame]) {
        [self initialize];
        [self commonSetup];
    }
    return self;
}

-(instancetype)initWithCoder:(NSCoder *)aDecoder {
    if(self = [super initWithCoder:aDecoder]) {
        [self initialize];
        
        UIColor *minColor = [aDecoder decodeObjectForKey:@"minColor"];
        if(minColor == nil) {
            minColor = [UIColor lightGrayColor];
        }
        self.minColor = minColor;
        
        UIColor *maxColor = [aDecoder decodeObjectForKey:@"maxColor"];
        if(maxColor == nil) {
            maxColor = [UIColor darkGrayColor];
        }
        self.maxColor = maxColor;
        
        NSNumber *value = [aDecoder decodeObjectForKey:@"value"];
        if(value != nil) {
            self.value = [value floatValue];
        }
        NSNumber *minValue = [aDecoder decodeObjectForKey:@"minimumValue"];
        if(minValue != nil) {
            self.minimumValue = [minValue floatValue];
        }
        
        NSNumber *maxValue = [aDecoder decodeObjectForKey:@"maximumValue"];
        if(maxValue != nil) {
            self.maximumValue = [maxValue floatValue];
        }
        
        UIImage *minValueImage = [aDecoder decodeObjectForKey:@"minimumValueImage"];
        if(minValueImage != nil) {
            self.minimumValueImage = minValueImage;
        }
        
        UIImage *maxValueImage = [aDecoder decodeObjectForKey:@"maximumValueImage"];
        if(maxValueImage != nil) {
            self.maximumValueImage = maxValueImage;
        }
        
        NSNumber *thickness = [aDecoder decodeObjectForKey:@"thickness"];
        if(thickness != nil) {
            self.thickness = [thickness floatValue];
        }
        
        UIImage *thumbIcon = [aDecoder decodeObjectForKey:@"thumbIcon"];
        if(thumbIcon != nil) {
            self.thumbIcon = thumbIcon;
        }
        
        [self commonSetup];
    }
    return self;
}

-(void)encodeWithCoder:(NSCoder *)aCoder {
    [super encodeWithCoder:aCoder];
    
    [aCoder encodeObject:self.minColor forKey:@"minColor"];
    [aCoder encodeObject:self.maxColor forKey:@"maxColor"];
    
    [aCoder encodeObject:@(self.value) forKey:@"value"];
    [aCoder encodeObject:@(self.minimumValue) forKey:@"minimumValue"];
    [aCoder encodeObject:@(self.maximumValue) forKey:@"maximumValue"];
    
    [aCoder encodeObject:self.minimumValueImage forKey:@"minimumValueImage"];
    [aCoder encodeObject:self.maximumValueImage forKey:@"maximumValueImage"];
    
    [aCoder encodeObject:@(self.thickness) forKey:@"thickness"];
    
    [aCoder encodeObject:self.thumbIcon forKey:@"thumbIcon"];
}

-(void)initialize {
    _minColor = [UIColor lightGrayColor];
    _maxColor = [UIColor darkGrayColor];
    _minTrackImageLayer = nil;
    _maxTrackImageLayer = nil;
    _thickness = defaultThickness;
    _continuous = YES;
    _value = 0.0f;
    _minimumValue = 0.0f;
    _maximumValue = 1.0f;
    [self initializeThumbLayer];
    [self initializeTrackLayer];
    [self initializeThumbIconLayer];
    
}

-(void)commonSetup{
    self.layer.delegate = self;
    [self.layer addSublayer:_trackLayer];
    [self.layer addSublayer:_thumbLayer];
    [_thumbLayer addSublayer:_thumbIconLayer];
}
    
-(void)initializeThumbLayer {
    CALayer *thumb = [[CALayer alloc] init];
    thumb.cornerRadius = defaultThumbSize/2.0;
    thumb.bounds = CGRectMake(0, 0, defaultThumbSize, defaultThumbSize);
    thumb.backgroundColor = [[UIColor whiteColor] CGColor];
    thumb.shadowColor = [[UIColor blackColor] CGColor];
    thumb.shadowOffset = CGSizeMake(0.0, 2.5);
    thumb.shadowRadius = 2.0;
    thumb.shadowOpacity = 0.25;
    thumb.borderColor = [[[UIColor blackColor] colorWithAlphaComponent:0.15] CGColor];
    thumb.borderWidth = 0.5;
    _thumbLayer = thumb;
}

-(void)initializeTrackLayer {
    CAGradientLayer *track = [[CAGradientLayer alloc] init];
    track.cornerRadius = defaultThickness / 2.0;
    track.startPoint = CGPointMake(0.0, 0.5);
    track.endPoint = CGPointMake(1.0, 0.5);
    track.locations = @[@0.0,@1.0];
    track.colors = @[(id)[[UIColor blueColor] CGColor], (id)[[UIColor orangeColor] CGColor]];
    track.borderColor = [[UIColor blackColor] CGColor];
    _trackLayer = track;
}

-(void)initializeThumbIconLayer {
    CGFloat size = defaultThumbSize - 4;
    CALayer *iconLayer = [[CALayer alloc] init];
    iconLayer.cornerRadius = size/2.0;
    iconLayer.bounds = CGRectMake(0, 0, size, size);
    iconLayer.backgroundColor = [[UIColor clearColor] CGColor];
    _thumbIconLayer = iconLayer;
}

-(void)setMinColor:(UIColor *)minColor {
    _minColor = minColor;
    [self updateTrackColors];
}

-(void)setMaxColor:(UIColor *)maxColor {
    _maxColor = maxColor;
    [self updateTrackColors];
}

-(void)setHasRainbow:(BOOL)hasRainbow {
    _hasRainbow = hasRainbow;
    [self updateTrackColors];
}

-(void)setValue:(CGFloat)value {
    [self setValue:value animated:NO];
}

-(void)setValue:(CGFloat)value animated:(BOOL)animated {
    _value = fmax(fmin(value,self.maximumValue),self.minimumValue);
    [self updateThumbPositionAnimated:animated];
}

-(void)setMinimumValueImage:(UIImage *)minimumValueImage {
    _minimumValueImage = minimumValueImage;
    
    if(_minimumValueImage != nil) {
        CALayer *imgLayer = _minTrackImageLayer;
        if(imgLayer == nil) {
            imgLayer = [[CALayer alloc] init];
            imgLayer.anchorPoint = CGPointMake(0.0, 0.5);
            [self.layer addSublayer:imgLayer];
        }
        imgLayer.contents = (id)[_minimumValueImage CGImage];
        imgLayer.bounds = CGRectMake(0,0,_minimumValueImage.size.width, _minimumValueImage.size.height);
        _minTrackImageLayer = imgLayer;
    } else if(_minTrackImageLayer != nil){
        [_minTrackImageLayer removeFromSuperlayer];
        _minTrackImageLayer = nil;
    }
    
    [self setNeedsLayout];
}

-(void)setMaximumValueImage:(UIImage *)maximumValueImage {
    _maximumValueImage = maximumValueImage;
    
    if(_maximumValueImage != nil) {
        CALayer *imgLayer = _maxTrackImageLayer;
        if(imgLayer == nil) {
            imgLayer = [[CALayer alloc] init];
            imgLayer.anchorPoint = CGPointMake(1.0, 0.5);
            [self.layer addSublayer:imgLayer];
        }
        imgLayer.contents = (id)[_maximumValueImage CGImage];
        imgLayer.bounds = CGRectMake(0,0,_maximumValueImage.size.width, _maximumValueImage.size.height);
        _maxTrackImageLayer = imgLayer;
    } else if(_maxTrackImageLayer != nil){
        [_maxTrackImageLayer removeFromSuperlayer];
        _maxTrackImageLayer = nil;
    }
    
    [self setNeedsLayout];
}

-(void)setThickness:(CGFloat)thickness {
    _thickness = thickness;
    _trackLayer.cornerRadius = thickness / 2.0;
    [self.layer setNeedsLayout];
}

-(UIColor*)trackBorderColor {
    CGColorRef color = _trackLayer.borderColor;
    if(color != nil) {
        return [UIColor colorWithCGColor:color];
    }
    return nil;
}

-(void)setTrackBorderColor:(UIColor *)trackBorderColor {
    _trackLayer.borderColor = [trackBorderColor CGColor];
}

-(CGFloat)trackBorderWidth {
    return _trackLayer.borderWidth;
}

-(void)setTrackBorderWidth:(CGFloat)trackBorderWidth {
    _trackLayer.borderWidth = trackBorderWidth;
}

-(void)setThumbSize:(CGFloat)thumbSize {
    _thumbSize = thumbSize;
    _thumbLayer.cornerRadius = thumbSize / 2.0;
    _thumbLayer.bounds = CGRectMake(0, 0, thumbSize, thumbSize);
    [self invalidateIntrinsicContentSize];
}

-(void)setThumbIcon:(UIImage*)thumbIcon {
    _thumbIcon = thumbIcon;
    if(thumbIcon != nil) {
        _thumbIconLayer.contents = (id)[thumbIcon CGImage];
    } else {
        _thumbIconLayer.contents = nil;
    }
}

-(UIColor*)thumbColor {
    CGColorRef color = _thumbLayer.backgroundColor;
    if(color != nil) {
        return [UIColor colorWithCGColor:color];
    }
    return [UIColor whiteColor];
}

-(void)setThumbColor:(UIColor *)thumbColor {
    _thumbLayer.backgroundColor = [thumbColor CGColor];
}

-(void)setGradientForHueWithSaturation:(CGFloat)saturation andBrightness:(CGFloat)brightness {
    self.minColor = [UIColor colorWithHue:0.0 saturation:saturation brightness:brightness alpha:1.0];
    self.hasRainbow = YES;
}


-(void)setGradientForSaturationWithHue:(CGFloat)hue andBrightness:(CGFloat)brightness {
    self.hasRainbow = NO;
    self.minColor = [UIColor colorWithHue:hue saturation:0.0 brightness:brightness alpha:1.0];
    self.maxColor = [UIColor colorWithHue:hue saturation:1.0 brightness:brightness alpha:1.0];
}

-(void)setGradientForBrightnessWithHue:(CGFloat)hue andSaturation:(CGFloat)saturation{
    self.hasRainbow = NO;
    self.minColor = [UIColor blackColor];
    self.maxColor = [UIColor colorWithHue:hue saturation:saturation brightness:1.0 alpha:1.0];
}

-(void)setGradientForRedWithGreen:(CGFloat)green andBlue:(CGFloat)blue{
    self.hasRainbow = NO;
    self.minColor = [UIColor colorWithRed:0.0 green:green blue:blue alpha:1.0];
    self.maxColor = [UIColor colorWithRed:1.0 green:green blue:blue alpha:1.0];
}

 
-(void)setGradientForGreenWithRed:(CGFloat)red andBlue:(CGFloat)blue{
    self.hasRainbow = NO;
    self.minColor = [UIColor colorWithRed:red green:0.0 blue:blue alpha:1.0];
    self.maxColor = [UIColor colorWithRed:red green:1.0 blue:blue alpha:1.0];
}

-(void)setGradientForBlueWithRed:(CGFloat)red andGreen:(CGFloat)green{
    self.hasRainbow = NO;
    self.minColor = [UIColor colorWithRed:red green:green blue:0.0 alpha:1.0];
    self.maxColor = [UIColor colorWithRed:red green:green blue:1.0 alpha:1.0];
}

-(void)setGradientForGrayscale{
    self.hasRainbow = NO;
    self.minColor = [UIColor blackColor];
    self.maxColor = [UIColor whiteColor];
}

-(CGSize)intrinsicContentSize {
    return CGSizeMake(UIViewNoIntrinsicMetric, self.thumbSize);
}

-(UIEdgeInsets)alignmentRectInsets {
    return UIEdgeInsetsMake(4.0, 2.0, 4.0, 2.0);
}

-(void)layoutSublayersOfLayer:(CALayer *)layer {
    [super layoutSublayersOfLayer:layer];
    
    if (layer != self.layer) {
        return;
    }
    
    CGFloat w = self.bounds.size.width;
    CGFloat h = self.bounds.size.height;
    CGFloat left = 2.0;
    
    if(_minTrackImageLayer != nil) {
        _minTrackImageLayer.position = CGPointMake(0.0, h/2.0);
        left = _minTrackImageLayer.bounds.size.width + 13.0;
    }
    w -= left;
    
    if(_maxTrackImageLayer != nil) {
        _maxTrackImageLayer.position = CGPointMake(self.bounds.size.width, h/2.0);
        w -= (_maxTrackImageLayer.bounds.size.width + 13.0);
    }else{
        w -= 2.0;
    }
    
    _trackLayer.bounds = CGRectMake(0, 0, w, self.thickness);
    _trackLayer.position = CGPointMake(w/2.0 + left, h/2.0);
    
    CGFloat halfSize = self.thumbSize/2.0;
    CGFloat layerSize = self.thumbSize - 4.0;
    if(self.thumbIcon == nil) {
        layerSize = fmin(fmax(self.thumbIcon.size.height,self.thumbIcon.size.width),layerSize);
        _thumbIconLayer.cornerRadius = 0.0;
        _thumbIconLayer.backgroundColor = [[UIColor clearColor] CGColor];
    } else {
        _thumbIconLayer.cornerRadius = layerSize/2.0;
    }
    _thumbIconLayer.position = CGPointMake(halfSize, halfSize);
    _thumbIconLayer.bounds = CGRectMake(0, 0, layerSize, layerSize);
    
    [self updateThumbPositionAnimated:NO];
}

-(BOOL)beginTrackingWithTouch:(UITouch *)touch withEvent:(UIEvent *)event {
    CGPoint pt = [touch locationInView:self];
    
    CGPoint center = _thumbLayer.position;
    CGFloat diameter = fmax(self.thumbSize,44.0);
    CGRect r = CGRectMake(center.x - diameter/2.0, center.y - diameter/2.0, diameter, diameter);
    if(CGRectContainsPoint(r, pt)) {
        [self sendActionsForControlEvents:UIControlEventTouchDown];
        return YES;
    }
    return NO;
}

-(BOOL)continueTrackingWithTouch:(UITouch*)touch withEvent:(nullable UIEvent *)event {
    CGPoint pt = [touch locationInView:self];
    CGFloat newValue = [self valueForLocation:pt];
    [self setValue:newValue animated: NO];
    if(self.continuous) {
        [self sendActionsForControlEvents:UIControlEventValueChanged];  
    }
    return YES;
}

-(void)endTrackingWithTouch:(UITouch *)touch withEvent:(UIEvent *)event {
    if(touch != nil) {
        CGPoint pt = [touch locationInView:self];
        CGFloat newValue = [self valueForLocation:pt];
        [self setValue:newValue animated: NO];
    }
    
    [self sendActionsForControlEvents:UIControlEventValueChanged | UIControlEventTouchUpInside];
}

-(void)updateThumbPositionAnimated:(BOOL)animated {
    CGFloat diff = self.maximumValue - self.minimumValue;
    CGFloat perc = (CGFloat)((self.value - self.minimumValue) / diff);
    if(isnan(perc)) {
        perc = 0.0;
    }
    
    CGFloat halfHeight = self.bounds.size.height / 2.0;
    CGFloat trackWidth = _trackLayer.bounds.size.width - self.thumbSize;
    CGFloat left = _trackLayer.position.x - trackWidth/2.0;
    
    if (!animated) {
        [CATransaction begin]; //Move the thumb position without animations
        [CATransaction setValue:[NSNumber numberWithBool:YES] forKey:kCATransactionDisableActions];
        _thumbLayer.position = CGPointMake(left + (trackWidth * perc), halfHeight);
        [CATransaction commit];
    } else {
        _thumbLayer.position = CGPointMake(left + (trackWidth * perc), halfHeight);
    }
}

-(CGFloat) valueForLocation:(CGPoint)point {
    
    CGFloat left = self.bounds.origin.x;
    CGFloat w = self.bounds.size.width;
    if (_minTrackImageLayer != nil) {
        CGFloat amt = _minTrackImageLayer.bounds.size.width + 13.0;
        w -= amt;
        left += amt;
    } else {
        w -= 2.0;
        left += 2.0;
    }
    
    if(_maxTrackImageLayer != nil) {
        w -= (_maxTrackImageLayer.bounds.size.width + 13.0);
    } else {
        w -= 2.0;
    }
    
    CGFloat diff = (CGFloat)(self.maximumValue - self.minimumValue);
    
    CGFloat perc = fmax(fmin((point.x - left) / w ,1.0), 0.0);
    
    return (perc * diff) + (CGFloat)(self.minimumValue);
}

-(void)updateTrackColors {
    if (!self.hasRainbow) {
        _trackLayer.colors = @[(id)[self.minColor CGColor],(id)[self.maxColor CGColor]];
        _trackLayer.locations = @[@0.0,@1.0];
        return;
    }
    //Otherwise make a rainbow with the saturation & lightness of the min color
    CGFloat h = 0.0;
    CGFloat s = 0.0;
    CGFloat l = 0.0;
    CGFloat a = 1.0;
    
    [[self minColor] getHue:&h saturation: &s brightness: &l alpha: &a];
    
    CGFloat hueSplit = 1.0/6.0;
    _trackLayer.locations = @[@(hueSplit*0), @(hueSplit*1), @(hueSplit*2), @(hueSplit*3), @(hueSplit*4), @(hueSplit*5), @(hueSplit*6)];
    _trackLayer.colors = @[(id)[[UIColor redColor] CGColor],
                           (id)[[UIColor yellowColor] CGColor],
                           (id)[[UIColor greenColor] CGColor],
                           (id)[[UIColor cyanColor] CGColor],
                           (id)[[UIColor blueColor] CGColor],
                           (id)[[UIColor magentaColor] CGColor],
                           (id)[[UIColor redColor] CGColor]];
}

@end
