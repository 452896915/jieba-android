# jieba-android
**结巴分词的Android版本**

感谢jieba分词原作者[fxsjy](https://github.com/fxsjy)，感谢jieba分词的java版本作者[huaban](https://github.com/huaban)，本代码的移植基于java版[jieba-analysis](https://github.com/huaban/jieba-analysis)，在其基础上加入了Android Asset下字典文件的生成和读取，并重点优化了在手机上的启动速度。直接读取原始字典文件进行初始化在测试手机上需要28秒完成，通过将加载字典文件生成的字典树存储成特殊格式的中间文件，本工程代码将初始化时间降到1.5秒，分词速度在1秒以内。实现详情参考文章：https://www.jianshu.com/p/fda5cf1d3e6a

**Demo App效果展示**

单纯分词效果：

![效果展示1](https://upload-images.jianshu.io/upload_images/2839011-b0c07fe3255cacf1.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/400)

分词结合搜索效果：

![效果展示2](https://upload-images.jianshu.io/upload_images/2839011-69032a2f47d64172.gif?imageMogr2/auto-orient/strip)

**接入方式**

在gradle中加入：
```
compile 'jackmego.com.jieba_android:jieba_android-release:0.1.0'
```

使用的时候首先进行初始化，一般在MyApplication里进行：

```
// 异步初始化
JiebaSegmenter.init(getApplicationContext());
```

该初始化是异步进行的，速度仅需1.5秒即可完成包含35万词典的字典树的生成。
该Android分词器提供了三个接口用于分词，下面两个简单接口分别是同步和异步分词接口：
```
// 异步接口
public void getDividedStringAsync(final String query, final RequestCallback<ArrayList<String>> callback) {...}

// 同步接口
public ArrayList<String> getDividedString(String query) {...}
```

同时保留了结巴分词原有的分词接口process，可以指定分词模式是索引模式（INDEX）或搜索引擎模式（SEARCH），两者的差别在于搜索引擎模式分词更精细，索引模式相对更粗粒度。
```
public static enum SegMode {
        INDEX,
        SEARCH
    }

public List<SegToken> process(String query, SegMode mode) {...}
```

**主要参数**

加载35万词典1.5秒，分词1秒以内。
