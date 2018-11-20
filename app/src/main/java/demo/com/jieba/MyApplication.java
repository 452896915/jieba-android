package demo.com.jieba;

import android.app.Application;

import jackmego.com.jieba_android.JiebaSegmenter;

/**
 * Created by JackMeGo on 2017/7/4.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 异步初始化
        JiebaSegmenter.init(getApplicationContext());
    }
}
