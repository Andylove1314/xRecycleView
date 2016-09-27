package com.app.card.guoancard;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kun on 2016/4/11.
 */
public class MyIndicator extends HorizontalScrollView {

    private Context mContext;
    protected int screenWidth;
    private int item_width;
    private List<Float> listWidth = new ArrayList<>();
    /**
     * 默认为5个item平分screenWidth
     */
    private static int COUNT = 5;
    /**
     * 分类数量
     */
    private int count;
    private LinearLayout mLayout;
    private LinearLayout container;
    private OnSelectedListener onSelectedListener;
    private View indicator;
    /**
     * 指示器默认的左边距
     */
    private float defaultLeft;
    private String state = FULL;// full fit

    public static final String FULL = "full";
    public static final String FIT = "fit";

    public void setState(String state) {
        this.state = state;
    }

    /**
     * 设置选择监听
     *
     * @param onSelectedListener
     */


    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    public MyIndicator(Context context) {
        super(context);
        init(context);
    }

    public MyIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mContext = context;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        mLayout = new LinearLayout(context);
        mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mLayout.setOrientation(LinearLayout.VERTICAL);

        container = new LinearLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setOrientation(LinearLayout.HORIZONTAL);
//        if (FIT.equals(state)) {
//            container.setPadding(dip2px(mContext, 10), 0, dip2px(mContext, 10), 0);
//        }
        setLayoutParams(new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        setHorizontalScrollBarEnabled(false);
        mLayout.addView(container);
        addView(mLayout);
        indicator = new View(context);
        indicator.setBackgroundColor(Color.parseColor("#fe564c"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dip2px(context, 3));
        lp.topMargin = dip2px(mContext, 5);
        if (FIT.equals(state)) {
            lp.rightMargin = dip2px(mContext, 20);
        }
        indicator.setLayoutParams(lp);
        mLayout.addView(indicator);
        setBackgroundResource(R.drawable.shape_tab_bg);
        setPadding(0, 0, 0, dip2px(mContext, 5));
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    /**
     * 设置指示器自选项
     */
    public void setIndicatorItems(String... name) {
        count = name.length;
        item_width = screenWidth / (count > COUNT ? COUNT : count);
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            final int index = i;
            TextView title = new TextView(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (FULL.equals(state)) {
                lp.width = item_width;
            }
            title.setPadding(0, dip2px(mContext, 10), 0, 0);
            title.setGravity(Gravity.CENTER);
            title.setText(name[i]);
            title.setLayoutParams(lp);
            title.setSingleLine(true);
            container.addView(title);
            title.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelectedListener.onSelected(index);
                }
            });
        }
        setOnItemSelected(0);
    }

    private void scrollCenter(int index) {
        float x = container.getChildAt(index).getX();
        float c = screenWidth / 2;
        smoothScrollTo((int) ((x + listWidth.get(index) / 2) - c), 0);
    }

    /**
     * 设置指示器自选项
     *
     * @param name
     */
    public void setIndicatorItems(List<String> name) {
        int count = name.size();
        if (count == 0) {
            return;
        }
        item_width = screenWidth / (count > COUNT ? COUNT : count);
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            final int index = i;
            TextView title = new TextView(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (FULL.equals(state)) {
                lp.width = item_width;
                title.setPadding(0, dip2px(mContext, 10), 0, 0);
            } else if (FIT.equals(state)) {
                title.setPadding(dip2px(mContext, 10), dip2px(mContext, 10), dip2px(mContext, 10), 0);
            }
            title.setGravity(Gravity.CENTER);
            title.setText(name.get(i).trim());
            title.setSingleLine(true);
            if (FIT.equals(state)) {
                listWidth.add(title.getPaint().measureText(name.get(i).toString().trim()) + dip2px(mContext, 20));
            }
            title.setLayoutParams(lp);
            container.addView(title);
            title.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelectedListener.onSelected(index);
                    if (FIT.equals(state)){
                        scrollCenter(index);
                    }
                }
            });
        }
        setOnItemSelected(0);
    }

    /**
     * 改变item状态
     *
     * @param index
     */
    private void changeItemStatus(int index) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView title = (TextView) container.getChildAt(i);
            if (i == index) {
                title.setTextColor(Color.parseColor("#fe564c"));
                ViewPropertyAnimator.animate(title).scaleX(1.1f).setDuration(300);
                ViewPropertyAnimator.animate(title).scaleY(1.1f).setDuration(300);
                requestChildFocus(container, title);
            } else {
                title.setTextColor(Color.parseColor("#333333"));
                ViewPropertyAnimator.animate(title).scaleX(1.0f).setDuration(300);
                ViewPropertyAnimator.animate(title).scaleY(1.0f).setDuration(300);
            }
        }
    }

    /**
     * 更新指示器宽度
     *
     * @param index
     */
    private void changeIndicatorWidth(int index) {
        TextView title = (TextView) container.getChildAt(index);
        TextPaint paint = title.getPaint();
        float titleWidth = paint.measureText(title.getText().toString());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) indicator
                .getLayoutParams();
        lp.width = (int) titleWidth + 20;
        if (FULL.equals(state)) {
            defaultLeft = (item_width - lp.width) / 2;
        } else if (FIT.equals(state)) {
            defaultLeft = (listWidth.get(index) - lp.width) / 2;
        }
        indicator.setLayoutParams(lp);
        indicator.invalidate();
    }

    /**
     * 选中
     *
     * @param index
     */
    public void setOnItemSelected(int index) {
        changeItemStatus(index);
        changeIndicatorWidth(index);
        if (FIT.equals(state)){
            scrollCenter(index);
        }
    }

    /**
     * 改变指示器左边距
     *
     * @param index
     * @param positionOffset
     */
    public void changeIndicatorLeft(int index, float positionOffset) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) indicator
                .getLayoutParams();
        int totalWidth = 0;
        if (FULL.equals(state)) {
            totalWidth = index * item_width;
            lp.leftMargin = (int) (positionOffset * item_width + totalWidth + defaultLeft);
        } else if (FIT.equals(state) && index < listWidth.size()) {
            for (int i = 0; i < index; i++) {
                totalWidth += listWidth.get(i);
            }
            lp.leftMargin = (int) (positionOffset * (listWidth.get(index)) + totalWidth + defaultLeft);
        }
        indicator.setLayoutParams(lp);

    }

    public interface OnSelectedListener {
        public void onSelected(int index);
    }

}
