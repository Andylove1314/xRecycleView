package com.jcodecraeer.xrecyclerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yin on 2016/6/13.
 */
public class XScrollView extends ScrollView {

    private boolean isLoadingData = false;
    private boolean isNoMore = false;
    private int mRefreshProgressStyle = ProgressStyle.SysProgress;
    private int mLoadingMoreProgressStyle = ProgressStyle.SysProgress;
    private ArrayList<View> mHeaderViews = new ArrayList<>();
    private ArrayList<View> mFootViews = new ArrayList<>();
    private RecyclerView.Adapter mWrapAdapter;
    private float mLastY = -1;
    private static final float DRAG_RATE = 3;
    private LoadingListener mLoadingListener;
    private ArrowRefreshHeader mRefreshHeader;
    private ArrowRefreshFooterer mRefreshFooter;
    private boolean pullRefreshEnabled = true;
    private boolean loadingMoreEnabled = true;
    private static final int TYPE_REFRESH_HEADER = -5;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = -3;
    private static final int HEADER_INIT_INDEX = 10000;
    private static List<Integer> sHeaderTypes = new ArrayList<>();
    private int mPageCount = 0;

//    private View container;
    private LinearLayout mLinearLayout;
    private Context mContext;
    private boolean flag = true;
    private boolean flag1 = true;
    private boolean flag2 = true;
    private boolean headElastic = false;
    private boolean footElastic = false;
    private boolean headGone = false;
    private boolean footGone = false;
    private int scrollHeight = 0;
    private int height = 0;
    private int width = 0;

    public void setWidth(int width) {
        this.width = width;
    }

    public XScrollView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public XScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public XScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        if (pullRefreshEnabled) {
            ArrowRefreshHeader refreshHeader = new ArrowRefreshHeader(getContext());
            mHeaderViews.add(0, refreshHeader);
            mRefreshHeader = refreshHeader;
            mRefreshHeader.setProgressStyle(mRefreshProgressStyle);
        }
        if (loadingMoreEnabled){
            ArrowRefreshFooterer refreshFooterer = new ArrowRefreshFooterer(getContext());
            mFootViews.add(refreshFooterer);
            mRefreshFooter = refreshFooterer;
            mRefreshFooter.setProgressStyle(mRefreshProgressStyle);
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (flag){
                    try {
                        addHeadAndFoot();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (loadingMoreEnabled){
                        height = getHeight();
                    }
                    flag = false;
                }
            }
        });
        setFillViewport(true);
    }

    public void setContainerHeight(int h) {
        scrollHeight = h;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addHeadAndFoot() {
        if (getChildCount() == 1){
            mLinearLayout = (LinearLayout) getChildAt(0);
            for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
                View childAt = mLinearLayout.getChildAt(i);
                ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                layoutParams.width = width == 0 ? getScreenWidth(mContext) : width;
                childAt.setLayoutParams(layoutParams);
            }
//            removeAllViews();
        }
//        mLinearLayout = new LinearLayout(mContext);

//        mLinearLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < mHeaderViews.size(); i++) {
            if ((mHeaderViews.get(i).getParent()) != null) {
                ((ViewGroup) mHeaderViews.get(i).getParent()).removeAllViews();
            }
            mLinearLayout.addView(mHeaderViews.get(i), 0);
        }

        mLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (flag1 && loadingMoreEnabled){
                    mLinearLayout.measure(0,0);
                    scrollHeight = Math.max(mLinearLayout.getMeasuredHeight(), scrollHeight);
                    if (scrollHeight > 0){
                        flag1 = false;
                    }
                }else if (flag2){
                    // 使XScrollView可滑动
                        View v = mLinearLayout.getChildAt(1);
                        View view = mLinearLayout.getChildAt(2);
                        if (view instanceof TextView){
                            v.measure(0,0);
                            mLinearLayout.measure(0,0);
                            if (v.getMeasuredHeight() <= getHeight()) {
                                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                                if (layoutParams.height != getHeight() - v.getMeasuredHeight() + 10) {
                                    layoutParams.height = getHeight() - v.getMeasuredHeight() + 10;
                                    view.setLayoutParams(layoutParams);
                                }else if(v.getMeasuredHeight() > 0){
                                    flag2 = false;
                                }
                            }else if (v.getMeasuredHeight() > getHeight()){
                                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                                layoutParams.height = 0;
                                view.setLayoutParams(layoutParams);
                                flag2 = false;
                            }
                        }
                }
            }
        });
        for (int i = 0; i < mFootViews.size(); i++) {
            if ((mFootViews.get(i).getParent()) != null) {
                ((ViewGroup) mFootViews.get(i).getParent()).removeAllViews();
            }
            mLinearLayout.addView(mFootViews.get(i));
        }
//        if (getChildCount() == 0) {
//            addView(mLinearLayout);
//        }
        mLinearLayout.measure(0, 0);
        measure(0, 0);
    }

    public void setElastic(boolean elastic) {
        this.headElastic = elastic;
        this.footElastic = elastic;
        mRefreshHeader.setElastic(elastic);
        mRefreshFooter.setElastic(elastic);
    }
    public void setHeadElastic(boolean elastic) {
        this.headElastic = elastic;
        mRefreshHeader.setElastic(elastic);
    }
    public void setFootElastic(boolean elastic) {
        this.footElastic = elastic;
        mRefreshFooter.setElastic(elastic);
    }
    public void setHeadGone(boolean gone) {
        this.headGone = gone;
        mRefreshHeader.setElastic(true);
        mRefreshHeader.setNoDelay();
    }
    public void setFootGone(boolean gone) {
        this.footGone = gone;
        mRefreshFooter.setElastic(true);
        mRefreshFooter.setNoDelay();
    }

    public void addHeaderView(View view) {
        if (pullRefreshEnabled && !(mHeaderViews.get(0) instanceof ArrowRefreshHeader)) {
            ArrowRefreshHeader refreshHeader = new ArrowRefreshHeader(getContext());
            mHeaderViews.add(0, refreshHeader);
            mRefreshHeader = refreshHeader;
            mRefreshHeader.setProgressStyle(mRefreshProgressStyle);
        }
        mHeaderViews.add(view);
        sHeaderTypes.add(HEADER_INIT_INDEX + mHeaderViews.size());
    }

    public void addFootView(final View view) {
        if (loadingMoreEnabled && !(mFootViews.get(0) instanceof ArrowRefreshFooterer)) {
            ArrowRefreshFooterer refreshHeader = new ArrowRefreshFooterer(getContext());
            mFootViews.add(0, refreshHeader);
            mRefreshFooter = refreshHeader;
            mRefreshFooter.setProgressStyle(mRefreshProgressStyle);
        }
        mHeaderViews.add(view);
        sHeaderTypes.add(HEADER_INIT_INDEX + mHeaderViews.size());
    }

    public void reset(){
        loadMoreComplete();
        refreshComplete();
    }

    private void loadMoreComplete() {
        if (loadingMoreEnabled){
            mRefreshFooter.refreshComplete();
        }
    }
//
//    public void noMoreLoading() {
//        isLoadingData = false;
//        View footView = mFootViews.get(0);
//        isNoMore = true;
//        if (footView instanceof LoadingMoreFooter) {
//            ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_NOMORE);
//        } else {
//            footView.setVisibility(View.GONE);
//        }
//    }

    public void refreshComplete() {
        mRefreshHeader.refreshComplete();
    }

    public void setRefreshHeader(ArrowRefreshHeader refreshHeader) {
        mRefreshHeader = refreshHeader;
    }

    public void setPullRefreshEnabled(boolean enabled) {
        pullRefreshEnabled = enabled;
    }

    public void setLoadingMoreEnabled(boolean enabled) {
        loadingMoreEnabled = enabled;
        if (!enabled && mFootViews.size() > 0) {
            mFootViews.get(0).setVisibility(GONE);
        }
    }

    private OnButtonListener mOnButtonListener;
    public interface OnButtonListener{
        void isButton();
    }
    public void setOnButtonListener(OnButtonListener listener) {
        this.mOnButtonListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && pullRefreshEnabled) {
                    mRefreshHeader.onMove(deltaY / DRAG_RATE);
                    if (mRefreshHeader.getVisibleHeight() > 0 && mRefreshHeader.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
//                        Log.i("getVisibleHeight", "getVisibleHeight = " + mRefreshHeader.getVisibleHeight());
//                        Log.i("getVisibleHeight", " mRefreshHeader.getState() = " + mRefreshHeader.getState());
                        return false;
                    }
                }
                if (isOnButton() && loadingMoreEnabled) {
                    mRefreshFooter.onMove(-deltaY / DRAG_RATE);
//                    if (mRefreshFooter.getVisibleHeight() > 0 && mRefreshFooter.getState() < ArrowRefreshFooterer.STATE_REFRESHING) {
//                        Log.i("getVisibleHeight", "getVisibleHeight = " + mRefreshHeader.getVisibleHeight());
//                        Log.i("getVisibleHeight", " mRefreshHeader.getState() = " + mRefreshHeader.getState());
//                        return false;
//                    }
                }
                if (isOnButtonMain() && !loadingMoreEnabled) {
                    mOnButtonListener.isButton();
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled) {
                    if (mRefreshHeader.releaseAction()) {
                        if (mLoadingListener != null && !headElastic) {
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    try {
                                        mLoadingListener.onRefresh();
                                    } catch (Exception e) {
                                    }
                                }
                            }, 300);
                        }else if(headElastic){
                            mRefreshHeader.reset();
                        }
                    }
                }
                if (isOnButton() && loadingMoreEnabled) {
                    if (mRefreshFooter.releaseAction()) {
                        if (mLoadingListener != null && !footElastic) {
                            int delay = 300;
                            if (footGone || headGone){
                                delay = 0;
                            }
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    try {
                                        mLoadingListener.onLoadMore();
                                    } catch (Exception e) {
                                    }
                                }
                            }, delay);
                        }else if(footElastic){
                            mRefreshFooter.reset();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private boolean isOnTop() {
        return !(mHeaderViews == null || mHeaderViews.isEmpty()) && getScrollY() == 0;
    }

    private boolean isOnButton() {
        if (height > scrollHeight && scrollHeight > 0) {
            ViewGroup.LayoutParams layoutParams = mLinearLayout.getLayoutParams();
            layoutParams.height = height;
            mLinearLayout.setLayoutParams(layoutParams);
        }
        if (scrollHeight > height){
            return !(mFootViews == null || mFootViews.isEmpty()) && mFootViews.get(0).getParent() != null && getScrollY() >= (scrollHeight - height);
        }else {
            return !(mFootViews == null || mFootViews.isEmpty()) && mFootViews.get(0).getParent() != null;
        }
    }

    private boolean isOnButtonMain() {
        if (height > scrollHeight && scrollHeight > 0) {
            ViewGroup.LayoutParams layoutParams = mLinearLayout.getLayoutParams();
            layoutParams.height = height;
            mLinearLayout.setLayoutParams(layoutParams);
        }
        if (scrollHeight > height){
            return !(mFootViews == null || mFootViews.isEmpty()) && mFootViews.get(0).getParent() != null && getScrollY() >= (scrollHeight - 200 - height);
        }else {
            return !(mFootViews == null || mFootViews.isEmpty()) && mFootViews.get(0).getParent() != null;
        }
    }


    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }


    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && pullRefreshEnabled && mLoadingListener != null) {
            mRefreshHeader.setState(ArrowRefreshHeader.STATE_REFRESHING);
            mRefreshHeader.onMove(mRefreshHeader.getMeasuredHeight());
            try {
                mLoadingListener.onRefresh();
            } catch (Exception e) {
            }
        }
    }
    public int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
