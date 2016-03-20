package com.bird.parrotlauncher;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;

public class AppClickListener implements View.OnClickListener{
    Pac[] pacsForListener;
    Context mContext;

    public AppClickListener(Pac[] pacs, Context ctxt) {
        pacsForListener = pacs;
        mContext = ctxt;
    }

    @Override
    public void onClick(View v) {
        Pac data;
        data= (Pac) v.getTag();

        Intent launchIntent = new Intent(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cp = new ComponentName(data.packageName,data.name);
        launchIntent.setComponent(cp);
        mContext.startActivity(launchIntent);
    }
}
