import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_aliplayer/flutter_aliplayer.dart';
import 'package:flutter_aliplayer/flutter_aliplayer_factory.dart';
import 'package:flutter_aliplayer_example/config.dart';

class SettingPage extends StatefulWidget {
  FlutterAliplayer _flutterAliPlayre;

  @override
  _SettingHomePageState createState() => _SettingHomePageState();
}

class _SettingHomePageState extends State<SettingPage> {
  TextEditingController _dnsTextEditingController = TextEditingController();
  String _sdkVersion;
  FlutterAliPlayerFactory _flutterAliPlayerFactory;
  List<String> _playerName = List();
  String _currentPlayerName = "Default";

  @override
  void initState() {
    super.initState();
    if (GlobalSettings.mPlayerName.isNotEmpty) {
      _currentPlayerName = GlobalSettings.mPlayerName;
    }
    _playerName.add("Default");
    if (Platform.isAndroid) {
      _playerName.add("SuperMediaPlayer");
      _playerName.add("ExoPlayer");
      _playerName.add("MediaPlayer");
    }
    if (Platform.isIOS) {
      _playerName..add("SuperMediaPlayer")..add("AppleAVPlayer");
    }
    widget._flutterAliPlayre = FlutterAliplayer.init(0);
    if (Platform.isAndroid) {
      _flutterAliPlayerFactory = FlutterAliPlayerFactory();
      widget._flutterAliPlayre = _flutterAliPlayerFactory.createAliPlayer();
    }
    widget._flutterAliPlayre.getSDKVersion().then((value) {
      setState(() {
        _sdkVersion = value;
      });
    });

    widget._flutterAliPlayre.getLogLevel().then((value) {
      setState(() {
        GlobalSettings.mLogLevel = value;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomPadding: false,
      appBar: AppBar(
        title: Text("Settings"),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.only(
            left: 5.0, top: 10.0, right: 5.0, bottom: 10.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            //VersionCode
            Text("版本号:$_sdkVersion"),

            //硬解开关
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text("硬解开关"),
                SizedBox(
                  width: 5.0,
                ),
                Switch(
                    value: GlobalSettings.mEnableHardwareDecoder,
                    onChanged: (value) {
                      widget._flutterAliPlayre.setEnableHardwareDecoder(value);
                      GlobalSettings.mEnableHardwareDecoder = value;
                      setState(() {
                        GlobalSettings.mEnableHardwareDecoder = value;
                      });
                    }),
              ],
            ),

            //播放器切换
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [Text("播放器切换"), _buildChangePlayer()],
            ),

            SizedBox(
              height: 5.0,
            ),

            //黑名单,Android显示，iOS不显示
            Text(Platform.operatingSystemVersion),
            _blackListForAndroid(),

            SizedBox(
              height: 10.0,
            ),

            //DSResolve
            _buildDNSResolve(),

            //Log
            Row(
              children: [
                Text("Log日志开关"),
                Switch(
                    value: GlobalSettings.mEnableAliPlayerLog,
                    onChanged: (value) {
                      widget._flutterAliPlayre.enableConsoleLog(value);
                      GlobalSettings.mEnableAliPlayerLog = value;
                      setState(() {
                        GlobalSettings.mEnableAliPlayerLog = value;
                      });
                    })
              ],
            ),
            _buildLog(),
          ],
        ),
      ),
    );
  }

  //播放器切换
  Widget _buildChangePlayer() {
    return Column(
      children: _playerName.map((e) {
        return Container(
          height: 35.0,
          child: RadioListTile(
              dense: true,
              title: Text("$e"),
              value: e,
              groupValue: _currentPlayerName,
              onChanged: (value) {
                setState(() {
                  if (value == "Default") {
                    GlobalSettings.mPlayerName = "";
                  } else {
                    GlobalSettings.mPlayerName = value;
                  }
                  _currentPlayerName = value;
                });
              }),
        );
      }).toList(),
    );
  }

  //黑名单
  Widget _blackListForAndroid() {
    if (Platform.isAndroid) {
      return Row(
        children: [
          RaisedButton(
            child: Text("HEVC黑名单"),
            onPressed: () {
              widget._flutterAliPlayre.createDeviceInfo().then((value) {
                widget._flutterAliPlayre
                    .addBlackDevice(FlutterAvpdef.BLACK_DEVICES_HEVC, value);
              });
            },
          ),
          SizedBox(
            width: 10.0,
          ),
          RaisedButton(
            child: Text("H264黑名单"),
            onPressed: () {
              widget._flutterAliPlayre.createDeviceInfo().then((value) {
                widget._flutterAliPlayre
                    .addBlackDevice(FlutterAvpdef.BLACK_DEVICES_H264, value);
              });
            },
          ),
        ],
      );
    } else {
      return SizedBox();
    }
  }

  //DNS
  Widget _buildDNSResolve() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text("DNSResolve"),
        Text("输入格式:域名1:端口1,ip1;域名2:端口2,ip2;..."),
        SizedBox(
          height: 5.0,
        ),
        TextField(
          controller: _dnsTextEditingController,
          keyboardType: TextInputType.multiline,
          maxLines: 3,
          decoration: InputDecoration(
            border: OutlineInputBorder(),
          ),
        ),
        RaisedButton(
          child: Text("设置DNS"),
          onPressed: () {
            String dns = _dnsTextEditingController.text;
            print("dns = $dns");
          },
        ),
      ],
    );
  }

  //Log
  Widget _buildLog() {
    if (GlobalSettings.mEnableAliPlayerLog) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            height: 35.0,
            child: RadioListTile(
                dense: true,
                title: Text("AF_LOG_LEVEL_NONE"),
                value: FlutterAvpdef.AF_LOG_LEVEL_NONE,
                groupValue: GlobalSettings.mLogLevel,
                onChanged: (value) {
                  widget._flutterAliPlayre.setLogLevel(value);

                  setState(() {
                    GlobalSettings.mLogLevel = value;
                  });
                }),
          ),
          Container(
            height: 35.0,
            child: RadioListTile(
                dense: true,
                title: Text("AF_LOG_LEVEL_FATAL"),
                value: FlutterAvpdef.AF_LOG_LEVEL_FATAL,
                groupValue: GlobalSettings.mLogLevel,
                onChanged: (value) {
                  widget._flutterAliPlayre.setLogLevel(value);
                  setState(() {
                    GlobalSettings.mLogLevel = value;
                  });
                }),
          ),
          Container(
            height: 35.0,
            child: RadioListTile(
                dense: true,
                title: Text("AF_LOG_LEVEL_ERROR"),
                value: FlutterAvpdef.AF_LOG_LEVEL_ERROR,
                groupValue: GlobalSettings.mLogLevel,
                onChanged: (value) {
                  widget._flutterAliPlayre.setLogLevel(value);
                  setState(() {
                    GlobalSettings.mLogLevel = value;
                  });
                }),
          ),
          Container(
            height: 35.0,
            child: RadioListTile(
                dense: true,
                title: Text("AF_LOG_LEVEL_WARNING"),
                value: FlutterAvpdef.AF_LOG_LEVEL_WARNING,
                groupValue: GlobalSettings.mLogLevel,
                onChanged: (value) {
                  widget._flutterAliPlayre.setLogLevel(value);
                  setState(() {
                    GlobalSettings.mLogLevel = value;
                  });
                }),
          ),
          Container(
            height: 35.0,
            child: RadioListTile(
                dense: true,
                title: Text("AF_LOG_LEVEL_INFO"),
                value: FlutterAvpdef.AF_LOG_LEVEL_INFO,
                groupValue: GlobalSettings.mLogLevel,
                onChanged: (value) {
                  widget._flutterAliPlayre.setLogLevel(value);
                  setState(() {
                    GlobalSettings.mLogLevel = value;
                  });
                }),
          ),
          Container(
            height: 35.0,
            child: RadioListTile(
                dense: true,
                title: Text("AF_LOG_LEVEL_DEBUG"),
                value: FlutterAvpdef.AF_LOG_LEVEL_DEBUG,
                groupValue: GlobalSettings.mLogLevel,
                onChanged: (value) {
                  widget._flutterAliPlayre.setLogLevel(value);
                  setState(() {
                    GlobalSettings.mLogLevel = value;
                  });
                }),
          ),
          Container(
            height: 35.0,
            child: RadioListTile(
                dense: true,
                title: Text("AF_LOG_LEVEL_TRACE"),
                value: FlutterAvpdef.AF_LOG_LEVEL_TRACE,
                groupValue: GlobalSettings.mLogLevel,
                onChanged: (value) {
                  widget._flutterAliPlayre.setLogLevel(value);
                  setState(() {
                    GlobalSettings.mLogLevel = value;
                  });
                }),
          ),
        ],
      );
    } else {
      return Container();
    }
  }
}
