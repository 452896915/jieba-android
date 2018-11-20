package jackmego.com.jieba_android;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 词典树分段，表示词典树的一个分枝
 */
public class Element implements Comparable<Element>, Serializable {
    private static final long serialVersionUID=10086L;

    // 数组大小上限
    private static final int ARRAY_LENGTH_LIMIT = 3;

    // Map存储结构
    public Map<Character, Element> childrenMap;
    // 数组方式存储结构
    private Element[] childrenArray;

    // 当前节点上存储的字符
    public Character nodeChar;
    // 当前节点存储的Segment数目
    // storeSize <=ARRAY_LENGTH_LIMIT ，使用数组存储， storeSize >ARRAY_LENGTH_LIMIT
    // ,则使用Map存储
    public int storeSize = 0;
    // 当前DictSegment状态 ,默认 0 , 1表示从根节点到当前节点的路径表示一个词
    public int nodeState = 0;


    Element(Character nodeChar) {
        if (nodeChar == null) {
            throw new IllegalArgumentException("参数为空异常，字符不能为空");
        }
        this.nodeChar = nodeChar;
    }


    Character getNodeChar() {
        return nodeChar;
    }


    /*
     * 判断是否有下一个节点
     */
    boolean hasNextNode() {
        return this.storeSize > 0;
    }

    public int getStoreSize() {
        return this.storeSize;
    }

    /**
     * 匹配词段
     *
     * @param charArray
     * @return Hit
     */
    Hit match(char[] charArray) {
        return this.match(charArray, 0, charArray.length, null);
    }


    /**
     * 匹配词段
     *
     * @param charArray
     * @param begin
     * @param length
     * @return Hit
     */
    Hit match(char[] charArray, int begin, int length) {
        return this.match(charArray, begin, length, null);
    }


    /**
     * 匹配词段
     *
     * @param charArray
     * @param begin
     * @param length
     * @param searchHit
     * @return Hit
     */
    Hit match(char[] charArray, int begin, int length, Hit searchHit) {

        if (searchHit == null) {
            // 如果hit为空，新建
            searchHit = new Hit();
            // 设置hit的其实文本位置
            searchHit.setBegin(begin);
        }
        else {
            // 否则要将HIT状态重置
            searchHit.setUnmatch();
        }
        // 设置hit的当前处理位置
        searchHit.setEnd(begin);

        Character keyChar = new Character(charArray[begin]);
        Element ds = null;

        // 引用实例变量为本地变量，避免查询时遇到更新的同步问题
        Element[] elementArray = this.childrenArray;
        Map<Character, Element> segmentMap = this.childrenMap;

        // STEP1 在节点中查找keyChar对应的DictSegment
        if (elementArray != null) {
            // 在数组中查找
            Element keyElement = new Element(keyChar);
            int position = Arrays.binarySearch(elementArray, 0, this.storeSize, keyElement);
            if (position >= 0) {
                ds = elementArray[position];
            }

        }
        else if (segmentMap != null) {
            // 在map中查找
            ds = (Element) segmentMap.get(keyChar);
        }

        // STEP2 找到DictSegment，判断词的匹配状态，是否继续递归，还是返回结果
        if (ds != null) {
            if (length > 1) {
                // 词未匹配完，继续往下搜索
                return ds.match(charArray, begin + 1, length - 1, searchHit);
            }
            else if (length == 1) {

                // 搜索最后一个char
                if (ds.nodeState == 1) {
                    // 添加HIT状态为完全匹配
                    searchHit.setMatch();
                }
                if (ds.hasNextNode()) {
                    // 添加HIT状态为前缀匹配
                    searchHit.setPrefix();
                    // 记录当前位置的DictSegment
                    searchHit.setMatchedElement(ds);
                }
                return searchHit;
            }

        }
        // STEP3 没有找到DictSegment， 将HIT设置为不匹配
        return searchHit;
    }


    /**
     * 加载填充词典片段
     *
     * @param charArray
     */
    void fillElement(char[] charArray) {
        this.fillElement(charArray, 0, charArray.length);
    }

    /**
     * 递归将一个词按每个字加入字典树
     *
     * @param charArray  eg: 一两千块
     * @param begin
     * @param length
     */
    private synchronized void fillElement(char[] charArray, int begin, int length) {
        // 获取字典表中的汉字对象
        Character beginChar = new Character(charArray[begin]); // eg: 一

        // 字典中没有该字，则将其添加入字典
//        if (!Utility.totalCharSet.contains(beginChar)) {
//            Utility.totalCharSet.add(beginChar);
//        }

        Character keyChar = beginChar;

        // 搜索当前节点的存储，查询对应keyChar的keyChar，如果没有则创建
        Element ds = lookforOrCreateSegment(keyChar);
        if (ds != null) {
            // 处理keyChar对应的segment
            if (length > 1) {
                // 词元还没有完全加入词典树
                ds.fillElement(charArray, begin + 1, length - 1);
            }
            else if (length == 1) {
                // 已经是词元的最后一个char,设置当前节点状态为enabled，
                // enabled=1表明一个完整的词，enabled=0表示从词典中屏蔽当前词
                ds.nodeState = 1;
            }
        }

    }


    /**
     * 查找本节点下对应的keyChar的segment *
     *
     * @param keyChar eg:一
     * @return
     */
    private Element lookforOrCreateSegment(Character keyChar) {
        Element ds = null;

        // 获取Map容器，如果Map未创建,则创建Map
        Map<Character, Element> segmentMap = getOrCreateChildrenMap();
        // 搜索Map
        ds = (Element) segmentMap.get(keyChar);
        if (ds == null) {
            // 构造新的segment
            ds = new Element(keyChar);
            segmentMap.put(keyChar, ds);
            // 当前节点存储segment数目+1
            this.storeSize++;
        }

        return ds;

//        if (this.storeSize <= ARRAY_LENGTH_LIMIT) {
//            // 获取数组容器，如果数组未创建则创建数组
//            Element[] elementArray = getChildrenArray();
//            // 搜寻数组
//            Element keyElement = new Element(keyChar); // eg:一
//            int position = Arrays.binarySearch(elementArray, 0, this.storeSize, keyElement);
//            if (position >= 0) {
//                ds = elementArray[position];
//            }
//
//            // 遍历数组后没有找到对应的segment
//            if (ds == null) {
//                ds = keyElement;
//                if (this.storeSize < ARRAY_LENGTH_LIMIT) {
//                    // 数组容量未满，使用数组存储
//                    elementArray[this.storeSize] = ds;
//                    // segment数目+1
//                    this.storeSize++;
//                    Arrays.sort(elementArray, 0, this.storeSize);
//
//                } else {
//                    // 数组容量已满，切换Map存储
//                    // 获取Map容器，如果Map未创建,则创建Map
//                    Map<Character, Element> segmentMap = getOrCreateChildrenMap();
//                    // 将数组中的segment迁移到Map中
//                    migrate(elementArray, segmentMap);
//                    // 存储新的segment
//                    segmentMap.put(keyChar, ds);
//                    // segment数目+1 ， 必须在释放数组前执行storeSize++ ， 确保极端情况下，不会取到空的数组
//                    this.storeSize++;
//                    // 释放当前的数组引用
//                    this.childrenArray = null;
//                }
//
//            }
//
//        }
//        else {
//            // 获取Map容器，如果Map未创建,则创建Map
//            Map<Character, Element> segmentMap = getOrCreateChildrenMap();
//            // 搜索Map
//            ds = (Element) segmentMap.get(keyChar);
//            if (ds == null) {
//                // 构造新的segment
//                ds = new Element(keyChar);
//                segmentMap.put(keyChar, ds);
//                // 当前节点存储segment数目+1
//                this.storeSize++;
//            }
//        }
    }


    /**
     * 获取数组容器 线程同步方法
     */
    private Element[] getChildrenArray() {
        if (this.childrenArray == null) {
            synchronized (this) {
                if (this.childrenArray == null) {
                    this.childrenArray = new Element[ARRAY_LENGTH_LIMIT];
                }
            }
        }
        return this.childrenArray;
    }


    /**
     * 获取Map容器 线程同步方法
     */
    private Map<Character, Element> getOrCreateChildrenMap() {
        if (this.childrenMap == null) {
            synchronized (this) {
                if (this.childrenMap == null) {
                    this.childrenMap = new HashMap<Character, Element>(ARRAY_LENGTH_LIMIT * 2, 0.8f);
                }
            }
        }
        return this.childrenMap;
    }

    public Map<Character, Element> getChildMap() {
        return this.childrenMap;
    }


    /**
     * 将数组中的segment迁移到Map中
     *
     * @param elementArray
     */
    private void migrate(Element[] elementArray, Map<Character, Element> segmentMap) {
        for (Element element : elementArray) {
            if (element != null) {
                segmentMap.put(element.nodeChar, element);
            }
        }
    }


    /**
     * 实现Comparable接口
     *
     * @param o
     * @return int
     */
    public int compareTo(Element o) {
        // 对当前节点存储的char进行比较
        return this.nodeChar.compareTo(o.nodeChar);
    }

}