package demo.com.jieba;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;

import jackmego.com.jieba_android.JiebaSegmenter;

public class MainActivity extends AppCompatActivity {
    private TagFlowLayout tagFlowLayout;
    private Button btn;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tagFlowLayout = findViewById(R.id.flow_layout);
        btn = findViewById(R.id.btn);
        editText = findViewById(R.id.editText);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> wordList = JiebaSegmenter.getJiebaSegmenterSingleton().getDividedString(editText.getText().toString());
                tagFlowLayout.setAdapter(new TagAdapter<String>(wordList) {
                    @Override
                    public View getView(FlowLayout parent, int position, String s) {
                        final TextView tv = (TextView) getLayoutInflater().inflate(R.layout.sentence_tv, tagFlowLayout, false);
                        tv.setText(s);

                        return tv;
                    }
                });
            }
        });
    }
}
