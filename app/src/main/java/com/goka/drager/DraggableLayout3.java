package com.goka.drager;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class DraggableLayout3 extends ViewGroup {

    private static final float SENSITIVITY = 1.0f;

    private ViewDragHelper viewDragHelper;

    private View headerView;
    private View view;

    private float initialMotionY;

    private int top;
    private int dragRange;
    private float dragOffset;


    public DraggableLayout3(Context context) {
        super(context);
    }

    public DraggableLayout3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableLayout3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public DraggableLayout3(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setup() {
        viewDragHelper = ViewDragHelper.create(this, SENSITIVITY, new ViewDragHelper.Callback() {

            DraggableLayout3 self = DraggableLayout3.this;

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return this.self.headerView == child;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                this.self.top = top;
                this.self.dragOffset = (float) top / this.self.dragRange;
                this.self.view.setAlpha(1 - this.self.dragOffset);
                requestLayout();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                int top = getPaddingTop();
                if (yvel > 0 || (yvel == 0 && this.self.dragOffset > 0.5f)) {
                    top += this.self.dragRange;
                }
                this.self.viewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return this.self.dragRange;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                final int topBound = getPaddingTop();
                final int bottomBound = getHeight() - this.self.headerView.getHeight() - this.self.headerView.getPaddingBottom();
                return Math.min(Math.max(top, topBound), bottomBound);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.headerView = findViewById(R.id.header3);
        this.view = findViewById(R.id.view3);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setup();
    }

    @Override
    public void computeScroll() {
        if (this.viewDragHelper.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

    public void smoothSlideTo(float offset) {
        final int topBound = getPaddingTop();
        float y = topBound + offset * this.dragRange;
        if (this.viewDragHelper.smoothSlideViewTo(this.headerView, this.headerView.getLeft(), (int) y)) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();

        if (action != MotionEvent.ACTION_DOWN) {
            this.viewDragHelper.cancel();
            return super.onInterceptTouchEvent(event);
        }

        final float x = event.getX();
        final float y = event.getY();
        boolean isHeaderViewUnder = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                this.initialMotionY = y;
                isHeaderViewUnder = this.viewDragHelper.isViewUnder(this.headerView, (int) x, (int) y);
                break;
            }
        }

        return this.viewDragHelper.shouldInterceptTouchEvent(event) || isHeaderViewUnder;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.viewDragHelper.processTouchEvent(event);

        final int action = event.getActionMasked();
        final float x = event.getX();
        final float y = event.getY();

        boolean isHeaderViewUnder = this.viewDragHelper.isViewUnder(this.headerView, (int) x, (int) y);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                this.initialMotionY = y;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (isHeaderViewUnder) {
                    final float dy = y - this.initialMotionY;
                    final int slop = this.viewDragHelper.getTouchSlop();
                    if (Math.abs(dy) < Math.abs(slop)) {
                        if (this.dragOffset == 0) {
                            smoothSlideTo(1f);
                        } else {
                            smoothSlideTo(0f);
                        }
                    } else {
                        float headerViewCenterY = this.headerView.getY() + this.headerView.getHeight() / 2;
                        ;
                        if (headerViewCenterY >= getHeight() / 2) {
                            smoothSlideTo(1f);
                        } else {
                            smoothSlideTo(0f);
                        }
                    }
                }
                break;
            }
        }

        return isHeaderViewUnder && isViewHit(this.headerView, (int) y) || isViewHit(this.view, (int) y);
    }

    private boolean isViewHit(View view, int y) {
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int screenY = parentLocation[1] + y;
        return screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.dragRange = getHeight() - this.headerView.getHeight();
        this.headerView.layout(0, this.top, r, this.top + this.headerView.getMeasuredHeight());
        this.view.layout(0, this.top + this.headerView.getMeasuredHeight(), r, this.top + b);
    }
}
