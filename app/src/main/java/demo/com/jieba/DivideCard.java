package demo.com.jieba;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DivideCard extends LinearLayout implements TagFlowLayout.OnSelectListener, View.OnClickListener {

    private Context mContext;
    private TextView mCopy;
    private TextView mSelectAll;
    private TextView mTranslate;
    private TextView mSearch;
    private TagFlowLayout mFlowLayout;
    private List<String> mAllWords;
    private Set<Integer> mSelectPosSet;


    public DivideCard(Context context) {
        this(context, null);
    }

    public DivideCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.ui_divider, this);
        mCopy = findViewById(R.id.divide_action_copy);
        mSelectAll = findViewById(R.id.divide_action_all);
        mTranslate = findViewById(R.id.divide_action_translate);
        mSearch = findViewById(R.id.divide_action_search);
        mFlowLayout = findViewById(R.id.ui_divide_flow);
        mFlowLayout.setOnSelectListener(this);

        mCopy.setOnClickListener(this);
        mSelectAll.setOnClickListener(this);
        mTranslate.setOnClickListener(this);
        mSearch.setOnClickListener(this);
    }

    public void setWords(List<String> words) {
        this.mAllWords = words;
        mFlowLayout.setAdapter(new TagAdapter<String>(words) {
            @Override
            public View getView(FlowLayout flowLayout, int i, String word) {
                TextView view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.ui_divider_item, flowLayout, false);
                view.setText(word);
                return view;
            }
        });
    }

    @Override
    public void onSelected(Set<Integer> selectPosSet) {
        this.mSelectPosSet = selectPosSet;
        applyActionsState();
    }

    @Override
    public void onClick(View v) {
        if (v == mSelectAll) {
            if (mSelectPosSet != null && mAllWords.size() == mSelectPosSet.size()) {
                selectNone();
                return;
            }
            selectAll();
        } else if (v == mCopy) {
            copyToClipboard();
        } else if (v == mSearch) {
            search();
        } else if (v == mTranslate) {
            translate();
        }
    }


    private void selectAll() {
        mSelectPosSet = new HashSet<>();
        for (int i = 0; i < mAllWords.size(); i++) {
            mSelectPosSet.add(i);
        }
        mFlowLayout.getAdapter().setSelectedList(mSelectPosSet);
        mSelectAll.setText(R.string.divide_action_none);
        applyActionsState();
    }

    private void selectNone() {
        mSelectPosSet.clear();
        mFlowLayout.getAdapter().setSelectedList(mSelectPosSet);
        mSelectAll.setText(R.string.divide_action_all);
        applyActionsState();
    }

    private String getSelectedWords() {
        StringBuilder sb = new StringBuilder("");
        for (Integer integer : mSelectPosSet) {
            sb.append(mAllWords.get(integer));
        }
        return sb.toString();
    }

    private void applyActionsState() {
        if (mSelectPosSet == null || mSelectPosSet.isEmpty()) {
            mCopy.setEnabled(false);
            mTranslate.setEnabled(false);
            mSearch.setEnabled(false);
        } else {
            mCopy.setEnabled(true);
            mTranslate.setEnabled(true);
            mSearch.setEnabled(true);
        }
    }

    private void copyToClipboard() {
        String content = getSelectedWords();
        ClipboardManager cbm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("trio_divided", content);
        cbm.setPrimaryClip(data);
        Toast.makeText(mContext, "已复制到剪切板", Toast.LENGTH_SHORT).show();
    }

    private void translate() {
        String content = getSelectedWords();
        String url;
        if (isContainChinese(content)) {
            url = "https://fanyi.baidu.com/#zh/en/" + content;
        } else {
            url = "https://fanyi.baidu.com/#en/zh/" + content;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        mContext.startActivity(intent);
    }

    private void search() {
        String content = getSelectedWords();
        String url = "https://www.baidu.com/s?word=" + content;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        mContext.startActivity(intent);
    }

    /**
     * 判断字符串中是否包含中文
     * @param str
     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    private boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }


}
