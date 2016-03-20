package com.bird.parrotlauncher;

import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class AppTouchListener implements View.OnTouchListener {

    int leftMargine;
    int topMargine;


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(v.getWidth(),v.getHeight());

                leftMargine = (int) event.getRawX()-v.getWidth()/2;
                topMargine = (int) event.getRawY()-v.getHeight()/2;

                if (leftMargine+v.getWidth() > v.getRootView().getWidth())
                    leftMargine = v.getRootView().getWidth() - v.getWidth();

                if (leftMargine<0)
                    leftMargine =0;

                if (topMargine+v.getHeight() >((View)v.getParent()).getHeight())
                    topMargine = ((View) v.getParent()).getHeight() - v.getHeight();

                if (topMargine<0)
                    topMargine = 0;

                lp.leftMargin = leftMargine;
                lp.topMargin = topMargine;
                v.setLayoutParams(lp);
                break;
            case MotionEvent.ACTION_UP:
                v.setOnTouchListener(null);
                break;

        }
        return true;
    }
}
