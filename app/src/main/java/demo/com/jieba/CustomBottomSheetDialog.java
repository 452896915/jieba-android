package demo.com.jieba;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.TextView;

import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;

public class CustomBottomSheetDialog extends BottomSheetDialog {
    private Context context;

    public CustomBottomSheetDialog(@NonNull Context context, ArrayList<String> targetItems) {
        super(context);
        this.context = context;

        create(targetItems);
    }

    public void create(ArrayList<String> targetItems) {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.divided_sentence_layout, null);
        setContentView(bottomSheetView);

        ((View)bottomSheetView.getParent()).setBackgroundColor(context.getResources().getColor(R.color.transparent));

        DivideCard divideCard = bottomSheetView.findViewById(R.id.divide_layout);
        divideCard.setWords(targetItems);
    }
}
