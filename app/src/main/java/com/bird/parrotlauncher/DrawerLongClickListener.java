package com.bird.parrotlauncher;


import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawerLongClickListener implements AdapterView.OnItemLongClickListener {

    Context mContext;
    SlidingDrawer drawerForAdapter;
    RelativeLayout homeViewForAdapter;
    Pac[] pacsForListener;


    public DrawerLongClickListener(Context ctxt, SlidingDrawer slidingDrawer, RelativeLayout homeView, Pac[] pacs){
        mContext = ctxt;
        drawerForAdapter = slidingDrawer;
        homeViewForAdapter =homeView;
        pacsForListener = pacs;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View item, int pos, long arg3) {
        MainActivity.appLaunchable=false;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(item.getWidth(),item.getHeight());
        lp.leftMargin= (int) item.getX();
        lp.topMargin = (int) item.getY();

        LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout ll = (LinearLayout) li.inflate(R.layout.drawer_item, null);

        ((ImageView)ll.findViewById(R.id.icon_image)).setImageDrawable(((ImageView) item.findViewById(R.id.icon_image)).getDrawable());
        ((TextView)ll.findViewById(R.id.icon_text)).setText(((TextView) item.findViewById(R.id.icon_text)).getText());

        ll.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                v.setOnTouchListener(new AppTouchListener());
                return false;
            }
        });


        ll.setOnClickListener(new AppClickListener(pacsForListener, mContext));



        AppSerializableData objectData = SerializationTools.loadSerializedData();
        if (objectData == null)
            objectData = new AppSerializableData();

        if (objectData.apps == null)
            objectData.apps = new ArrayList<Pac>();


        Pac pacToAdd = pacsForListener[pos];
        pacToAdd.x = lp.leftMargin;
        pacToAdd.y = lp.topMargin;
        if (MainActivity.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            pacToAdd.landscape = true;
        else
            pacToAdd.landscape = false;

        pacToAdd.cacheIcon();
        ll.setTag(pacToAdd);
        objectData.apps.add(pacToAdd);
        SerializationTools.serializeData(objectData);



        homeViewForAdapter.addView(ll, lp);
        drawerForAdapter.animateClose();
        drawerForAdapter.bringToFront();
        return false;
    }
}
