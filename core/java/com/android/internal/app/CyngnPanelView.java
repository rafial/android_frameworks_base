package com.android.internal.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CyngnPanelView extends LinearLayout implements View.OnClickListener {

    private static final int EXPANDED_SIZE_DP = (int)(54 + 92 * 2.5); // show 2.5 tiles
    private static final int INITIAL_TRANSLATE_SIZE = 146;

    private View mContentView;
    private View mDragBar;

    private int mCollapsedHeight;
    private int mExpandedHeight;
    private int mTranslateY;
    private Context mContext;

    public CyngnPanelView(Context context) {
        this(context, null, 0);
    }

    public CyngnPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CyngnPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = getChildAt(0);
        mDragBar = getChildAt(1);
        mDragBar.setOnClickListener(this);

        mCollapsedHeight = mContentView.getLayoutParams().height;
        mExpandedHeight = dpToPx(EXPANDED_SIZE_DP);
        mTranslateY = -dpToPx(INITIAL_TRANSLATE_SIZE);
    }

    public int getInitialTranslateY() {
        return mTranslateY;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    public void onClick(View view) {
        // Drag bar clicked
        if (isExpanded()) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        animate(mContentView.getHeight(), mExpandedHeight);
    }

    public void collapse() {
        animate(mContentView.getHeight(), mCollapsedHeight);
    }

    public boolean isExpanded() {
        return mContentView.getHeight() == mExpandedHeight;
    }

    private void animate(int from, int to) {
        ValueAnimator anim =
                ValueAnimator.ofInt(from, to);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer)valueAnimator.getAnimatedValue();
                updateHeight(val);
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private void updateHeight(int height) {
        int clamped = Math.min(Math.max(height, mCollapsedHeight), mExpandedHeight);
        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        params.height = clamped;
        mContentView.setLayoutParams(params);
    }

    private void settle(float y) {
        if (Math.abs(y - mCollapsedHeight) < Math.abs(mExpandedHeight - y)) {
            collapse();
        } else {
            expand();
        }
    }

    private boolean mCapturedDragView;
    private float mTouchY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        float x = ev.getX();
        float y = ev.getY();

        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCapturedDragView = false;

                if (isDragViewUnder((int)x, (int)y)) {
                    mCapturedDragView = true;
                    mTouchY = y;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mCapturedDragView) {
                    float dy = y - mTouchY;
                    ViewConfiguration vc = ViewConfiguration.get(mContext);
                    if (Math.abs(dy) > vc.getScaledTouchSlop()) {
                        return true;
                    }
                }
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                settle(y);
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.i("EMAN", "top = " + mContentView.getTop() + ", y=" + y);
                float dy = y - mTouchY;
                int height = mContentView.getHeight() + (int)dy;
                //int height = (int)y - mContentView.getTop();
                updateHeight(height);
                break;
            case MotionEvent.ACTION_CANCEL:
                settle(y);
                break;
        }

        mTouchY = y;
        return true;
    }

    private boolean isDragViewUnder(int x, int y) {
        View dragView = mDragBar;
        if (dragView == null) return false;
        int[] viewLocation = new int[2];
        dragView.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + dragView.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + dragView.getHeight();
    }
}
