//
//  VideoViewFactory.m
//  flutter_aliplayer
//
//  Created by aliyun on 2020/10/9.
//
#import "AliPlayerFactory.h"
#import "FlutterAliPlayerView.h"
#import "NSDictionary+ext.h"
#import "MJExtension.h"

#define kAliPlayerMethod    @"method"

@interface AliPlayerFactory () {
    NSObject<FlutterBinaryMessenger>* _messenger;
    FlutterMethodChannel* _channel;
    FlutterMethodChannel* _listPlayerchannel;
    FlutterMethodChannel* _commonChannel;
    UIView *playerView;
    NSString *mSnapshotPath;
}

@property (nonatomic, strong) FlutterEventSink eventSink;
@property (nonatomic, assign) BOOL enableMix;

@end

@implementation AliPlayerFactory

- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    self = [super init];
    if (self) {
        _messenger = messenger;
        __weak __typeof__(self) weakSelf = self;
        
        _commonChannel = [FlutterMethodChannel methodChannelWithName:@"plugins.flutter_aliplayer_factory" binaryMessenger:messenger];
        [_commonChannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
            [weakSelf onMethodCall:call result:result atObj:@""];
        }];
        
        _channel = [FlutterMethodChannel methodChannelWithName:@"flutter_aliplayer" binaryMessenger:messenger];
        [_channel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
            [weakSelf onMethodCall:call result:result atObj:weakSelf.aliPlayer];
        }];
        
        _listPlayerchannel = [FlutterMethodChannel methodChannelWithName:@"flutter_alilistplayer" binaryMessenger:messenger];
        [_listPlayerchannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
            [weakSelf onMethodCall:call result:result atObj:weakSelf.aliListPlayer];
        }];
        
        FlutterEventChannel *eventChannel = [FlutterEventChannel eventChannelWithName:@"flutter_aliplayer_event" binaryMessenger:messenger];
        [eventChannel setStreamHandler:self];
        
    }
    return self;
}

#pragma mark - FlutterStreamHandler
- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments
                                       eventSink:(FlutterEventSink)eventSink{
    self.eventSink = eventSink;
    return nil;
}

- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    return nil;
}

- (NSObject<FlutterMessageCodec>*)createArgsCodec {
    return [FlutterStandardMessageCodec sharedInstance];
}

- (nonnull NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame
                                            viewIdentifier:(int64_t)viewId
                                                 arguments:(id _Nullable)args {
    FlutterAliPlayerView* player =
    [[FlutterAliPlayerView alloc] initWithWithFrame:frame
                                     viewIdentifier:viewId
                                          arguments:args
                                    binaryMessenger:_messenger];
    playerView = player.view;
    if (_aliPlayer) {
        _aliPlayer.playerView = playerView;
    }
    if (_aliListPlayer) {
        _aliListPlayer.playerView = playerView;
    }
    return player;
}

- (void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result atObj:(NSObject*)player{
    NSString* method = [call method];
    SEL methodSel=NSSelectorFromString([NSString stringWithFormat:@"%@:",method]);
    NSArray *arr = @[call,result,player];
    if([self respondsToSelector:methodSel]){
        IMP imp = [self methodForSelector:methodSel];
        void (*func)(id, SEL, NSArray*) = (void *)imp;
        func(self, methodSel, arr);
    }else{
        result(FlutterMethodNotImplemented);
    }
}


- (void)initService:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    FlutterResult result = arr[1];
    FlutterStandardTypedData* fdata = [call arguments];
    [AliPrivateService initKeyWithData:fdata.data];
    result(nil);
}

- (void)setUrl:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSString* url = [call arguments];
    AVPUrlSource *source = [[AVPUrlSource alloc] urlWithString:url];
    [player setUrlSource:source];
}

- (void)prepare:(NSArray*)arr {
    AliPlayer *player = arr[2];
    [player prepare];
}

- (void)play:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    [player start];
    result(nil);
}

- (void)pause:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    [player pause];
    result(nil);
}

- (void)stop:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    [player stop];
    result(nil);
}

- (void)destroy:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    [player destroy];
    if([player isKindOfClass:AliListPlayer.class]){
        self.aliListPlayer = nil;
    }else{
        self.aliPlayer = nil;
    }
    result(nil);
}

-(void)enableMix:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    FlutterResult result = arr[1];
    NSNumber* val = [call arguments];
    self.enableMix = val.boolValue;
    if (val.boolValue) {
        [AliPlayer setAudioSessionDelegate:self];
    }else{
        [AliPlayer setAudioSessionDelegate:nil];
    }
    result(nil);
}

- (void)isLoop:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    result(@([player isLoop]));
}

- (void)setLoop:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* isLoop = [call arguments];
    [player setLoop:isLoop.boolValue];
}

- (void)isAutoPlay:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    result(@([player isAutoPlay]));
}

- (void)setAutoPlay:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player setAutoPlay:val.boolValue];
}

- (void)isMuted:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    result(@([player isMuted]));
}

- (void)setMuted:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    NSNumber* val = [call arguments];
    AliPlayer *player = arr[2];
    [player setMuted:val.boolValue];
}

- (void)enableHardwareDecoder:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    result(@([player enableHardwareDecoder]));
}

- (void)setEnableHardwareDecoder:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player setEnableHardwareDecoder:val.boolValue];
}

- (void)getRotateMode:(NSArray*)arr {
    AliPlayer *player = arr[2];
    FlutterResult result = arr[1];
    result(@(player.rotateMode));
}

- (void)setRotateMode:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player setRotateMode:val.intValue];
}

- (void)getScalingMode:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    int mode = 0;
    switch (player.scalingMode) {
        case AVP_SCALINGMODE_SCALEASPECTFIT:
            mode = 0;
            break;
        case AVP_SCALINGMODE_SCALEASPECTFILL:
            mode = 1;
            break;
        case AVP_SCALINGMODE_SCALETOFILL:
            mode = 2;
            break;
            
        default:
            break;
    }
    result(@(mode));
}

- (void)setScalingMode:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
//    与android保持一致
    int mode = AVP_SCALINGMODE_SCALEASPECTFIT;
    switch (val.intValue) {
        case 0:
            mode = AVP_SCALINGMODE_SCALEASPECTFIT;
            break;
        case 1:
            mode = AVP_SCALINGMODE_SCALEASPECTFILL;
            break;
        case 2:
            mode = AVP_SCALINGMODE_SCALETOFILL;
            break;
            
        default:
            break;
    }
    [player setScalingMode:mode];
    result(nil);
}

- (void)getMirrorMode:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    result(@(player.mirrorMode));
}

- (void)setMirrorMode:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player setMirrorMode:val.intValue];
}

- (void)getRate:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    result(@(player.rate));
}

- (void)setRate:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player setRate:val.floatValue];
}

- (void)snapshot:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSString* val = [call arguments];
    mSnapshotPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
    if (val.length>0) {
        mSnapshotPath = [mSnapshotPath stringByAppendingPathComponent:val];
    }
    [player snapShot];
}

- (void)createThumbnailHelper:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSString* val = [call arguments];
    [player setThumbnailUrl:val];
    self.eventSink(@{kAliPlayerMethod:@"thumbnail_onPrepared_Success"});
}

- (void)requestBitmapAtPosition:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player getThumbnail:val.integerValue];
}

- (void)getVolume:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    result(@(player.volume));
}

- (void)setVolume:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player setVolume:val.floatValue];
}

- (void)setVideoBackgroundColor:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    int c = val.intValue;
    UIColor *color = [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0];
    [player setVideoBackgroundColor:color];
}

-(void)getSDKVersion:(NSArray*)arr{
    FlutterResult result = arr[1];
    result([AliPlayer getSDKVersion]);
}

- (void)enableConsoleLog:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    NSNumber* val = [call arguments];
    [AliPlayer setEnableLog:val.boolValue];
}

- (void)getLogLevel:(NSArray*)arr {
    FlutterResult result = arr[1];
    //TODO 拿不到
    result(@(-1));
}

- (void)setLogLevel:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    NSNumber* val = [call arguments];
    [AliPlayer setLogCallbackInfo:val.intValue callbackBlock:nil];
}

- (void)seekTo:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSDictionary* dic = [call arguments];
    NSNumber *position = dic[@"position"];
    NSNumber *seekMode = dic[@"seekMode"];
    [player seekToTime:position.integerValue seekMode:seekMode.intValue];
}

//TODO 应该是根据已经有的key 替换比较合理
- (void)setConfig:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSDictionary* val = [call arguments];
    AVPConfig *config = [player getConfig];
    
    [AVPConfig mj_setupReplacedKeyFromPropertyName:^NSDictionary *{
        return @{
            @"httpProxy" : @"mHttpProxy",
            @"referer" :@"mReferrer",
            @"networkTimeout" :@"mNetworkTimeout",
            @"highBufferDuration":@"mHighBufferDuration",
            @"maxDelayTime" :@"mMaxDelayTime",
            @"maxBufferDuration" :@"mMaxBufferDuration",
            @"startBufferDuration" :@"mStartBufferDuration",
            @"maxProbeSize" :@"mMaxProbeSize",
            @"maxProbeSize" :@"mMaxProbeSize",
            @"clearShowWhenStop" :@"mClearFrameWhenStop",
            @"enableVideoTunnelRender" :@"mEnableVideoTunnelRender",
            @"enableSEI" :@"mEnableSEI",
            @"userAgent" :@"mUserAgent",
            @"networkRetryCount" :@"mNetworkRetryCount",
            @"liveStartIndex" :@"mLiveStartIndex",
            @"customHeaders" :@"mCustomHeaders",
            @"disableAudio":@"mDisableAudio",
            @"disableVideo":@"mDisableVideo",
        };
    }];
    
    config = [AVPConfig mj_objectWithKeyValues:val];
    
    [player setConfig:config];
    
}

//- (void)getCacheConfig:(NSArray*)arr {
//    FlutterResult result = arr[1];
//    AliPlayer *player = arr[2];
//    [AVPCacheConfig mj_setupReplacedKeyFromPropertyName:^NSDictionary *{
//        return @{
//                 @"enable" : @"mEnable",
//                 @"path" :@"mDir",
//                 @"maxSizeMB" :@"mMaxSizeMB",
//                 @"maxDuration" :@"mMaxDurationS",
//                 };
//    }];
//    result(config.mj_keyValues);
//}

- (void)setCacheConfig:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSDictionary* val = [call arguments];
    
    [AVPCacheConfig mj_setupReplacedKeyFromPropertyName:^NSDictionary *{
        return @{
            @"enable" : @"mEnable",
            @"path" :@"mDir",
            @"maxSizeMB" :@"mMaxSizeMB",
            @"maxDuration" :@"mMaxDurationS",
        };
    }];
    AVPCacheConfig *config = [AVPCacheConfig mj_objectWithKeyValues:val];
    NSString *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
    [config setPath:[path stringByAppendingPathComponent:config.path]];
    
    [player setCacheConfig:config];
    
}

- (void)getConfig:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    AVPConfig *config = [player getConfig];
    
    [AVPConfig mj_setupReplacedKeyFromPropertyName:^NSDictionary *{
        return @{
            @"httpProxy" : @"mHttpProxy",
            @"referer" :@"mReferrer",
            @"networkTimeout" :@"mNetworkTimeout",
            @"highBufferDuration":@"mHighBufferDuration",
            @"maxDelayTime" :@"mMaxDelayTime",
            @"maxBufferDuration" :@"mMaxBufferDuration",
            @"startBufferDuration" :@"mStartBufferDuration",
            @"maxProbeSize" :@"mMaxProbeSize",
            @"maxProbeSize" :@"mMaxProbeSize",
            @"clearShowWhenStop" :@"mClearFrameWhenStop",
            @"enableVideoTunnelRender" :@"mEnableVideoTunnelRender",
            @"enableSEI" :@"mEnableSEI",
            @"userAgent" :@"mUserAgent",
            @"networkRetryCount" :@"mNetworkRetryCount",
            @"liveStartIndex" :@"mLiveStartIndex",
            @"customHeaders" :@"mCustomHeaders",
        };
    }];
    result(config.mj_keyValues);
}

-(void)setSource:(AVPSource*)source withDefinitions:(NSDictionary*)dic{
    NSArray *definitionList = [dic objectForKey:@"definitionList"];
    if (definitionList && [definitionList isKindOfClass:NSArray.class] && definitionList.count>0) {
        NSMutableString *mutStr = @"".mutableCopy;
        for (NSString *str in definitionList) {
            [mutStr appendString:str];
            [mutStr appendString:@","];
        }
        [mutStr deleteCharactersInRange:NSMakeRange(mutStr.length-1, 1)];
        [source setDefinitions:mutStr];
    }
}

- (void)setVidSts:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSDictionary *dic = call.arguments;
    AVPVidStsSource *source = [AVPVidStsSource mj_objectWithKeyValues:dic];
    
    NSString *previewTime = [dic getStrByKey:@"previewTime"];
    if(previewTime && previewTime.length>0){
        VidPlayerConfigGenerator* vp = [[VidPlayerConfigGenerator alloc] init];
        [vp setPreviewTime:previewTime.intValue];
        source.playConfig = [vp generatePlayerConfig];
    }
    
    [self setSource:source withDefinitions:dic];
    [player setStsSource:source];
}

- (void)setVidAuth:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    NSDictionary *dic = call.arguments;
    AVPVidAuthSource *source = [AVPVidAuthSource mj_objectWithKeyValues:dic];
    
    NSString *previewTime = [dic getStrByKey:@"previewTime"];
    if(previewTime && previewTime.length>0){
        VidPlayerConfigGenerator* vp = [[VidPlayerConfigGenerator alloc] init];
        [vp setPreviewTime:previewTime.intValue];
        source.playConfig = [vp generatePlayerConfig];
    }
    
    [self setSource:source withDefinitions:dic];
    [player setAuthSource:source];
}

- (void)setVidMps:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliPlayer *player = arr[2];
    AVPVidMpsSource *source = [[AVPVidMpsSource alloc] init];
    NSDictionary *dic = [call.arguments removeNull];
    [source setVid:dic[@"vid"]];
    [source setAccId:dic[@"accessKeyId"]];
    [source setRegion:dic[@"region"]];
    [source setStsToken:dic[@"securityToken"]];
    [source setAccSecret:dic[@"accessKeySecret"]];
    [source setPlayDomain:dic[@"playDomain"]];
    [source setAuthInfo:dic[@"authInfo"]];
    [source setMtsHlsUriToken:dic[@"hlsUriToken"]];
    [self setSource:source withDefinitions:dic];
    [player setMpsSource:source];
}

- (void)addVidSource:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliListPlayer *player = arr[2];
    NSDictionary *dic = [call arguments];
    [player addVidSource:dic[@"vid"] uid:dic[@"uid"]];
}

- (void)addUrlSource:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliListPlayer *player = arr[2];
    NSDictionary *dic = [call arguments];
    [player addUrlSource:dic[@"url"] uid:dic[@"uid"]];
}

- (void)moveTo:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliListPlayer *player = arr[2];
    NSDictionary *dic = [[call arguments] removeNull];
    
    NSString *aacId = [dic getStrByKey:@"accId"];
    if (aacId.length>0) {
        [player moveTo:dic[@"uid"] accId:dic[@"accId"] accKey:dic[@"accKey"] token:dic[@"token"] region:dic[@"region"]];
    }else{
        [player moveTo:dic[@"uid"]];
    }
}

- (void)moveToNext:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliListPlayer *player = arr[2];
    NSDictionary *dic = [[call arguments] removeNull];
    [player moveToNext:dic[@"accId"] accKey:dic[@"accKey"] token:dic[@"token"] region:dic[@"region"]];
}

- (void)setPreloadCount:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliListPlayer *player = arr[2];
    NSNumber* val = [call arguments];
    [player setPreloadCount:val.intValue];
}

- (void)getMediaInfo:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    AVPMediaInfo * info = [player getMediaInfo];
    
    //TODO 后面需要统一键值转换规则
    [AVPMediaInfo mj_setupReplacedKeyFromPropertyName:^NSDictionary *{
        return @{
            @"mTitle":@"title",
            @"mCoverUrl":@"coverURL",
            @"mTrackInfos":@"tracks",
        };
    }];
    
    [AVPTrackInfo mj_setupReplacedKeyFromPropertyName:^NSDictionary *{
        return @{
            @"vodDefinition":@"trackDefinition",
            @"index":@"trackIndex",
        };
    }];
    
    [AVPThumbnailInfo mj_setupReplacedKeyFromPropertyName:^NSDictionary *{
        return @{
            @"URL" : @"url",
        };
    }];
    NSLog(@"getMediaInfo==%@",info.mj_JSONString);
    result(info.mj_keyValues);
}

- (void)getCurrentTrack:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    FlutterResult result = arr[1];
    AliPlayer *player = arr[2];
    NSNumber *idxNum = call.arguments;
    AVPTrackInfo * info = [player getCurrentTrack:idxNum.intValue];
    NSLog(@"getCurrentTrack==%@",info.mj_JSONString);
    result(info.mj_keyValues);
}

- (void)selectTrack:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliListPlayer *player = arr[2];
    NSDictionary *dic = [[call arguments] removeNull];
    NSNumber *trackIdxNum = dic[@"trackIdx"];
    NSNumber *accurateNum = dic[@"accurate"];
    if (accurateNum.intValue==-1) {
        [player selectTrack:trackIdxNum.intValue];
    }else{
        [player selectTrack:trackIdxNum.intValue accurate:accurateNum.boolValue];
    }
    
}

- (void)addExtSubtitle:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    AliListPlayer *player = arr[2];
    NSString *url = [call arguments];
    [player addExtSubtitle:url];
}

- (void)selectExtSubtitle:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    FlutterResult result = arr[1];
    AliListPlayer *player = arr[2];
    NSDictionary *dic = [[call arguments] removeNull];
    NSNumber *trackIdxNum = dic[@"trackIndex"];
    NSNumber *enableNum = dic[@"enable"];
    [player selectExtSubtitle:trackIdxNum.intValue enable:enableNum.boolValue];
    result(nil);
}

- (void)setStreamDelayTime:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    FlutterResult result = arr[1];
    AliListPlayer *player = arr[2];
    NSDictionary *dic = [[call arguments] removeNull];
    NSNumber *trackIdxNum = dic[@"index"];
    NSNumber *timeNum = dic[@"time"];
    [player setStreamDelayTime:trackIdxNum.intValue time:timeNum.intValue];
    result(nil);
}

- (void)setPreferPlayerName:(NSArray*)arr {
    FlutterMethodCall* call = arr.firstObject;
    FlutterResult result = arr[1];
    AliListPlayer *player = arr[2];
    NSString *playerName = [call arguments];
    [player setPreferPlayerName:playerName];
    result(nil);
}

- (void)getPlayerName:(NSArray*)arr {
    FlutterResult result = arr[1];
    AliListPlayer *player = arr[2];
    result([player getPlayerName]);
}

#pragma --mark getters
- (AliPlayer *)aliPlayer{
    if (!_aliPlayer) {
        _aliPlayer = [[AliPlayer alloc] init];
        _aliPlayer.scalingMode =  AVP_SCALINGMODE_SCALEASPECTFIT;
        _aliPlayer.rate = 1;
        _aliPlayer.delegate = self;
        _aliPlayer.playerView = playerView;
    }
    return _aliPlayer;
}

- (AliListPlayer*) aliListPlayer{
    if(!_aliListPlayer){
        _aliListPlayer = [[AliListPlayer alloc] init];
        _aliListPlayer.scalingMode =  AVP_SCALINGMODE_SCALEASPECTFIT;
        _aliListPlayer.rate = 1;
        _aliListPlayer.delegate = self;
        _aliListPlayer.playerView = playerView;
        _aliListPlayer.stsPreloadDefinition = @"FD";
    }
    return _aliListPlayer;
}


#pragma mark AVPDelegate

/**
 @brief 播放器状态改变回调
 @param player 播放器player指针
 @param oldStatus 老的播放器状态 参考AVPStatus
 @param newStatus 新的播放器状态 参考AVPStatus
 */
- (void)onPlayerStatusChanged:(AliPlayer*)player oldStatus:(AVPStatus)oldStatus newStatus:(AVPStatus)newStatus {
    self.eventSink(@{kAliPlayerMethod:@"onStateChanged",@"newState":@(newStatus)});
}

/**
 @brief 错误代理回调
 @param player 播放器player指针
 @param errorModel 播放器错误描述，参考AliVcPlayerErrorModel
 */
- (void)onError:(AliPlayer*)player errorModel:(AVPErrorModel *)errorModel {
    self.eventSink(@{kAliPlayerMethod:@"onError",@"errorCode":@(errorModel.code),@"errorMsg":errorModel.message});
}

- (void)onSEIData:(AliPlayer*)player type:(int)type data:(NSData *)data {
    NSString *str = [NSString stringWithUTF8String:data.bytes];
    NSLog(@"SEI: %@", str);
}

/**
 @brief 播放器事件回调
 @param player 播放器player指针
 @param eventType 播放器事件类型，@see AVPEventType
 */
-(void)onPlayerEvent:(AliPlayer*)player eventType:(AVPEventType)eventType {
    switch (eventType) {
        case AVPEventPrepareDone:
            self.eventSink(@{kAliPlayerMethod:@"onPrepared"});
            break;
        case AVPEventFirstRenderedStart:
            self.eventSink(@{kAliPlayerMethod:@"onRenderingStart"});
            break;
        case AVPEventLoadingStart:
            self.eventSink(@{kAliPlayerMethod:@"onLoadingBegin"});
            break;
        case AVPEventLoadingEnd:
            self.eventSink(@{kAliPlayerMethod:@"onLoadingEnd"});
            break;
        case AVPEventCompletion:
            self.eventSink(@{kAliPlayerMethod:@"onCompletion"});
            break;
        case AVPEventSeekEnd:
            self.eventSink(@{kAliPlayerMethod:@"onSeekComplete"});
            break;
        default:
            break;
    }
}

/**
 @brief 播放器事件回调
 @param player 播放器player指针
 @param eventWithString 播放器事件类型
 @param description 播放器事件说明
 @see AVPEventType
 */
-(void)onPlayerEvent:(AliPlayer*)player eventWithString:(AVPEventWithString)eventWithString description:(NSString *)description {
    self.eventSink(@{kAliPlayerMethod:@"onInfo",@"infoCode":@(eventWithString),@"extraMsg":description});
}

/**
 @brief 视频当前播放位置回调
 @param player 播放器player指针
 @param position 视频当前播放位置
 */
- (void)onCurrentPositionUpdate:(AliPlayer*)player position:(int64_t)position {
     self.eventSink(@{kAliPlayerMethod:@"onInfo",@"infoCode":@(2),@"extraValue":@(position)});
}

/**
 @brief 视频缓存位置回调
 @param player 播放器player指针
 @param position 视频当前缓存位置
 */
- (void)onBufferedPositionUpdate:(AliPlayer*)player position:(int64_t)position {
    self.eventSink(@{kAliPlayerMethod:@"onInfo",@"infoCode":@(1),@"extraValue":@(position)});
}

/**
 @brief 获取track信息回调
 @param player 播放器player指针
 @param info track流信息数组 参考AVPTrackInfo
 */
- (void)onTrackReady:(AliPlayer*)player info:(NSArray<AVPTrackInfo*>*)info {
    self.eventSink(@{kAliPlayerMethod:@"onTrackReady"});
}

/**
 @brief 外挂字幕被添加
 @param player 播放器player指针
 @param trackIndex 字幕显示的索引号
 @param URL 字幕url
 */
- (void)onSubtitleExtAdded:(AliPlayer*)player trackIndex:(int)trackIndex URL:(NSString *)URL {
    self.eventSink(@{kAliPlayerMethod:@"onSubtitleExtAdded",@"trackIndex":@(trackIndex),@"url":URL});
}

/**
 @brief 实时网速回调
 @param speed 实时网速
 */
- (void)onCurrentDownloadSpeed:(AliPlayer *)player speed:(int64_t)speed {
    self.eventSink(@{kAliPlayerMethod:@"onCurrentDownloadSpeed",@"netSpeed":@(speed)});
}
/**
 @brief 字幕显示回调
 @param player 播放器player指针
 @param trackIndex 字幕流索引.
 @param subtitleID  字幕ID.
 @param subtitle 字幕显示的字符串
 */
- (void)onSubtitleShow:(AliPlayer*)player trackIndex:(int)trackIndex subtitleID:(long)subtitleID subtitle:(NSString *)subtitle {
    self.eventSink(@{kAliPlayerMethod:@"onSubtitleShow",@"trackIndex":@(trackIndex),@"subtitleID":@(subtitleID),@"subtitle":subtitle});
}

/**
 @brief 字幕隐藏回调
 @param player 播放器player指针
 @param trackIndex 字幕流索引.
 @param subtitleID  字幕ID.
 */
- (void)onSubtitleHide:(AliPlayer*)player trackIndex:(int)trackIndex subtitleID:(long)subtitleID {
    self.eventSink(@{kAliPlayerMethod:@"onSubtitleHide",@"trackIndex":@(trackIndex),@"subtitleID":@(subtitleID)});
}

/**
 @brief 获取截图回调
 @param player 播放器player指针
 @param image 图像
 */
- (void)onCaptureScreen:(AliPlayer *)player image:(UIImage *)image {
    BOOL result =[UIImagePNGRepresentation(image)writeToFile:mSnapshotPath atomically:YES]; // 保存成功会返回YES
    if (result == YES) {
        self.eventSink(@{kAliPlayerMethod:@"onSnapShot",@"snapShotPath":mSnapshotPath});
    }
}

/**
 @brief track切换完成回调
 @param player 播放器player指针
 @param info 切换后的信息 参考AVPTrackInfo
 */
- (void)onTrackChanged:(AliPlayer*)player info:(AVPTrackInfo*)info {
    NSLog(@"onTrackChanged==%@",info.mj_JSONString);
    self.eventSink(@{kAliPlayerMethod:@"onTrackChanged",@"info":info.mj_keyValues});
}

/**
 @brief 获取缩略图成功回调
 @param positionMs 指定的缩略图位置
 @param fromPos 此缩略图的开始位置
 @param toPos 此缩略图的结束位置
 @param image 缩图略图像指针,对于mac是NSImage，iOS平台是UIImage指针
 */
- (void)onGetThumbnailSuc:(int64_t)positionMs fromPos:(int64_t)fromPos toPos:(int64_t)toPos image:(id)image {
    NSData *imageData = UIImageJPEGRepresentation(image,1);
//    FlutterStandardTypedData * fdata = [FlutterStandardTypedData typedDataWithBytes:imageData];
    self.eventSink(@{kAliPlayerMethod:@"onThumbnailGetSuccess",@"thumbnailRange":@[@(fromPos),@(toPos)],@"thumbnailbitmap":imageData});
}

/**
 @brief 获取缩略图失败回调
 @param positionMs 指定的缩略图位置
 */
- (void)onGetThumbnailFailed:(int64_t)positionMs {
    self.eventSink(@{kAliPlayerMethod:@"onThumbnailGetFail"});
}

/**
 @brief 视频缓冲进度回调
 @param player 播放器player指针
 @param progress 缓存进度0-100
 */
- (void)onLoadingProgress:(AliPlayer*)player progress:(float)progress {
    self.eventSink(@{kAliPlayerMethod:@"onLoadingProgress",@"percent":@((int)progress)});
}


#pragma --mark CicadaAudioSessionDelegate
- (BOOL)setActive:(BOOL)active error:(NSError **)outError
{
    return [[AVAudioSession sharedInstance] setActive:active error:outError];
}

- (BOOL)setCategory:(NSString *)category withOptions:(AVAudioSessionCategoryOptions)options error:(NSError **)outError
{
    if (self.enableMix) {
        options = AVAudioSessionCategoryOptionMixWithOthers | AVAudioSessionCategoryOptionDuckOthers;
    }
    return [[AVAudioSession sharedInstance] setCategory:category withOptions:options error:outError];
}

- (BOOL)setCategory:(AVAudioSessionCategory)category mode:(AVAudioSessionMode)mode routeSharingPolicy:(AVAudioSessionRouteSharingPolicy)policy options:(AVAudioSessionCategoryOptions)options error:(NSError **)outError
{
    if (self.enableMix) {
        return YES;
    }

    if (@available(iOS 11.0, tvOS 11.0, *)) {
        return [[AVAudioSession sharedInstance] setCategory:category mode:mode routeSharingPolicy:policy options:options error:outError];
    }
    return NO;
}

@end

