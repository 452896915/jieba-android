# jieba-android
**结巴分词的Android版本**

感谢jieba分词原作者[fxsjy](https://github.com/fxsjy)，感谢jieba分词的java版本作者[huaban](https://github.com/huaban)，本代码的移植基于java版[jieba-analysis](https://github.com/huaban/jieba-analysis)，在其基础上加入了Android Asset下字典文件的生成和读取，并重点优化了在手机上的启动速度。直接读取原始字典文件进行初始化在测试手机上需要28秒完成，通过将加载字典文件生成的字典树存储成特殊格式的中间文件，本工程代码将初始化时间降到1.5秒，分词速度在1秒以内。实现详情参考文章：https://www.jianshu.com/p/fda5cf1d3e6a
