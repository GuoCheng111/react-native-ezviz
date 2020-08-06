#import "RCTViewManager.h"
#import <MapKit/MapKit.h>



#import "EzvizView.h"

@interface EzvizManager : RCTViewManager

@end

@implementation EzvizManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
  return [[EzvizView alloc] init];
}

RCT_CUSTOM_VIEW_PROPERTY(deviceSerial, NSString, EzvizView)
{
    view.deviceSerial = json;
}

RCT_CUSTOM_VIEW_PROPERTY(verifyCode, NSString, EzvizView)
{
    view.verifyCode = json;
}

RCT_EXPORT_METHOD(play:(nonnull NSNumber *)reactTag
                  options:(NSDictionary *)options)
{
    NSString *type = [RCTConvert NSString:options[@"type"]];
    
}

RCT_EXPORT_METHOD(stop:(nonnull NSNumber *)reactTag
                  options:(NSDictionary *)options)
{
    NSString *type = [RCTConvert NSString:options[@"type"]];
    
}

RCT_EXPORT_METHOD(openSound:(nonnull NSNumber *)reactTag
                  options:(NSDictionary *)options)
{
    NSString *type = [RCTConvert NSString:options[@"type"]];
    
}

RCT_EXPORT_METHOD(closeSound:(nonnull NSNumber *)reactTag
                  options:(NSDictionary *)options)
{
    NSString *type = [RCTConvert NSString:options[@"type"]];
    
}

RCT_EXPORT_METHOD(controlPIZ:(nonnull NSNumber *)reactTag
                  options:(NSDictionary *)options)
{
    NSString *type = [RCTConvert NSString:options[@"type"]];
    
}

RCT_EXPORT_METHOD(startVoiceTalk:(nonnull NSNumber *)reactTag
                  options:(NSDictionary *)options)
{
    NSString *type = [RCTConvert NSString:options[@"type"]];
    
}

RCT_EXPORT_METHOD(stopVoiceTalk:(nonnull NSNumber *)reactTag
                  options:(NSDictionary *)options)
{
    NSString *type = [RCTConvert NSString:options[@"type"]];
    
}

@end
