package jackmego.com.jieba_android;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Utility {
    public static final String LOGTAG = "logtag";
    // 公用字典表，存储单词的第一个字
//    public static final Set<Character> totalCharSet = new HashSet<>(16, 0.95f);

    public static void writeElemToFile(Context context, Element element) {
        try {
            FileOutputStream fos = context.openFileOutput("test.dat", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(element);

            oos.close();
            fos.close();

            Log.d(LOGTAG, "writeElemToFile success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Element readElemFromFile(Context context) {
        Log.d(LOGTAG, "start readElemFromFile");
        long start = System.currentTimeMillis();
        try{
            InputStream is = context.openFileInput("test.dat");
            ObjectInputStream ois = new ObjectInputStream(is);
            Element tc = (Element)ois.readObject() ;
            System.out.println(tc);

            ois.close();
            is.close();

            long end = System.currentTimeMillis();
            Log.d(LOGTAG, String.format("readElemFromFile takes %d ms", end-start));

            Log.d(LOGTAG, "end readElemFromFile");

            return tc;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
