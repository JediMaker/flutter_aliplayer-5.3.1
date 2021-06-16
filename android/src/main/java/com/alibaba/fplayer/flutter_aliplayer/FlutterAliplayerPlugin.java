package com.alibaba.fplayer.flutter_aliplayer;

import androidx.annotation.NonNull;

import com.aliyun.player.AliPlayerFactory;
import com.aliyun.private_service.PrivateService;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterAliplayerPlugin
 */
public class FlutterAliplayerPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private FlutterAliDownloader mAliyunDownload;
    private FlutterPluginBinding flutterPluginBinding;
    private FlutterAliListPlayer mFlutterAliListPlayer;
    private FlutterAliPlayerView mFlutterAliPlayerView;
    private FlutterAliPlayer mFlutterAliPlayer;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding;
        mFlutterAliPlayerView = new FlutterAliPlayerView(flutterPluginBinding);
        flutterPluginBinding.getPlatformViewRegistry().registerViewFactory("flutter_aliplayer_render_view", mFlutterAliPlayerView);
        mAliyunDownload = new FlutterAliDownloader(flutterPluginBinding.getApplicationContext(), flutterPluginBinding);
        MethodChannel mAliPlayerFactoryMethodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "plugins.flutter_aliplayer_factory");
        mAliPlayerFactoryMethodChannel.setMethodCallHandler(this);
    }

    //   This static function is optional and equivalent to onAttachedToEngine. It supports the old
//   pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
//   plugin registration via this function while apps migrate to use the new Android APIs
//   post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
//
//   It is encouraged to share logic between onAttachedToEngine and registerWith to keep
//   them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
//   depending on the user's project. onAttachedToEngine or registerWith must both be defined
//   in the same class.
    public static void registerWith(Registrar registrar) {
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "createAliPlayer":
                mFlutterAliPlayer = new FlutterAliPlayer(flutterPluginBinding);
                mFlutterAliPlayerView.setPlayer(mFlutterAliPlayer.getAliPlayer());
                result.success(null);
                break;
            case "createAliListPlayer":
                mFlutterAliListPlayer = new FlutterAliListPlayer(flutterPluginBinding);
                mFlutterAliPlayerView.setPlayer(mFlutterAliListPlayer.getAliListPlayer());
                result.success(null);
                break;
            case "initService":
                byte[] datas = (byte[]) call.arguments;
                PrivateService.initService(flutterPluginBinding.getApplicationContext(),datas);
                break;
        }

    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    }
}
