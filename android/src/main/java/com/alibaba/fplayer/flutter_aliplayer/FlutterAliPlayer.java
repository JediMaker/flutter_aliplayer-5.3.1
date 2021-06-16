package com.alibaba.fplayer.flutter_aliplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import com.aliyun.player.AliPlayer;
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
import com.aliyun.player.source.Definition;
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
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;

public class FlutterAliPlayer implements EventChannel.StreamHandler, MethodCallHandler {

    private FlutterPlugin.FlutterPluginBinding mFlutterPluginBinding;

    private final Gson mGson;
    private Context mContext;
    private EventChannel.EventSink mEventSink;
    private EventChannel mEventChannel;
    private AliPlayer mAliPlayer;
    private MethodChannel mAliPlayerMethodChannel;
    private String mSnapShotPath;
    private ThumbnailHelper mThumbnailHelper;

    public FlutterAliPlayer(FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        this.mFlutterPluginBinding = flutterPluginBinding;
        this.mContext = flutterPluginBinding.getApplicationContext();
        mGson = new Gson();
        mAliPlayer = AliPlayerFactory.createAliPlayer(mFlutterPluginBinding.getApplicationContext());
        mAliPlayerMethodChannel = new MethodChannel(mFlutterPluginBinding.getFlutterEngine().getDartExecutor(),"flutter_aliplayer");
        mAliPlayerMethodChannel.setMethodCallHandler(this);
        mEventChannel = new EventChannel(mFlutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_aliplayer_event");
        mEventChannel.setStreamHandler(this);
        initListener(mAliPlayer);
    }

    public AliPlayer getAliPlayer(){
        return mAliPlayer;
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
                map.put("method","onTrackChanged");
                Map<String,Object> infoMap = new HashMap<>();
                infoMap.put("vodFormat",trackInfo.getVodFormat());
                infoMap.put("videoHeight",trackInfo.getVideoHeight());
                infoMap.put("videoWidth",trackInfo.getVideoHeight());
                infoMap.put("subtitleLanguage",trackInfo.getSubtitleLang());
                infoMap.put("trackBitrate",trackInfo.getVideoBitrate());
                infoMap.put("vodFileSize",trackInfo.getVodFileSize());
                infoMap.put("trackIndex",trackInfo.getIndex());
                infoMap.put("trackDefinition",trackInfo.getVodDefinition());
                infoMap.put("audioSampleFormat",trackInfo.getAudioSampleFormat());
                infoMap.put("audioLanguage",trackInfo.getAudioLang());
                infoMap.put("vodPlayUrl",trackInfo.getVodPlayUrl());
                infoMap.put("trackType",trackInfo.getType().ordinal());
                infoMap.put("audioSamplerate",trackInfo.getAudioSampleRate());
                infoMap.put("audioChannels",trackInfo.getAudioChannels());
                map.put("info",infoMap);
                mEventSink.success(map);
            }

            @Override
            public void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
                Map<String,Object> map = new HashMap<>();
                map.put("method","onChangedFail");
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
            case "createAliPlayer":
                createAliPlayer();
                break;
            case "setUrl":
                String url = methodCall.arguments.toString();
                setDataSource(url);
                break;
            case "setVidSts":
                Map<String,Object> stsMap = (Map<String,Object>)methodCall.arguments;
                VidSts vidSts = new VidSts();
                vidSts.setRegion((String) stsMap.get("region"));
                vidSts.setVid((String) stsMap.get("vid"));
                vidSts.setAccessKeyId((String) stsMap.get("accessKeyId"));
                vidSts.setAccessKeySecret((String) stsMap.get("accessKeySecret"));
                vidSts.setSecurityToken((String) stsMap.get("securityToken"));

                List<String> stsMaplist = (List<String>) stsMap.get("definitionList");
                if(stsMaplist != null){
                    List<Definition> definitionList = new ArrayList<>();
                    for (String item : stsMaplist) {
                        if(Definition.DEFINITION_AUTO.getName().equals(item)){
                            definitionList.add(Definition.DEFINITION_AUTO);
                        }else{
                            if(Definition.DEFINITION_FD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_FD);
                            }else if(Definition.DEFINITION_LD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_LD);
                            }else if(Definition.DEFINITION_SD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_SD);
                            }else if(Definition.DEFINITION_HD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_HD);
                            }else if(Definition.DEFINITION_OD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_OD);
                            }else if(Definition.DEFINITION_2K.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_2K);
                            }else if(Definition.DEFINITION_4K.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_4K);
                            }else if(Definition.DEFINITION_SQ.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_SQ);
                            }else if(Definition.DEFINITION_HQ.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_HQ);
                            }
                        }
                    }
                    vidSts.setDefinition(definitionList);
                }

                if(stsMap.containsKey("previewTime") && !TextUtils.isEmpty((CharSequence) stsMap.get("previewTime"))){
                    VidPlayerConfigGen vidPlayerConfigGen = new VidPlayerConfigGen();
                    int previewTime = Integer.valueOf((String)stsMap.get("previewTime"));
                    vidPlayerConfigGen.setPreviewTime(previewTime);
                    vidSts.setPlayConfig(vidPlayerConfigGen);
                }
                setDataSource(vidSts);
                break;
            case "setVidAuth":
                Map<String,Object> authMap = (Map<String,Object>)methodCall.arguments;
                VidAuth vidAuth = new VidAuth();
                vidAuth.setVid((String) authMap.get("vid"));
                vidAuth.setRegion((String) authMap.get("region"));
                vidAuth.setPlayAuth((String) authMap.get("playAuth"));

                List<String> authMaplist = (List<String>) authMap.get("definitionList");
                if(authMaplist != null){
                    List<Definition> definitionList = new ArrayList<>();
                    for (String item : authMaplist) {
                        if(Definition.DEFINITION_AUTO.getName().equals(item)){
                            definitionList.add(Definition.DEFINITION_AUTO);
                        }else{
                            if(Definition.DEFINITION_FD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_FD);
                            }else if(Definition.DEFINITION_LD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_LD);
                            }else if(Definition.DEFINITION_SD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_SD);
                            }else if(Definition.DEFINITION_HD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_HD);
                            }else if(Definition.DEFINITION_OD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_OD);
                            }else if(Definition.DEFINITION_2K.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_2K);
                            }else if(Definition.DEFINITION_4K.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_4K);
                            }else if(Definition.DEFINITION_SQ.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_SQ);
                            }else if(Definition.DEFINITION_HQ.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_HQ);
                            }
                        }
                    }
                    vidAuth.setDefinition(definitionList);
                }

                if(authMap.containsKey("previewTime") && !TextUtils.isEmpty((String) authMap.get("previewTime"))){
                    VidPlayerConfigGen vidPlayerConfigGen = new VidPlayerConfigGen();
                    int previewTime = Integer.valueOf((String) authMap.get("previewTime"));
                    vidPlayerConfigGen.setPreviewTime(previewTime);
                    vidAuth.setPlayConfig(vidPlayerConfigGen);
                }
                setDataSource(vidAuth);
                break;
            case "setVidMps":
                Map<String,Object> mpsMap = (Map<String,Object>)methodCall.arguments;
                VidMps vidMps = new VidMps();
                vidMps.setMediaId((String) mpsMap.get("vid"));
                vidMps.setRegion((String) mpsMap.get("region"));
                vidMps.setAccessKeyId((String) mpsMap.get("accessKeyId"));
                vidMps.setAccessKeySecret((String) mpsMap.get("accessKeySecret"));

                List<String> mpsMaplist = (List<String>) mpsMap.get("definitionList");
                if(mpsMaplist != null){
                    List<Definition> definitionList = new ArrayList<>();
                    for (String item : mpsMaplist) {
                        if(Definition.DEFINITION_AUTO.getName().equals(item)){
                            definitionList.add(Definition.DEFINITION_AUTO);
                        }else{
                            if(Definition.DEFINITION_FD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_FD);
                            }else if(Definition.DEFINITION_LD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_LD);
                            }else if(Definition.DEFINITION_SD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_SD);
                            }else if(Definition.DEFINITION_HD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_HD);
                            }else if(Definition.DEFINITION_OD.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_OD);
                            }else if(Definition.DEFINITION_2K.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_2K);
                            }else if(Definition.DEFINITION_4K.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_4K);
                            }else if(Definition.DEFINITION_SQ.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_SQ);
                            }else if(Definition.DEFINITION_HQ.getName().equals(item)){
                                definitionList.add(Definition.DEFINITION_HQ);
                            }
                        }
                    }
                    vidMps.setDefinition(definitionList);
                }

                if(mpsMap.containsKey("playDomain") && !TextUtils.isEmpty((String) mpsMap.get("playDomain"))){
                    vidMps.setPlayDomain((String) mpsMap.get("playDomain"));
                }
                vidMps.setAuthInfo((String) mpsMap.get("authInfo"));
                vidMps.setHlsUriToken((String) mpsMap.get("hlsUriToken"));
                vidMps.setSecurityToken((String) mpsMap.get("securityToken"));
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
                setVideoBackgroundColor((Long) methodCall.arguments);
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
                result.success(null);
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
            case "getSDKVersion":
                result.success(AliPlayerFactory.getSdkVersion());
                break;
            case "setPreferPlayerName":
                String playerName = methodCall.arguments();
                setPlayerName(playerName);
                break;
            case "getPlayerName":
                result.success(getPlayerName());
                break;
            case "setStreamDelayTime":
                Map<String,Object> streamDelayTimeMap = (Map<String, Object>) methodCall.arguments;
                Integer index = (Integer) streamDelayTimeMap.get("index");
                Integer time = (Integer) streamDelayTimeMap.get("time");
                setStreamDelayTime(index,time);
                break;
            default:
                result.notImplemented();
        }
    }

    private void createAliPlayer(){
        mAliPlayer = AliPlayerFactory.createAliPlayer(mContext);
        initListener(mAliPlayer);
    }

    private void setDataSource(String url){
        if(mAliPlayer != null){
            UrlSource urlSource = new UrlSource();
            urlSource.setUri(url);
            ((AliPlayer)mAliPlayer).setDataSource(urlSource);
        }
    }

    private void setDataSource(VidSts vidSts){
        if(mAliPlayer != null){
            ((AliPlayer)mAliPlayer).setDataSource(vidSts);
        }
    }

    private void setDataSource(VidAuth vidAuth){
        if(mAliPlayer != null){
            ((AliPlayer)mAliPlayer).setDataSource(vidAuth);
        }
    }

    private void setDataSource(VidMps vidMps){
        if(mAliPlayer != null){
            ((AliPlayer)mAliPlayer).setDataSource(vidMps);
        }
    }

    private void prepare(){
        if(mAliPlayer != null){
            mAliPlayer.prepare();
        }
    }

    private void start(){
        if(mAliPlayer != null){
            mAliPlayer.start();
        }
    }

    private void pause(){
        if(mAliPlayer != null){
            mAliPlayer.pause();
        }
    }

    private void stop(){
        if(mAliPlayer != null){
            mAliPlayer.stop();
        }
    }

    private void release(){
        if(mAliPlayer != null){
            mAliPlayer.release();
            mAliPlayer = null;
        }
    }

    private void seekTo(long position,int seekMode){
        if(mAliPlayer != null){
            IPlayer.SeekMode mSeekMode;
            if(seekMode == IPlayer.SeekMode.Accurate.getValue()){
                mSeekMode = IPlayer.SeekMode.Accurate;
            }else{
                mSeekMode = IPlayer.SeekMode.Inaccurate;
            }
            mAliPlayer.seekTo(position,mSeekMode);
        }
    }

    private MediaInfo getMediaInfo(){
        if(mAliPlayer != null){
            return mAliPlayer.getMediaInfo();
        }
        return null;
    }

    private void snapshot(){
        if(mAliPlayer != null){
            mAliPlayer.snapshot();
        }
    }

    private void setLoop(Boolean isLoop){
        if(mAliPlayer != null){
            mAliPlayer.setLoop(isLoop);
        }
    }

    private Boolean isLoop(){
        return mAliPlayer != null && mAliPlayer.isLoop();
    }

    private void setAutoPlay(Boolean isAutoPlay){
        if(mAliPlayer != null){
            mAliPlayer.setAutoPlay(isAutoPlay);
        }
    }

    private Boolean isAutoPlay(){
        if (mAliPlayer != null) {
            return mAliPlayer.isAutoPlay();
        }
        return false;
    }

    private void setMuted(Boolean muted){
        if(mAliPlayer != null){
            mAliPlayer.setMute(muted);
        }
    }

    private Boolean isMuted(){
        if (mAliPlayer != null) {
            return mAliPlayer.isMute();
        }
        return false;
    }

    private void setEnableHardWareDecoder(Boolean mEnableHardwareDecoder){
        if(mAliPlayer != null){
            mAliPlayer.enableHardwareDecoder(mEnableHardwareDecoder);
        }
    }

    private void setScaleMode(int model){
        if(mAliPlayer != null){
            IPlayer.ScaleMode mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
            if(model == IPlayer.ScaleMode.SCALE_ASPECT_FIT.getValue()){
                mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
            }else if(model == IPlayer.ScaleMode.SCALE_ASPECT_FILL.getValue()){
                mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL;
            }else if(model == IPlayer.ScaleMode.SCALE_TO_FILL.getValue()){
                mScaleMode = IPlayer.ScaleMode.SCALE_TO_FILL;
            }
            mAliPlayer.setScaleMode(mScaleMode);
        }
    }

    private int getScaleMode(){
        int scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT.getValue();
        if (mAliPlayer != null) {
            scaleMode =  mAliPlayer.getScaleMode().getValue();
        }
        return scaleMode;
    }

    private void setMirrorMode(int mirrorMode){
        if(mAliPlayer != null){
            IPlayer.MirrorMode mMirrorMode;
            if(mirrorMode == IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL.getValue()){
                mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL;
            }else if(mirrorMode == IPlayer.MirrorMode.MIRROR_MODE_VERTICAL.getValue()){
                mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_VERTICAL;
            }else{
                mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE;
            }
            mAliPlayer.setMirrorMode(mMirrorMode);
        }
    }

    private int getMirrorMode(){
        int mirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE.getValue();
        if (mAliPlayer != null) {
            mirrorMode = mAliPlayer.getMirrorMode().getValue();
        }
        return mirrorMode;
    }

    private void setRotateMode(int rotateMode){
        if(mAliPlayer != null){
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
            mAliPlayer.setRotateMode(mRotateMode);
        }
    }

    private int getRotateMode(){
        int rotateMode = IPlayer.RotateMode.ROTATE_0.getValue();
        if(mAliPlayer != null){
            rotateMode =  mAliPlayer.getRotateMode().getValue();
        }
        return rotateMode;
    }

    private void setSpeed(double speed){
        if(mAliPlayer != null){
            mAliPlayer.setSpeed((float) speed);
        }
    }

    private double getSpeed(){
        double speed = 0;
        if(mAliPlayer != null){
            speed = mAliPlayer.getSpeed();
        }
        return speed;
    }

    private void setVideoBackgroundColor(long color){
        if(mAliPlayer != null){
            mAliPlayer.setVideoBackgroundColor((int) color);
        }
    }

    private void setVolume(double volume){
        if(mAliPlayer != null){
            mAliPlayer.setVolume((float)volume);
        }
    }

    private double getVolume(){
        double volume = 1.0;
        if(mAliPlayer != null){
            volume = mAliPlayer.getVolume();
        }
        return volume;
    }

    private void setConfig(PlayerConfig playerConfig){
        if(mAliPlayer != null){
            mAliPlayer.setConfig(playerConfig);
        }
    }

    private PlayerConfig getConfig(){
        if(mAliPlayer != null){
            return mAliPlayer.getConfig();
        }
        return null;
    }

    private CacheConfig getCacheConfig(){
        return new CacheConfig();
    }

    private void setCacheConfig(CacheConfig cacheConfig){
        if(mAliPlayer != null){
            mAliPlayer.setCacheConfig(cacheConfig);
        }
    }

    private TrackInfo getCurrentTrack(int currentTrackIndex){
        if(mAliPlayer != null){
            return mAliPlayer.currentTrack(currentTrackIndex);
        }else{
            return null;
        }
    }

    private void selectTrack(int trackId,boolean accurate){
        if(mAliPlayer != null){
            mAliPlayer.selectTrack(trackId,accurate);
        }
    }

    private void addExtSubtitle(String url){
        if(mAliPlayer != null){
            mAliPlayer.addExtSubtitle(url);
        }
    }

    private void selectExtSubtitle(int trackIndex,boolean enable){
        if(mAliPlayer != null){
            mAliPlayer.selectExtSubtitle(trackIndex,enable);
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

    private void setPlayerName(String playerName) {
        if(mAliPlayer != null){
            mAliPlayer.setPreferPlayerName(playerName);
        }
    }

    private String getPlayerName(){
        return mAliPlayer == null ? "" : mAliPlayer.getPlayerName();
    }

    private void setStreamDelayTime(int index,int time){
        if(mAliPlayer != null){
            mAliPlayer.setStreamDelayTime(index,time);
        }
    }
}
