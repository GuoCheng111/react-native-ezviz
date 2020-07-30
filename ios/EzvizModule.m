
#import <React/RCTBridgeModule.h>
#import "RCTUIManager.h"

#import "EZOpenSDK/EZOpenSDK.h"
#import "EZOpenSDK/EZCameraInfo.h"
#import "EZOpenSDK/EZDeviceInfo.h"

@interface EzvizModule: NSObject<RCTBridgeModule>

@end

@implementation EzvizModule

RCT_EXPORT_MODULE();
RCT_EXPORT_METHOD(setAccessToken:(NSString *)token
resolver:(RCTPromiseResolveBlock)resolve
rejecter:(RCTPromiseRejectBlock)reject){
    resolve(nil);
}
@end
