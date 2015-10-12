package com.DKB.shutupdrive.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.DKB.shutupdrive.R;

/**
 * Created by kyle on 10/11/15.
 */
public class DotsPageIndicator extends LinearLayout implements ViewPager.OnPageChangeListener {
    private int pages, padding;
    private float size;
    private int dotColor, dotSelectedColor;

    public DotsPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DotsPageIndicator,
                0, 0);
        try {
            padding = (int) a.getDimension(R.styleable.DotsPageIndicator_padding, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
            size = a.getDimension(R.styleable.DotsPageIndicator_dotSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            dotColor = a.getColor(R.styleable.DotsPageIndicator_dotColor, Color.WHITE);
            dotSelectedColor = a.getColor(R.styleable.DotsPageIndicator_dotSelectedColor, Color.DKGRAY);
        } finally {
            a.recycle();
        }
        this.setGravity(Gravity.CENTER);
        this.setOrientation(HORIZONTAL);
    }

    public void setViewPager(ViewPager pager) {
        ViewPager pager1 = pager;
        pager1.addOnPageChangeListener(this);
        this.pages = pager1.getAdapter().getCount();
        createDots();
    }

    private void createDots(){
        for (int i = 0; i < pages; i++) {
            TextView tv = new TextView(getContext());
            tv.setText(getContext().getString(R.string.dot));
            tv.setTextSize(size);
            tv.setPadding(padding, padding, padding, padding);
            tv.setGravity(Gravity.CENTER);
            if (i == 0) {
                tv.setTextColor(dotSelectedColor);
            } else {
                tv.setTextColor(dotColor);
            }
            this.addView(tv);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < pages; i++) {
            if (i == position)
                ((TextView) this.getChildAt(i)).setTextColor(dotSelectedColor);
            else
                ((TextView) this.getChildAt(i)).setTextColor(dotColor);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
