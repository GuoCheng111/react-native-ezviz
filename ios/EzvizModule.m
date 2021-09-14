
#import <React/RCTBridgeModule.h>
#import "RCTUIManager.h"

#import "EZOpenSDK.h"
#import "EZCameraInfo.h"
#import "EZDeviceInfo.h"

@interface EzvizModule: NSObject<RCTBridgeModule>

@end

@implementation EzvizModule

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(setAccessToken:(NSString *)token
resolver:(RCTPromiseResolveBlock)resolve
rejecter:(RCTPromiseRejectBlock)reject){
    NSLog(@"setAccessToken %@",token);
    [EZOpenSDK setAccessToken:token];
    resolve(nil);
}


RCT_EXPORT_METHOD(getDeviceInfo:(NSString *)deviceSerial
resolver:(RCTPromiseResolveBlock)resolve
rejecter:(RCTPromiseRejectBlock)reject){
    resolve(nil);
}

RCT_EXPORT_METHOD(init:(NSString *)appKey
resolver:(RCTPromiseResolveBlock)resolve
rejecter:(RCTPromiseRejectBlock)reject){
    [EZOpenSDK initLibWithAppKey:appKey];
    resolve(nil);
}

RCT_EXPORT_METHOD(releaseLib){
    [EZOpenSDK destoryLib];
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

-(NSDictionary*) constantsToExport
{
    return @{
        @"EZPTZAction_EZPTZActionSTART":@(EZPTZActionStart),
        @"EZPTZAction_EZPTZActionSTOP":@(EZPTZActionStop),
        @"EZPTZCommand_EZPTZCommandLeft":@(EZPTZCommandLeft),
        @"EZPTZCommand_EZPTZCommandRight":@(EZPTZCommandRight),
        @"EZPTZCommand_EZPTZCommandUp":@(EZPTZCommandUp),
        @"EZPTZCommand_EZPTZCommandDown":@(EZPTZCommandDown),
    };
}

@end

@implementation RCTConvert(EZPTZAction)
RCT_ENUM_CONVERTER(EZPTZAction, (@{
    @"EZPTZAction_EZPTZActionSTART":@(EZPTZActionStart),
    @"EZPTZAction_EZPTZActionSTOP":@(EZPTZActionStop),
}), EZPTZActionStop, integerValue)
@end

@implementation RCTConvert(EZPTZCommand)
RCT_ENUM_CONVERTER(EZPTZCommand, (@{
    @"EZPTZCommand_EZPTZCommandLeft":@(EZPTZCommandLeft),
    @"EZPTZCommand_EZPTZCommandRight":@(EZPTZCommandRight),
    @"EZPTZCommand_EZPTZCommandUp":@(EZPTZCommandUp),
    @"EZPTZCommand_EZPTZCommandDown":@(EZPTZCommandDown),
}), EZPTZCommandLeft, integerValue)

@end
