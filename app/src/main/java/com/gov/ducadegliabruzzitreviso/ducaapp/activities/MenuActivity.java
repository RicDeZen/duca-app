package com.gov.ducadegliabruzzitreviso.ducaapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.LinkTask;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.MyUtils;
import com.lukedeighton.wheelview.WheelView;
import com.lukedeighton.wheelview.adapter.WheelArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    //variabili di classe
    private Context context;
    private String data_path;
    private Resources res;
    private List<Drawable> menuItems = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private WheelView wheelView;
    private boolean[] valid = new boolean[4];
    //private float density
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //inizializzazione di base
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        data_path = context.getFilesDir() + "/DucaApp";
        res = getResources();
        setContentView(R.layout.activity_menu);
        TextView textViewMenuTitle = (TextView)findViewById(R.id.textView_menu_title);
        textViewMenuTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyUtils.pixelForAll(36, context));
        //setup canali notifiche
        if(Build.VERSION.SDK_INT >= 26){
            NotificationManager manager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_ID), getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.channel_desc));
            NotificationChannel channel2 = new NotificationChannel(getString(R.string.channel2_ID), getString(R.string.channel2_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription(getString(R.string.channel2_desc));
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(channel2);
        }

        //setup pulsante logo
        ImageButton imageButton = (ImageButton)findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.URL_sito))));
            }
        });

        //setup pulsante Sync
        final ImageView syncButton = (ImageView)findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                class ProgressThread extends Thread{
                    private LinkTask a;
                    public ProgressThread(LinkTask a){
                        this.a = a;
                    }
                    public void run(){
                        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                        while(a.getStatus() != AsyncTask.Status.FINISHED){
                            try{Thread.sleep(500);}
                            catch(InterruptedException e){e.printStackTrace();}
                        }
                        syncButton.clearAnimation();
                        AnimatedVectorDrawableCompat avd;
                        if(!a.hasError()){
                            avd = AnimatedVectorDrawableCompat.create(context, R.drawable.tick_vector);
                            syncButton.setImageDrawable(avd);
                            Animatable animatable = (Animatable) syncButton.getDrawable();
                            animatable.start();
                        }
                        else syncButton.setImageResource(R.drawable.ic_sync_icon_red);
                        checkUrlValidity();
                        handleUrlValidity();
                    }
                }
                syncButton.setImageResource(R.drawable.ic_sync_icon);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.you_spin_me_right_round_baby_right_round);
                animation.setFillAfter(true);
                syncButton.startAnimation(animation);
                LinkTask linkTask = new LinkTask(getApplicationContext());
                linkTask.execute(sharedPreferences.getString("pref_url_site", "https://www.liceoduca.edu.it"), data_path);
                new ProgressThread(linkTask).start();
            }
        });

        //setup popup Orari
        final View popupLayout = getLayoutInflater().inflate(R.layout.popup_window, null);
        final PopupWindow popupWindow = new PopupWindow(popupLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(R.style.popup_window_animation);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        TextView textView_orario1 = (TextView) popupLayout.findViewById(R.id.textView_orarioDocenti);
        TextView textView_orario2 = (TextView) popupLayout.findViewById(R.id.textView_orarioClassi);
        textView_orario1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String orarioDocenti = sharedPreferences.getString("pref_url_orario_docenti", "empty");
                    if(URLUtil.isValidUrl(orarioDocenti)) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orarioDocenti)));
                }
                catch(IndexOutOfBoundsException e){}
            }
        });
        textView_orario2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String orarioClassi = sharedPreferences.getString("pref_url_orario_classi", "empty");
                    if(URLUtil.isValidUrl(orarioClassi)) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orarioClassi)));;
                }
                catch(IndexOutOfBoundsException e){}
            }
        });

        //setup textSwitcher centrale
        final TextSwitcher textSwitcher = (TextSwitcher)findViewById(R.id.text_switcher);
        textSwitcher.setCurrentText("NOTIZIE");
        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

        //setup lista circolare
        menuItems.add(res.getDrawable(R.drawable.news_vector));
        menuItems.add(res.getDrawable(R.drawable.docs_vector));
        menuItems.add(res.getDrawable(R.drawable.sos_vector));
        menuItems.add(res.getDrawable(R.drawable.timetable_vector));
        menuItems.add(res.getDrawable(R.drawable.settings_vector));

        wheelView = (WheelView) findViewById(R.id.menu_wheelview);
        wheelView.setWheelItemRadius(MyUtils.pixelForAll(43, context));
        wheelView.setWheelRadius(MyUtils.pixelForAll(276, context));
        wheelView.setWheelOffsetY(MyUtils.pixelForAll(60, context));
        ConstraintLayout kingLayout = (ConstraintLayout) findViewById(R.id.king_layout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(kingLayout);
        constraintSet.connect(R.id.imageView5, ConstraintSet.BOTTOM, R.id.menu_wheelview, ConstraintSet.BOTTOM, MyUtils.pixelForAll(216, context));
        constraintSet.applyTo(kingLayout);
        wheelView.setAdapter(new MyWheelAdapter(menuItems));
        checkUrlValidity();
        handleUrlValidity();
        wheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
            @Override
            public void onWheelItemClick(WheelView parent, int position, boolean isSelected) {
                switch(position){
                    case 0:
                        if(valid[0]) startActivity(new Intent(context, FeedActivity.class));
                        break;
                    case 1:
                        if(valid[1]) startActivity(new Intent(context, CircolariActivity.class));
                        break;
                    case 2:
                        if(MyUtils.checkNetwork(context)){
                            Intent intent = new Intent(context, BrowserActivity.class);
                            intent.putExtra("URL", "http://myduca.it/sos/login");
                            startActivity(intent);
                        }
                        break;
                    case 3:
                        //popup orario
                        if(valid[2] && valid[3]){
                            parent.setAngle(parent.getAngleForPosition(2));
                            int offsetY = Math.round( (parent.getWheelRadius() - parent.getWheelItemRadius() - parent.getWheelOffsetY() + bottomBarHeight()) );
                            popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, offsetY);
                        }
                        break;
                    case 4:
                        startActivity(new Intent(context, SettingsActivity.class));
                        break;
                }
            }
        });
        wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectListener() {
            @Override
            public void onWheelItemSelected(WheelView parent, Drawable itemDrawable, int position) {
                switch(position){
                    case 0:
                        textSwitcher.setText("NOTIZIE");
                        break;
                    case 1:
                        textSwitcher.setText("CIRCOLARI");
                        break;
                    case 2:
                        textSwitcher.setText("SOS STUDIO");
                        break;
                    case 3:
                        textSwitcher.setText("ORARIO");
                        break;
                    case 4:
                        textSwitcher.setText("IMPOSTAZIONI");
                        break;
                }
            }
        });
    }

    static class MyWheelAdapter extends WheelArrayAdapter<Drawable>{
        public MyWheelAdapter(List<Drawable> items){
            super(items);
        }
        @Override
        public Drawable getDrawable(int position) {
            return getItem(position);
        }
        @Override
        public int getCount() {
            return 5;
        }
    }

    public int bottomBarHeight(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            Display d = getWindowManager().getDefaultDisplay();
            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);
            int realHeight = realDisplayMetrics.heightPixels;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);
            int displayHeight = displayMetrics.heightPixels;
            return realHeight - displayHeight;
        } else {
            return 0;
        }
    }

    public void checkUrlValidity(){
        valid[0] = URLUtil.isValidUrl(sharedPreferences.getString("pref_url_site", "https://www.liceoduca.edu.it"));
        valid[1] = URLUtil.isValidUrl(sharedPreferences.getString("pref_url_circolari", "https://www.liceoduca.edu.it"));
        valid[2] = URLUtil.isValidUrl(sharedPreferences.getString("pref_url_orario_docenti", "empty"));
        valid[3] = URLUtil.isValidUrl(sharedPreferences.getString("pref_url_orario_classi", "empty"));
    }
    public void handleUrlValidity(){
        if(valid[0]) menuItems.set(0, res.getDrawable(R.drawable.news_vector));
        else menuItems.set(0, res.getDrawable(R.drawable.news_vector_unavailable));
        if(valid[1]) menuItems.set(1, res.getDrawable(R.drawable.docs_vector));
        else menuItems.set(0, res.getDrawable(R.drawable.docs_vector_unavailable));
        if(valid[2] && valid[3]) menuItems.set(3, res.getDrawable(R.drawable.timetable_vector));
        else menuItems.set(3, res.getDrawable(R.drawable.timetable_vector_unavailable));
        wheelView.setAdapter(new MyWheelAdapter(menuItems));
    }
}