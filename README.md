# QigsawBundle [![](https://jitpack.io/v/com.gitee.DonaldDu/QigsawBundle.svg)](https://jitpack.io/#com.gitee.DonaldDu/QigsawBundle)

```
    implementation 'com.gitee.DonaldDu.QigsawBundle:Plugin:xxx'

    apply plugin: 'qigsaw-bundle'
    qigsawBundleOption {
        log = true
        apkFileHost = 'http://www.qigsaw.com/'
        copyToDirectory = file('../splits').absolutePath

        options = [
                '--ks=' + file("../keystore/debug.jks"),
//              "--connected-device",//fixme 注释这行，如果没有连接手机或模拟器
                "--ks-pass=pass:qigsawtest",
                '--ks-key-alias=qigsawtest',
                "--key-pass=pass:qigsawtest"
        ]
    }
```

#### 介绍

