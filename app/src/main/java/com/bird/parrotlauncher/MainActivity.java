package com.bird.parrotlauncher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends Activity {
    DrawerAdapter drawerAdapterObject;
    GridView drawerGrid;
    SlidingDrawer slidingDrawer;
    RelativeLayout homeView;
    Pac[] pacs;
    PackageManager pm;
    AppWidgetManager mAppWidgetManager;
    LauncherAppWidgetHost mAppWidgetHost;
    int REQUEST_CREATE_APPWIDGET = 900;
    int numWidgets;
    static Activity activity;




    private ImageButton CallTopButton;



    static boolean appLaunchable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_main);

        activity = this;
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, R.id.APPWIDGET_HOST_ID);

        drawerGrid = (GridView) findViewById(R.id.content);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);
        homeView = (RelativeLayout) findViewById(R.id.home_view);
        pm = getPackageManager();
        set_pacs();
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override
            public void onDrawerOpened() {
                appLaunchable = true;
            }
        });


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(new PacReceiver(), filter);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }



    void selectWidget() {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        startActivityForResult(pickIntent, R.id.REQUEST_PICK_APPWIDGET);
    }

    void addEmptyData(Intent pickIntent) {
        ArrayList customInfo = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList customExtras = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK ) {
            if (requestCode == R.id.REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            }
            else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        }
        else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(homeView.getWidth()/3, homeView.getHeight()/3);
        lp.leftMargin = numWidgets * (homeView.getWidth()/3);

        hostView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                System.out.println("LONG PRESSED WIDGET");
                return false;
            }
        });

        homeView.addView(hostView,lp);
        slidingDrawer.bringToFront();
        numWidgets ++;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAppWidgetHost.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
    }



    public class LoadApps extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... params) {
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> pacsList = pm.queryIntentActivities(mainIntent, 0);
            pacs = new Pac[pacsList.size()];
            for(int I=0;I<pacsList.size();I++){
                pacs[I]= new Pac();
                pacs[I].icon=pacsList.get(I).loadIcon(pm);
                pacs[I].packageName=pacsList.get(I).activityInfo.packageName;
                pacs[I].name=pacsList.get(I).activityInfo.name;
                pacs[I].label=pacsList.get(I).loadLabel(pm).toString();
            }
            new SortApps().exchange_sort(pacs);
            return null;
        }


        @Override
        protected void onPostExecute(String result){

        }
    }




    public void  set_pacs() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pacsList = pm.queryIntentActivities(mainIntent, 0);
        pacs = new Pac[pacsList.size()];
        for (int I = 0; I < pacsList.size(); I++) {
            pacs[I] = new Pac();
            pacs[I].icon = pacsList.get(I).loadIcon(pm);
            pacs[I].packageName = pacsList.get(I).activityInfo.packageName;
            pacs[I].name = pacsList.get(I).activityInfo.name;
            pacs[I].label = pacsList.get(I).loadLabel(pm).toString();
        }
        new SortApps().exchange_sort(pacs);
        drawerAdapterObject = new DrawerAdapter(this, pacs);
        drawerGrid.setAdapter(drawerAdapterObject);
        drawerGrid.setOnItemClickListener(new DrawerClickListener(this, pacs, pm));
        drawerGrid.setOnItemLongClickListener(new DrawerLongClickListener(this, slidingDrawer, homeView, pacs));

    }




















    public class PacReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            set_pacs();
        }
    }


    @Override
    public void onBackPressed() {

        return;
    }





}