package jackmego.com.jieba_android;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static jackmego.com.jieba_android.Utility.LOGTAG;

public class WordDictionary {
    private static WordDictionary singleton;

    private static final String MAIN_DICT = "jieba/dict.txt";
    private static final String MAIN_PROCESSED = "dict_processed.txt";
    private static final String OUTFILE = "jieba/" + MAIN_PROCESSED;

    public final Map<String, Double> freqs = new HashMap<String, Double>(); // 加载中间文件的话，这里也要改
    public Element restoredElement; // 生成中间文件的时候要修改这个变量的引用为element
    private Double minFreq = Double.MAX_VALUE;
    private Double total = 0.0;
    public Element element;
    private StringBuilder dicLineBuild = new StringBuilder();
    private final String TAB = "\t";
    private final String SHARP = "#";
    private final String SLASH = "/";
    private final String DOLLAR = "$";

    private WordDictionary(AssetManager assetManager) {
        // 加载字典树
        // 分两种情况：预处理和实际运行加载，预处理的时候执行preProcess函数，会将字典树生成中间文件存储到文本中
        // 实际运行的时候，直接从Asset加载文本文件通过restoreElement函数恢复成字典树
//      preProcess(assetManager);

        // 实际运行的时候直接使用下面的代码加载该中间文件
        long start = System.currentTimeMillis();

        // 加载字典文件
        List<String> strArray = getStrArrayFromFile(assetManager);

        if (strArray == null) {
            Log.d(LOGTAG, "getStrArrayFromFile failed, stop");
            return;
        }

        restoredElement = new Element((char) 0);
        ArrayList<Element> elemArr = new ArrayList<>();
        elemArr.add(restoredElement);

        restoreElement(elemArr, strArray, 0);

        long end = System.currentTimeMillis();
        Log.d(LOGTAG, String.format("restoreElement takes %d ms", end-start));
    }

    /**
     * 预处理，生成中间文件
     * @param assetManager
     */
    private void preProcess(AssetManager assetManager) {
        boolean result = this.loadDict(assetManager);

        if (result) {
            ArrayList<Element> arr = new ArrayList<>();
            arr.add(element);
            saveDictToFile(arr);
        } else {
            Log.e(LOGTAG, "Error");
        }



        /* 这段代码用于测试从sd卡读取
        List<String> strArray = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), MAIN_PROCESSED);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readline = br.readLine();

            strArray = java.util.Arrays.asList(readline.split(TAB));
            br.close();
            Log.d(LOGTAG, "读取成功：" + readline);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * d/b/c/	g/	f/e/	#/	j/	#/	h/	#/	#/
     */
    private void restoreElement(ArrayList<Element> elemArray, List<String> strArray, int startIndex) {
        if (elemArray.size() <= 0) {
            return;
        }

        ArrayList<Element> newElemArray = new ArrayList<>();

        for (int i = 0; i < elemArray.size(); i++) {
            String strCluster = strArray.get(startIndex);
            String[] strList = strCluster.split(SLASH);

            Element e = elemArray.get(i);
            // #/
            if (strList.length == 1 && strList[0].equalsIgnoreCase(SHARP)) {
                e.nodeState = 1;
                e.storeSize = 0;
            } else { //  f/e/
                e.childrenMap = new HashMap<>();
                for (int j = 0; j < strList.length; j++) {
                    String s = strList[j];
                    boolean isWord = s.length() == 2;
                    Character ch = new Character(s.charAt(0));
                    Element childElem = new Element(ch);
                    childElem.nodeState = isWord ? 1 : 0;

                    e.childrenMap.put(ch, childElem);
                    e.storeSize++;

                    newElemArray.add(childElem);
                }
            }

            startIndex++;
        }

        restoreElement(newElemArray, strArray, startIndex);
    }


    /**
     *              ROOT
     *        b/  -- c$/   --  d/
     *       e$/f/ -- #/   --  g/
     *       h$/ ---- #/  ---- i$/
     *       #/  --------- #/
     * @param elementArray
     */
    private void saveDictToFile(ArrayList<Element> elementArray) {
        if (elementArray.size() <= 0) {
            Log.d(LOGTAG, "saveDictToFile final str: " + dicLineBuild.toString());

            try {
                File file = new File(Environment.getExternalStorageDirectory(), MAIN_PROCESSED);

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(file);

                // 第一行是字典数据
                dicLineBuild.append("\r\n");

                // 第二行： 最小频率 TAB 单词1 TAB 频率 TAB 单词2 TAB 频率 ...
                dicLineBuild.append(minFreq);

                for (Map.Entry<String, Double> entry : freqs.entrySet()) {
                    dicLineBuild.append(TAB);
                    dicLineBuild.append(entry.getKey());
                    dicLineBuild.append(TAB);
                    dicLineBuild.append(entry.getValue());
                }

                fos.write(dicLineBuild.toString().getBytes());

                fos.close();

                Log.d(LOGTAG, String.format("字典中间文件生成成功，存储在%s", file.getAbsolutePath()));
            } catch (Exception e) {
                Log.d(LOGTAG, "字典中间文件生成失败！");
                e.printStackTrace();
            }

            return;
        }

        ArrayList<Element> childArray = new ArrayList();
        // elementArray有几个元素，就要添加TAB分割的几个数据段，每个数据段是该Element的子节点的字+"/"，比如 e/f/ TAB #/ TAB g/
        // 如果从根节点到当前节点的路径表示一个词，那么在后面添加$符号,如  e$/f/ TAB #/ TAB g/
        for (int i = 0; i < elementArray.size(); i++) {
            Element element = elementArray.get(i);

            // e/f/
            if (element.hasNextNode()) {
                for (Map.Entry<Character, Element> entry : element.childrenMap.entrySet()) {
                    dicLineBuild.append(entry.getKey());

                    if (entry.getValue().nodeState == 1) {
                        dicLineBuild.append(DOLLAR);  // 从根节点到当前节点的路径表示一个词，那么在后面添加$符号,如  e$/f/ TAB #/ TAB g/
                    }

                    dicLineBuild.append(SLASH);

                    // 将该节点的所有子节点入列表，供下一次递归
                    childArray.add(entry.getValue());
                }
            } else { // #/
                dicLineBuild.append(SHARP);
                dicLineBuild.append(SLASH);
            }

            // TAB
            dicLineBuild.append(TAB);
        }

        saveDictToFile(childArray);
    }


    public static WordDictionary getInstance(AssetManager assetManager) {
        if (singleton == null) {
            synchronized (WordDictionary.class) {
                if (singleton == null) {
                    singleton = new WordDictionary(assetManager);
                    return singleton;
                }
            }
        }
        return singleton;
    }

    public boolean loadDict(AssetManager assetManager) {
        element = new Element((char) 0); // 创建一个根Element，只有一个，其他的Element全是其子孙节点
        InputStream is = null;
        try {
            long start = System.currentTimeMillis();
            is = assetManager.open(MAIN_DICT);

            if (is == null) {
                Log.e(LOGTAG, "Load asset file error:" + MAIN_DICT);
                return false;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            long s = System.currentTimeMillis();
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 2)
                    continue;

                String word = tokens[0]; // eg:一两千块
                double freq = Double.valueOf(tokens[1]);
                total += freq;
                String trimmedword = addWord(word);  // 将一个单词的每个字递归的插入字典树  eg:一两千块
                freqs.put(trimmedword, freq);        // 并统计单词首个字的频率
            }

            // normalize
            for (Map.Entry<String, Double> entry : freqs.entrySet()) {
                entry.setValue((Math.log(entry.getValue() / total)));
                minFreq = Math.min(entry.getValue(), minFreq);
            }
            Log.d(LOGTAG, String.format("main dict load finished, time elapsed %d ms",
                    System.currentTimeMillis() - s));
        } catch (IOException e) {
            Log.e(LOGTAG, String.format("%s load failure!", MAIN_DICT));
            return false;
        } finally {
            try {
                if (null != is)
                    is.close();
            }
            catch (IOException e) {
                Log.e(LOGTAG, String.format("%s close failure!", MAIN_DICT));
                return false;
            }
        }

        return true;
    }


    /**
     * 将一个单词的每个字递归的插入字典树
     * @param word
     * @return
     */
    private String addWord(String word) {
        if (null != word && !"".equals(word.trim())) {
            String key = word.trim().toLowerCase(Locale.getDefault());
            element.fillElement(key.toCharArray());
            return key;
        }
        else
            return null;
    }

    public Element getTrie() {
        return this.restoredElement; //this.element;
    }


    public boolean containsWord(String word) {
        return freqs.containsKey(word);
    }


    public Double getFreq(String key) {
        if (containsWord(key))
            return freqs.get(key);
        else
            return minFreq;
    }

    public List<String> getStrArrayFromFile(AssetManager assetManager) {
        List<String> strArray;

        InputStream is = null;
        try {
            is = assetManager.open(OUTFILE);

            if (is == null) {
                Log.e(LOGTAG, "Load asset file error:" + OUTFILE);
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            // 第一行是字典文件
            String dictLine = br.readLine();
            strArray = java.util.Arrays.asList(dictLine.split(TAB));

            // 第二行是：最小频率 TAB 单词1 TAB 频率 TAB 单词2 TAB 频率 ...
            String freqLine = br.readLine();
            final List<String> strArray2 = java.util.Arrays.asList(freqLine.split(TAB));
            minFreq = Double.valueOf(strArray2.get(0));

            final int wordCnt = (strArray2.size() - 1) / 2;

            // freqs.put操作需要3秒才能完成，所以放在一个线程中异步进行，在map加载完成之前调用分词会不那么准确，但是不会报错
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < wordCnt; i++) {
                        freqs.put(strArray2.get(2 * i + 1), Double.valueOf(strArray2.get(2 * i + 2)));
                    }
                }
            }).start();

            br.close();
            is.close();

            return strArray;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
