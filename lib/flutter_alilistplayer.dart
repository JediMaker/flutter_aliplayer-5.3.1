import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'flutter_aliplayer.dart';
export 'flutter_aliplayer.dart';

class FlutterAliListPlayer extends FlutterAliplayer {
  FlutterAliListPlayer.init(int id) : super.init(id) {
    channel = new MethodChannel('flutter_alilistplayer');
  }

  Future<void> setPreloadCount(int count) async {
    return channel.invokeMethod("setPreloadCount", count);
  }

  Future<void> addVidSource({@required vid, @required uid}) async {
    Map<String, dynamic> info = {'vid': vid, 'uid': uid};
    return channel.invokeMethod("addVidSource", info);
  }

  Future<void> addUrlSource({@required url, @required uid}) async {
    Map<String, dynamic> info = {'url': url, 'uid': uid};
    return channel.invokeMethod("addUrlSource", info);
  }

  Future<void> removeSource(String uid) async {
    return channel.invokeMethod("removeSource", uid);
  }

  Future<void> clear() async {
    return channel.invokeMethod("clear");
  }

  Future<void> moveToNext(
      {@required accId,
      @required accKey,
      @required token,
      @required region}) async {
    Map<String, dynamic> info = {
      'accId': accId,
      'accKey': accKey,
      'token': token,
      'region': region
    };
    return channel.invokeMethod("moveToNext", info);
  }

  Future<void> moveToPre(
      {@required accId,
      @required accKey,
      @required token,
      @required region}) async {
    Map<String, dynamic> info = {
      'accId': accId,
      'accKey': accKey,
      'token': token,
      'region': region
    };
    return channel.invokeMethod("moveToPre", info);
  }

  ///移动到指定位置开始准备播放,url播放方式只需要填写uid；sts播放方式，需要更新sts信息
  ///uid 指定资源的uid，代表在列表中的唯一标识
  Future<void> moveTo(
      {@required String uid,
      String accId,
      String accKey,
      String token,
      String region}) async {
    Map<String, dynamic> info = {
      'uid': uid,
      'accId': accId,
      'accKey': accKey,
      'token': token,
      'region': region
    };
    return channel.invokeMethod("moveTo", info);
  }
}
