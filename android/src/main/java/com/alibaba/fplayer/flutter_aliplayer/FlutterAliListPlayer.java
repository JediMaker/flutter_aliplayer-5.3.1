package com.alibaba.fplayer.flutter_aliplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;

import com.aliyun.player.AliListPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.VidPlayerConfigGen;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.nativeclass.CacheConfig;
import com.aliyun.player.nativeclass.MediaInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.nativeclass.Thumbnail;
import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.player.source.StsInfo;
import com.aliyun.player.source.UrlSource;
import com.aliyun.player.source.VidAuth;
import com.aliyun.player.source.VidMps;
import com.aliyun.player.source.VidSts;
import com.aliyun.thumbnail.ThumbnailBitmapInfo;
import com.aliyun.thumbnail.ThumbnailHelper;
import com.aliyun.utils.ThreadManager;
import com.cicada.player.utils.Logger;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class FlutterAliListPlayer implements EventChannel.StreamHandler, MethodChannel.MethodCallHandler {

    private FlutterPlugin.FlutterPluginBinding mFlutterPluginBinding;

    private final Gson mGson;
    private Context mContext;
    private EventChannel.EventSink mEventSink;
    private EventChannel mEventChannel;
    private AliListPlayer mAliListPlayer;
    private String mSnapShotPath;
    private ThumbnailHelper mThumbnailHelper;

    public FlutterAliListPlayer(FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        this.mFlutterPluginBinding = flutterPluginBinding;
        this.mContext = flutterPluginBinding.getApplicationContext();
        mGson = new Gson();
        mAliListPlayer = AliPlayerFactory.createAliListPlayer(flutterPluginBinding.getApplicationContext());
        MethodChannel mAliListPlayerMethodChannel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(),"flutter_alilistplayer");
        mAliListPlayerMethodChannel.setMethodCallHandler(this);
        mEventChannel = new EventChannel(mFlutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_aliplayer_event");
        mEventChannel.setStreamHandler(this);
        initListener(mAliListPlayer);
    }

    public AliListPlayer getAliListPlayer(){
        return mAliListPlayer;
    }

    private void initListener(final IPlayer player){
        player.setOnPreparedListener(new IPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onPrepared");
                mEventSink.success(map);
            }
        });

        player.setOnRenderingStartListener(new IPlayer.OnRenderingStartListener() {
            @Override
            public void onRenderingStart() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onRenderingStart");
                mEventSink.success(map);
            }
        });

        player.setOnVideoSizeChangedListener(new IPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(int width, int height) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onVideoSizeChanged");
                map.put("width",width);
                map.put("height",height);
                mEventSink.success(map);
            }
        });

        player.setOnSnapShotListener(new IPlayer.OnSnapShotListener() {
            @Override
            public void onSnapShot(final Bitmap bitmap, int width, int height) {
                final Map<String,Object> map = new HashMap<>();
                map.put("method","onSnapShot");
                map.put("snapShotPath",mSnapShotPath);

                ThreadManager.threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        File f = new File(mSnapShotPath);
                        FileOutputStream out = null;
                        if (f.exists()) {
                            f.delete();
                        }
                        try {
                            out = new FileOutputStream(f);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally{
                            if(out != null){
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

                mEventSink.success(map);

            }
        });

        player.setOnTrackChangedListener(new IPlayer.OnTrackChangedListener() {
            @Override
            public void onChangedSuccess(TrackInfo trackInfo) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onChangedSuccess");
                //TODO
                mEventSink.success(map);
            }

            @Override
            public void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onChangedFail");
                //TODO
                mEventSink.success(map);
            }
        });

        player.setOnSeekCompleteListener(new IPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onSeekComplete");
                mEventSink.success(map);
            }
        });

        player.setOnSeiDataListener(new IPlayer.OnSeiDataListener() {
            @Override
            public void onSeiData(int type, byte[] bytes) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onSeiData");
                //TODO
                mEventSink.success(map);
            }
        });

        player.setOnLoadingStatusListener(new IPlayer.OnLoadingStatusListener() {
            @Override
            public void onLoadingBegin() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onLoadingBegin");
                mEventSink.success(map);
            }

            @Override
            public void onLoadingProgress(int percent, float netSpeed) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onLoadingProgress");
                map.put("percent",percent);
                map.put("netSpeed",netSpeed);
                mEventSink.success(map);
            }

            @Override
            public void onLoadingEnd() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onLoadingEnd");
                mEventSink.success(map);
            }
        });

        player.setOnStateChangedListener(new IPlayer.OnStateChangedListener() {
            @Override
            public void onStateChanged(int newState) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onStateChanged");
                map.put("newState",newState);
                mEventSink.success(map);
            }
        });

        player.setOnSubtitleDisplayListener(new IPlayer.OnSubtitleDisplayListener() {
            @Override
            public void onSubtitleExtAdded(int trackIndex, String url) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onSubtitleExtAdded");
                map.put("trackIndex",trackIndex);
                map.put("url",url);
                mEventSink.success(map);
            }

            @Override
            public void onSubtitleShow(int trackIndex, long id, String data) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onSubtitleShow");
                map.put("trackIndex",trackIndex);
                map.put("subtitleID",id);
                map.put("subtitle",data);
                mEventSink.success(map);
            }

            @Override
            public void onSubtitleHide(int trackIndex, long id) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onSubtitleHide");
                map.put("trackIndex",trackIndex);
                map.put("subtitleID",id);
                mEventSink.success(map);
            }
        });

        player.setOnInfoListener(new IPlayer.OnInfoListener() {
            @Override
            public void onInfo(InfoBean infoBean) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onInfo");
                map.put("infoCode",infoBean.getCode().getValue());
                map.put("extraValue",infoBean.getExtraValue());
                map.put("extraMsg",infoBean.getExtraMsg());
                mEventSink.success(map);
            }
        });

        player.setOnErrorListener(new IPlayer.OnErrorListener() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onError");
                map.put("errorCode",errorInfo.getCode().getValue());
                map.put("errorExtra",errorInfo.getExtra());
                map.put("errorMsg",errorInfo.getMsg());
                mEventSink.success(map);
            }
        });

        player.setOnTrackReadyListener(new IPlayer.OnTrackReadyListener() {
            @Override
            public void onTrackReady(MediaInfo mediaInfo) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onTrackReady");
                mEventSink.success(map);
            }
        });

        player.setOnCompletionListener(new IPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onCompletion");
                mEventSink.success(map);
            }
        });

    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        this.mEventSink = events;
    }

    @Override
    public void onCancel(Object arguments) {
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "setPreloadCount":
                Integer count = (Integer) methodCall.arguments;
                setPreloadCount(count);
                break;
            case "createAliPlayer":
                break;
            case "setUrl":
                String url = methodCall.arguments.toString();
                setDataSource(url);
                break;
            case "setVidSts":
                Map<String,String> stsMap = (Map<String,String>)methodCall.arguments;
                VidSts vidSts = new VidSts();
                vidSts.setRegion(stsMap.get("region"));
                vidSts.setVid(stsMap.get("vid"));
                vidSts.setAccessKeyId(stsMap.get("accessKeyId"));
                vidSts.setAccessKeySecret(stsMap.get("accessKeySecret"));
                vidSts.setSecurityToken(stsMap.get("securityToken"));

                if(stsMap.containsKey("previewTime") && !TextUtils.isEmpty(stsMap.get("previewTime"))){
                    VidPlayerConfigGen vidPlayerConfigGen = new VidPlayerConfigGen();
                    int previewTime = Integer.valueOf(stsMap.get("previewTime"));
                    vidPlayerConfigGen.setPreviewTime(previewTime);
                    vidSts.setPlayConfig(vidPlayerConfigGen);
                }
                setDataSource(vidSts);
                break;
            case "setVidAuth":
                Map<String,String> authMap = (Map<String,String>)methodCall.arguments;
                VidAuth vidAuth = new VidAuth();
                vidAuth.setVid(authMap.get("vid"));
                vidAuth.setRegion(authMap.get("region"));
                vidAuth.setPlayAuth(authMap.get("playAuth"));
                if(authMap.containsKey("previewTime") && !TextUtils.isEmpty(authMap.get("previewTime"))){
                    VidPlayerConfigGen vidPlayerConfigGen = new VidPlayerConfigGen();
                    int previewTime = Integer.valueOf(authMap.get("previewTime"));
                    vidPlayerConfigGen.setPreviewTime(previewTime);
                    vidAuth.setPlayConfig(vidPlayerConfigGen);
                }
                setDataSource(vidAuth);
                break;
            case "setVidMps":
                Map<String,String> mpsMap = (Map<String,String>)methodCall.arguments;
                VidMps vidMps = new VidMps();
                vidMps.setMediaId(mpsMap.get("vid"));
                vidMps.setRegion(mpsMap.get("region"));
                vidMps.setAccessKeyId(mpsMap.get("accessKeyId"));
                vidMps.setAccessKeySecret(mpsMap.get("accessKeySecret"));
                if(mpsMap.containsKey("playDomain") && !TextUtils.isEmpty(mpsMap.get("playDomain"))){
                    vidMps.setPlayDomain(mpsMap.get("playDomain"));
                }
                vidMps.setAuthInfo(mpsMap.get("authInfo"));
                vidMps.setHlsUriToken(mpsMap.get("hlsUriToken"));
                vidMps.setSecurityToken(mpsMap.get("securityToken"));
                setDataSource(vidMps);
                break;
            case "prepare":
                prepare();
                break;
            case "play":
                start();
                break;
            case "pause":
                pause();
                break;
            case "stop":
                stop();
                break;
            case "destroy":
                release();
                break;
            case "seekTo":
            {
                Map<String,Object> seekToMap = (Map<String,Object>)methodCall.arguments;
                Integer position = (Integer) seekToMap.get("position");
                Integer seekMode = (Integer) seekToMap.get("seekMode");
                seekTo(position,seekMode);
            }
            break;
            case "getMediaInfo":
            {
                MediaInfo mediaInfo = getMediaInfo();
                if(mediaInfo != null){
                    Map<String,Object> getMediaInfoMap = new HashMap<>();
                    getMediaInfoMap.put("title",mediaInfo.getTitle());
                    getMediaInfoMap.put("status",mediaInfo.getStatus());
                    getMediaInfoMap.put("mediaType",mediaInfo.getMediaType());
                    getMediaInfoMap.put("duration",mediaInfo.getDuration());
                    getMediaInfoMap.put("transcodeMode",mediaInfo.getTransCodeMode());
                    getMediaInfoMap.put("coverURL",mediaInfo.getCoverUrl());
                    List<Thumbnail> thumbnail = mediaInfo.getThumbnailList();
                    List<Map<String,Object>> thumbailList = new ArrayList<>();
                    for (Thumbnail thumb : thumbnail) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("url",thumb.mURL);
                        thumbailList.add(map);
                        getMediaInfoMap.put("thumbnails",thumbailList);
                    }
                    List<TrackInfo> trackInfos = mediaInfo.getTrackInfos();
                    List<Map<String,Object>> trackInfoList = new ArrayList<>();
                    for (TrackInfo trackInfo : trackInfos) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("vodFormat",trackInfo.getVodFormat());
                        map.put("videoHeight",trackInfo.getVideoHeight());
                        map.put("videoWidth",trackInfo.getVideoHeight());
                        map.put("subtitleLanguage",trackInfo.getSubtitleLang());
                        map.put("trackBitrate",trackInfo.getVideoBitrate());
                        map.put("vodFileSize",trackInfo.getVodFileSize());
                        map.put("trackIndex",trackInfo.getIndex());
                        map.put("trackDefinition",trackInfo.getVodDefinition());
                        map.put("audioSampleFormat",trackInfo.getAudioSampleFormat());
                        map.put("audioLanguage",trackInfo.getAudioLang());
                        map.put("vodPlayUrl",trackInfo.getVodPlayUrl());
                        map.put("trackType",trackInfo.getType().ordinal());
                        map.put("audioSamplerate",trackInfo.getAudioSampleRate());
                        map.put("audioChannels",trackInfo.getAudioChannels());
                        trackInfoList.add(map);
                        getMediaInfoMap.put("tracks",trackInfoList);
                    }
                    result.success(getMediaInfoMap);
                }
            }
            break;
            case "snapshot":
                mSnapShotPath = methodCall.arguments.toString();
                snapshot();
                break;
            case "setLoop":
                setLoop((Boolean)methodCall.arguments);
                break;
            case "isLoop":
                result.success(isLoop());
                break;
            case "setAutoPlay":
                setAutoPlay((Boolean)methodCall.arguments);
                break;
            case "isAutoPlay":
                result.success(isAutoPlay());
                break;
            case "setMuted":
                setMuted((Boolean)methodCall.arguments);
                break;
            case "isMuted":
                result.success(isMuted());
                break;
            case "setEnableHardwareDecoder":
                Boolean setEnableHardwareDecoderArgumnt = (Boolean) methodCall.arguments;
                setEnableHardWareDecoder(setEnableHardwareDecoderArgumnt);
                break;
            case "setScalingMode":
                setScaleMode((Integer) methodCall.arguments);
                break;
            case "getScalingMode":
                result.success(getScaleMode());
                break;
            case "setMirrorMode":
                setMirrorMode((Integer) methodCall.arguments);
                break;
            case "getMirrorMode":
                result.success(getMirrorMode());
                break;
            case "setRotateMode":
                setRotateMode((Integer) methodCall.arguments);
                break;
            case "getRotateMode":
                result.success(getRotateMode());
                break;
            case "setRate":
                setSpeed((Double) methodCall.arguments);
                break;
            case "getRate":
                result.success(getSpeed());
                break;
            case "setVideoBackgroundColor":
                setVideoBackgroundColor((Integer) methodCall.arguments);
                break;
            case "setVolume":
                setVolume((Double) methodCall.arguments);
                break;
            case "getVolume":
                result.success(getVolume());
                break;
            case "setConfig":
            {
                Map<String,Object> setConfigMap = (Map<String, Object>) methodCall.arguments;
                PlayerConfig config = getConfig();
                if(config != null){
                    String configJson = mGson.toJson(setConfigMap);
                    config = mGson.fromJson(configJson,PlayerConfig.class);
                    setConfig(config);
                }
            }
            break;
            case "getConfig":
                PlayerConfig config = getConfig();
                String json = mGson.toJson(config);
                Map<String,Object> configMap = mGson.fromJson(json,Map.class);
                result.success(configMap);
                break;
            case "getCacheConfig":
                CacheConfig cacheConfig = getCacheConfig();
                String cacheConfigJson = mGson.toJson(cacheConfig);
                Map<String,Object> cacheConfigMap = mGson.fromJson(cacheConfigJson,Map.class);
                result.success(cacheConfigMap);
                break;
            case "setCacheConfig":
                Map<String,Object> setCacheConnfigMap = (Map<String, Object>) methodCall.arguments;
                String setCacheConfigJson = mGson.toJson(setCacheConnfigMap);
                CacheConfig setCacheConfig = mGson.fromJson(setCacheConfigJson,CacheConfig.class);
                setCacheConfig(setCacheConfig);
                break;
            case "getCurrentTrack":
                Integer currentTrackIndex = (Integer) methodCall.arguments;
                TrackInfo currentTrack = getCurrentTrack(currentTrackIndex);
                if(currentTrack != null){
                    Map<String,Object> map = new HashMap<>();
                    map.put("vodFormat",currentTrack.getVodFormat());
                    map.put("videoHeight",currentTrack.getVideoHeight());
                    map.put("videoWidth",currentTrack.getVideoHeight());
                    map.put("subtitleLanguage",currentTrack.getSubtitleLang());
                    map.put("trackBitrate",currentTrack.getVideoBitrate());
                    map.put("vodFileSize",currentTrack.getVodFileSize());
                    map.put("trackIndex",currentTrack.getIndex());
                    map.put("trackDefinition",currentTrack.getVodDefinition());
                    map.put("audioSampleFormat",currentTrack.getAudioSampleFormat());
                    map.put("audioLanguage",currentTrack.getAudioLang());
                    map.put("vodPlayUrl",currentTrack.getVodPlayUrl());
                    map.put("trackType",currentTrack.getType().ordinal());
                    map.put("audioSamplerate",currentTrack.getAudioSampleRate());
                    map.put("audioChannels",currentTrack.getAudioChannels());
                    result.success(map);
                }
                break;
            case "selectTrack":
                Map<String,Object> selectTrackMap = (Map<String, Object>) methodCall.arguments;
                Integer trackIdx = (Integer) selectTrackMap.get("trackIdx");
                Integer accurate = (Integer) selectTrackMap.get("accurate");
                selectTrack(trackIdx, accurate == 1);
                break;
            case "addExtSubtitle":
                String extSubtitlUrl = (String) methodCall.arguments;
                addExtSubtitle(extSubtitlUrl);
                break;
            case "selectExtSubtitle":
                Map<String,Object> selectExtSubtitleMap = (Map<String, Object>) methodCall.arguments;
                Integer trackIndex = (Integer) selectExtSubtitleMap.get("trackIndex");
                Boolean selectExtSubtitlEnable = (Boolean) selectExtSubtitleMap.get("enable");
                selectExtSubtitle(trackIndex,selectExtSubtitlEnable);
                break;
            case "addVidSource":
                String addSourceVid = methodCall.argument("vid");
                String vidUid = methodCall.argument("uid");
                addVidSource(addSourceVid,vidUid);
                break;
            case "addUrlSource":
                String addSourceUrl = methodCall.argument("url");
                String urlUid = methodCall.argument("uid");
                addUrlSource(addSourceUrl,urlUid);
                break;
            case "removeSource":
                String removeUid = methodCall.arguments();
                removeSource(removeUid);
                break;
            case "clear":
                clear();
                break;
            case "moveToNext":
                String moveToNextAccessKeyId = methodCall.argument("accId");
                String moveToNextAccessKeySecret = methodCall.argument("accKey");
                String moveToNextSecurityToken = methodCall.argument("token");
                String moveToNextRegion = methodCall.argument("region");
                StsInfo moveToNextStsInfo = new StsInfo();
                moveToNextStsInfo.setAccessKeyId(moveToNextAccessKeyId);
                moveToNextStsInfo.setAccessKeySecret(moveToNextAccessKeySecret);
                moveToNextStsInfo.setSecurityToken(moveToNextSecurityToken);
                moveToNextStsInfo.setRegion(moveToNextRegion);
                moveToNext(moveToNextStsInfo);
                break;
            case "moveToPre":
                String moveToPreAccessKeyId = methodCall.argument("accId");
                String moveToPreAccessKeySecret = methodCall.argument("accKey");
                String moveToPreSecurityToken = methodCall.argument("token");
                String moveToPreRegion = methodCall.argument("region");
                StsInfo moveToPreStsInfo = new StsInfo();
                moveToPreStsInfo.setAccessKeyId(moveToPreAccessKeyId);
                moveToPreStsInfo.setAccessKeySecret(moveToPreAccessKeySecret);
                moveToPreStsInfo.setSecurityToken(moveToPreSecurityToken);
                moveToPreStsInfo.setRegion(moveToPreRegion);
                moveToPre(moveToPreStsInfo);
                break;
            case "moveTo":
                String moveToAccessKeyId = methodCall.argument("accId");
                String moveToAccessKeySecret = methodCall.argument("accKey");
                String moveToSecurityToken = methodCall.argument("token");
                String moveToRegion = methodCall.argument("region");
                String moveToUid = methodCall.argument("uid");
                if(!TextUtils.isEmpty(moveToAccessKeyId)){
                    StsInfo moveToStsInfo = new StsInfo();
                    moveToStsInfo.setAccessKeyId(moveToAccessKeyId);
                    moveToStsInfo.setAccessKeySecret(moveToAccessKeySecret);
                    moveToStsInfo.setSecurityToken(moveToSecurityToken);
                    moveToStsInfo.setRegion(moveToRegion);
                    moveTo(moveToUid,moveToStsInfo);
                }else{
                    moveTo(moveToUid);
                }

                break;
            case "enableConsoleLog":
                Boolean enableLog = (Boolean) methodCall.arguments;
                enableConsoleLog(enableLog);
                break;
            case "setLogLevel":
                Integer level = (Integer) methodCall.arguments;
                setLogLevel(level);
                break;
            case "getLogLevel":
                result.success(getLogLevel());
                break;
            case "createDeviceInfo":
                result.success(createDeviceInfo());
                break;
            case "addBlackDevice":
                Map<String,String> addBlackDeviceMap = methodCall.arguments();
                String blackType = addBlackDeviceMap.get("black_type");
                String blackDevice = addBlackDeviceMap.get("black_device");
                addBlackDevice(blackType,blackDevice);
                break;
            case "createThumbnailHelper":
                String thhumbnailUrl = (String) methodCall.arguments;
                createThumbnailHelper(thhumbnailUrl);
                break;
            case "requestBitmapAtPosition":
                Integer requestBitmapProgress = (Integer) methodCall.arguments;
                requestBitmapAtPosition(requestBitmapProgress);
                break;
            default:
                result.notImplemented();
        }
    }

    private void setPreloadCount(int count){
        if(mAliListPlayer != null){
            mAliListPlayer.setPreloadCount(count);
        }
    }

    private void setDataSource(String url){
        if(mAliListPlayer != null){
            UrlSource urlSource = new UrlSource();
            urlSource.setUri(url);
            mAliListPlayer.setDataSource(urlSource);
        }
    }

    private void setDataSource(VidSts vidSts){
        if(mAliListPlayer != null){
            mAliListPlayer.setDataSource(vidSts);
        }
    }

    private void setDataSource(VidAuth vidAuth){
        if(mAliListPlayer != null){
            mAliListPlayer.setDataSource(vidAuth);
        }
    }

    private void setDataSource(VidMps vidMps){
        if(mAliListPlayer != null){
            mAliListPlayer.setDataSource(vidMps);
        }
    }

    private void prepare(){
        if(mAliListPlayer != null){
            mAliListPlayer.prepare();
        }
    }

    private void start(){
        if(mAliListPlayer != null){
            mAliListPlayer.start();
        }
    }

    private void pause(){
        if(mAliListPlayer != null){
            mAliListPlayer.pause();
        }
    }

    private void stop(){
        if(mAliListPlayer != null){
            mAliListPlayer.stop();
        }
    }

    private void release(){
        if(mAliListPlayer != null){
            mAliListPlayer.release();
            mAliListPlayer = null;
        }
    }

    private void seekTo(long position,int seekMode){
        if(mAliListPlayer != null){
            IPlayer.SeekMode mSeekMode;
            if(seekMode == IPlayer.SeekMode.Accurate.getValue()){
                mSeekMode = IPlayer.SeekMode.Accurate;
            }else{
                mSeekMode = IPlayer.SeekMode.Inaccurate;
            }
            mAliListPlayer.seekTo(position,mSeekMode);
        }
    }

    private MediaInfo getMediaInfo(){
        if(mAliListPlayer != null){
            return mAliListPlayer.getMediaInfo();
        }
        return null;
    }

    private void snapshot(){
        if(mAliListPlayer != null){
            mAliListPlayer.snapshot();
        }
    }

    private void setLoop(Boolean isLoop){
        if(mAliListPlayer != null){
            mAliListPlayer.setLoop(isLoop);
        }
    }

    private Boolean isLoop(){
        return mAliListPlayer != null && mAliListPlayer.isLoop();
    }

    private void setAutoPlay(Boolean isAutoPlay){
        if(mAliListPlayer != null){
            mAliListPlayer.setAutoPlay(isAutoPlay);
        }
    }

    private Boolean isAutoPlay(){
        if (mAliListPlayer != null) {
            mAliListPlayer.isAutoPlay();
        }
        return false;
    }

    private void setMuted(Boolean muted){
        if(mAliListPlayer != null){
            mAliListPlayer.setMute(muted);
        }
    }

    private Boolean isMuted(){
        if (mAliListPlayer != null) {
            mAliListPlayer.isMute();
        }
        return false;
    }

    private void setEnableHardWareDecoder(Boolean mEnableHardwareDecoder){
        if(mAliListPlayer != null){
            mAliListPlayer.enableHardwareDecoder(mEnableHardwareDecoder);
        }
    }

    private void setScaleMode(int model){
        if(mAliListPlayer != null){
            IPlayer.ScaleMode mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
            if(model == IPlayer.ScaleMode.SCALE_ASPECT_FIT.getValue()){
                mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
            }else if(model == IPlayer.ScaleMode.SCALE_ASPECT_FILL.getValue()){
                mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL;
            }else if(model == IPlayer.ScaleMode.SCALE_TO_FILL.getValue()){
                mScaleMode = IPlayer.ScaleMode.SCALE_TO_FILL;
            }
            mAliListPlayer.setScaleMode(mScaleMode);
        }
    }

    private int getScaleMode(){
        int scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT.getValue();
        if (mAliListPlayer != null) {
            scaleMode =  mAliListPlayer.getScaleMode().getValue();
        }
        return scaleMode;
    }

    private void setMirrorMode(int mirrorMode){
        if(mAliListPlayer != null){
            IPlayer.MirrorMode mMirrorMode;
            if(mirrorMode == IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL.getValue()){
                mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL;
            }else if(mirrorMode == IPlayer.MirrorMode.MIRROR_MODE_VERTICAL.getValue()){
                mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_VERTICAL;
            }else{
                mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE;
            }
            mAliListPlayer.setMirrorMode(mMirrorMode);
        }
    }

    private int getMirrorMode(){
        int mirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE.getValue();
        if (mAliListPlayer != null) {
            mirrorMode = mAliListPlayer.getMirrorMode().getValue();
        }
        return mirrorMode;
    }

    private void setRotateMode(int rotateMode){
        if(mAliListPlayer != null){
            IPlayer.RotateMode mRotateMode;
            if(rotateMode == IPlayer.RotateMode.ROTATE_90.getValue()){
                mRotateMode = IPlayer.RotateMode.ROTATE_90;
            }else if(rotateMode == IPlayer.RotateMode.ROTATE_180.getValue()){
                mRotateMode = IPlayer.RotateMode.ROTATE_180;
            }else if(rotateMode == IPlayer.RotateMode.ROTATE_270.getValue()){
                mRotateMode = IPlayer.RotateMode.ROTATE_270;
            }else{
                mRotateMode = IPlayer.RotateMode.ROTATE_0;
            }
            mAliListPlayer.setRotateMode(mRotateMode);
        }
    }

    private int getRotateMode(){
        int rotateMode = IPlayer.RotateMode.ROTATE_0.getValue();
        if(mAliListPlayer != null){
            rotateMode =  mAliListPlayer.getRotateMode().getValue();
        }
        return rotateMode;
    }

    private void setSpeed(double speed){
        if(mAliListPlayer != null){
            mAliListPlayer.setSpeed((float) speed);
        }
    }

    private double getSpeed(){
        double speed = 0;
        if(mAliListPlayer != null){
            speed = mAliListPlayer.getSpeed();
        }
        return speed;
    }

    private void setVideoBackgroundColor(int color){
        if(mAliListPlayer != null){
            mAliListPlayer.setVideoBackgroundColor(color);
        }
    }

    private void setVolume(double volume){
        if(mAliListPlayer != null){
            mAliListPlayer.setVolume((float)volume);
        }
    }

    private double getVolume(){
        double volume = 1.0;
        if(mAliListPlayer != null){
            volume = mAliListPlayer.getVolume();
        }
        return volume;
    }

    private void setConfig(PlayerConfig playerConfig){
        if(mAliListPlayer != null){
            mAliListPlayer.setConfig(playerConfig);
        }
    }

    private PlayerConfig getConfig(){
        if(mAliListPlayer != null){
            return mAliListPlayer.getConfig();
        }
        return null;
    }

    private CacheConfig getCacheConfig(){
        return new CacheConfig();
    }

    private void setCacheConfig(CacheConfig cacheConfig){
        if(mAliListPlayer != null){
            mAliListPlayer.setCacheConfig(cacheConfig);
        }
    }

    private TrackInfo getCurrentTrack(int currentTrackIndex){
        if(mAliListPlayer != null){
            return mAliListPlayer.currentTrack(currentTrackIndex);
        }else{
            return null;
        }
    }

    private void selectTrack(int trackId,boolean accurate){
        if(mAliListPlayer != null){
            mAliListPlayer.selectTrack(trackId,accurate);
        }
    }

    private void addExtSubtitle(String url){
        if(mAliListPlayer != null){
            mAliListPlayer.addExtSubtitle(url);
        }
    }

    private void selectExtSubtitle(int trackIndex,boolean enable){
        if(mAliListPlayer != null){
            mAliListPlayer.selectExtSubtitle(trackIndex,enable);
        }
    }

    private void enableConsoleLog(Boolean enableLog){
        Logger.getInstance(mContext).enableConsoleLog(enableLog);
    }

    private void setLogLevel(int level){
        Logger.LogLevel mLogLevel;
        if(level == Logger.LogLevel.AF_LOG_LEVEL_NONE.getValue()){
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_NONE;
        }else if(level == Logger.LogLevel.AF_LOG_LEVEL_FATAL.getValue()){
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_FATAL;
        }else if(level == Logger.LogLevel.AF_LOG_LEVEL_ERROR.getValue()){
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_ERROR;
        }else if(level == Logger.LogLevel.AF_LOG_LEVEL_WARNING.getValue()){
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_WARNING;
        }else if(level == Logger.LogLevel.AF_LOG_LEVEL_INFO.getValue()){
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_INFO;
        }else if(level == Logger.LogLevel.AF_LOG_LEVEL_DEBUG.getValue()){
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_DEBUG;
        }else if(level == Logger.LogLevel.AF_LOG_LEVEL_TRACE.getValue()){
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_TRACE;
        }else{
            mLogLevel = Logger.LogLevel.AF_LOG_LEVEL_NONE;
        }
        Logger.getInstance(mContext).setLogLevel(mLogLevel);
    }

    private Integer getLogLevel(){
        return Logger.getInstance(mContext).getLogLevel().getValue();
    }

    private String createDeviceInfo(){
        AliPlayerFactory.DeviceInfo deviceInfo = new AliPlayerFactory.DeviceInfo();
        deviceInfo.model = Build.MODEL;
        return deviceInfo.model;
    }

    private void addBlackDevice(String blackType,String modelInfo){
        AliPlayerFactory.DeviceInfo deviceInfo = new AliPlayerFactory.DeviceInfo();
        deviceInfo.model = modelInfo;
        AliPlayerFactory.BlackType aliPlayerBlackType;
        if(!TextUtils.isEmpty(blackType) && blackType.equals("HW_Decode_H264")){
            aliPlayerBlackType = AliPlayerFactory.BlackType.HW_Decode_H264;
        }else{
            aliPlayerBlackType = AliPlayerFactory.BlackType.HW_Decode_HEVC;
        }
        AliPlayerFactory.addBlackDevice(aliPlayerBlackType,deviceInfo);
    }

    private void createThumbnailHelper(String url){
        mThumbnailHelper = new ThumbnailHelper(url);
        mThumbnailHelper.setOnPrepareListener(new ThumbnailHelper.OnPrepareListener() {
            @Override
            public void onPrepareSuccess() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","thumbnail_onPrepared_Success");
                mEventSink.success(map);
            }

            @Override
            public void onPrepareFail() {
                Map<String,Object> map = new HashMap<>();
                map.put("method","thumbnail_onPrepared_Fail");
                mEventSink.success(map);
            }
        });

        mThumbnailHelper.setOnThumbnailGetListener(new ThumbnailHelper.OnThumbnailGetListener() {
            @Override
            public void onThumbnailGetSuccess(long l, ThumbnailBitmapInfo thumbnailBitmapInfo) {
                if(thumbnailBitmapInfo != null && thumbnailBitmapInfo.getThumbnailBitmap() != null){
                    Map<String,Object> map = new HashMap<>();

                    Bitmap thumbnailBitmap = thumbnailBitmapInfo.getThumbnailBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    thumbnailBitmap.recycle();
                    long[] positionRange = thumbnailBitmapInfo.getPositionRange();

                    map.put("method","onThumbnailGetSuccess");
                    map.put("thumbnailbitmap",stream.toByteArray());
                    map.put("thumbnailRange",positionRange);
                    mEventSink.success(map);
                }
            }

            @Override
            public void onThumbnailGetFail(long l, String s) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onThumbnailGetFail");
                mEventSink.success(map);
            }
        });
        mThumbnailHelper.prepare();
    }

    private void requestBitmapAtPosition(int position){
        if(mThumbnailHelper != null){
            mThumbnailHelper.requestBitmapAtPosition(position);
        }
    }


    /** ========================================================= */

    private void addVidSource(String vid,String uid){
        if(mAliListPlayer != null){
            mAliListPlayer.addVid(vid,uid);
        }
    }
    private void addUrlSource(String url,String uid){
        if(mAliListPlayer != null){
            mAliListPlayer.addUrl(url,uid);
        }
    }

    private void removeSource(String uid){
        if(mAliListPlayer != null){
            mAliListPlayer.removeSource(uid);
        }
    }

    private void clear(){
        if(mAliListPlayer != null){
            mAliListPlayer.clear();
        }
    }

    private void moveToNext(StsInfo stsInfo) {
        if(mAliListPlayer != null){
            mAliListPlayer.moveToNext(stsInfo);
        }
    }

    private void moveToPre(StsInfo stsInfo){
        if(mAliListPlayer != null){
            mAliListPlayer.moveToPrev(stsInfo);
        }
    }

    private void moveTo(String uid,StsInfo stsInfo){
        if(mAliListPlayer != null){
            mAliListPlayer.moveTo(uid,stsInfo);
        }
    }

    private void moveTo(String uid){
        if(mAliListPlayer != null){
            mAliListPlayer.moveTo(uid);
        }
    }
}
