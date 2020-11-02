# IM.Chat.Android.SDK

# PREREQUISITES

* JDK 1.8

# Download

Use Gradle:
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://dl.bintray.com/ruilisi/IM.Chat.Android.SDK' }
        maven { url 'https://jitpack.io' }
    }
}

android {
     compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    dataBinding {
        enabled = true
    }
}

dependencies {
    implementation 'com.chat.android.im:im:0.0.1'
}
```
# ProGuard

Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguard-rules.pro
```
-keep class com.chat.android.im.bean.** {*; }
-dontwarn com.chat.android.im.bean.**

-keep class com.chat.android.im.message.** {*; }
-dontwarn com.chat.android.im.message.**
```
# How do I use?

Simple use cases will look something like this:
```
    IM im = IM.getInstance(getApplication());
    im.setDataConfig(getDataConfig());
    im.openChat(new IMCallback() {
        @Override
        public void onFailure(String error) {
        }

        @Override
        public void onSuccess() {
        }
    });
    
    private UnifyDataConfig getDataConfig() {
        return new UnifyDataConfig.Builder()
                .setUrl("wss://xxx/websocket")
                .setRoomId("xxx")
                .setUserId("xxx")
                .setUserToken("xxx")
                .build();
    }
```
# License

BSD, part MIT and Apache 2.0

