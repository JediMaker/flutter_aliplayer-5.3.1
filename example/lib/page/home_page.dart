import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_aliplayer/flutter_aliplayer_factory.dart';
import 'package:flutter_aliplayer_example/config.dart';
import 'package:flutter/services.dart';
import 'package:flutter_aliplayer_example/page/auth_page.dart';
import 'package:flutter_aliplayer_example/page/download_page.dart';
import 'package:flutter_aliplayer_example/page/setting_page.dart';
import 'package:flutter_aliplayer_example/page/sts_page.dart';
import 'package:flutter_aliplayer_example/page/url_page.dart';
import 'package:flutter_aliplayer_example/page/video_grid_page.dart';
import 'package:flutter_aliplayer_example/util/common_utils.dart';

import 'local_page.dart';

class HomePage extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<HomePage> {
  bool _showDownload = false;
  DownloadPage _downloadPage;

  List titleArr = [
    'URL播放',
    'STS播放',
    'AUTH播放',
    '播放列表演示(VID)',
    '播放列表演示(URL)',
    '断点下载',
    '本地文件'
  ];

  @override
  void initState() {
    super.initState();
    _loadEncrypted();
    _downloadPage = DownloadPage();
    SystemChrome.setPreferredOrientations(
        [DeviceOrientation.portraitUp, DeviceOrientation.portraitDown]);
  }

  _loadEncrypted() async {
    if (Platform.isAndroid) {
      var bytes = await rootBundle.load("assets/encryptedApp.dat");
      // getExternalStorageDirectories
      FlutterAliPlayerFactory flutterAliPlayerFactory =
          FlutterAliPlayerFactory();
      flutterAliPlayerFactory.initService(bytes.buffer.asUint8List());
    } else if (Platform.isIOS) {
      var bytes = await rootBundle.load("assets/encryptedApp_ios.dat");
      FlutterAliPlayerFactory flutterAliPlayerFactory =
          FlutterAliPlayerFactory();
      flutterAliPlayerFactory.initService(bytes.buffer.asUint8List());
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _showDownload
          ? AppBar(
              leading: IconButton(
                icon: Icon(Icons.arrow_back),
                onPressed: () {
                  setState(() {
                    _showDownload = false;
                  });
                },
              ),
              title: Text("Download"),
              centerTitle: true,
              actions: [
                IconButton(
                  icon: Icon(Icons.add),
                  onPressed: () {
                    _downloadPage.showDownloadDialog();
                  },
                ),
              ],
            )
          : AppBar(
              title: const Text('Plugin for aliplayer'),
              actions: [
                IconButton(
                  icon: Icon(Icons.settings),
                  onPressed: () => CommomUtils.pushPage(context, SettingPage()),
                ),
              ],
            ),
      body: Stack(
        children: [
          ListView.builder(
            padding: EdgeInsets.all(8.0),
            itemExtent: 50.0,
            itemCount: titleArr.length,
            itemBuilder: (BuildContext context, int index) {
              return FlatButton(
                child: Text(titleArr[index]),
                onPressed: () {
                  switch (index) {
                    case 0:
                      CommomUtils.pushPage(context, UrlPage());
                      break;
                    case 1:
                      CommomUtils.pushPage(context, StsPage());
                      break;
                    case 2:
                      CommomUtils.pushPage(context, AuthPage());
                      break;
                    case 3:
                      CommomUtils.pushPage(
                          context, VideoGridPage(playMode: ModeType.STS));
                      break;
                    case 4:
                      CommomUtils.pushPage(
                          context, VideoGridPage(playMode: ModeType.URL));
                      break;
                    case 5:
                      _showDownload = true;
                      setState(() {});
                      break;
                    case 6:
                      if (Platform.isAndroid) {
                        CommomUtils.pushPage(context, LocalPage());
                      }
                      break;
                    default:
                  }
                },
              );
            },
          ),
          Offstage(
            offstage: !_showDownload,
            child: _downloadPage,
          ),
        ],
      ),
    );
  }
}