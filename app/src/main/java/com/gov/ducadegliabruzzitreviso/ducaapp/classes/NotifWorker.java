package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;
import com.gov.ducadegliabruzzitreviso.ducaapp.activities.CircolariActivity;
import com.gov.ducadegliabruzzitreviso.ducaapp.activities.FeedActivity;
import com.gov.ducadegliabruzzitreviso.ducaapp.activities.InfoActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NotifWorker extends Worker {
    private static String data_path;
    public NotifWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        data_path = getInputData().getString("data_path");
        checkFeed();
        checkCircolari();
        return Result.success();
    }

    private void checkFeed(){
        try {
            URL url = new URL(getInputData().getString("url_feed"));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            InputStream isURL = con.getInputStream();
            File feed_file = new File(data_path + "/feed_file.xml");
            InputStream isFILE = new FileInputStream(feed_file);
            XMLFeedParser parser1 = new XMLFeedParser();
            XMLFeedParser parser2 = new XMLFeedParser();
            List OnlineItems = parser1.parse(isURL);
            List LocalItems = parser2.parse(isFILE);
            List news = new ArrayList();
            for(int i = 0; i < OnlineItems.size(); i++){
                boolean e = false;
                for(int j = 0; j < LocalItems.size(); j++){
                    if(((FeedItem)(OnlineItems.get(i))).title.equals(((FeedItem)(LocalItems.get(j))).title)){e = true;}
                }
                if(!e) news.add(OnlineItems.get(i));
            }
            if(news.size() == 1) sendNotificationFeed("Duca Degli Abruzzi | Nuova notizia", (FeedItem)(news.get(0)));
            if(news.size() > 1) sendBigNotificationFeed(news);
            isURL.close();
            isFILE.close();
        }catch(Exception e){ }
    }

    private void checkCircolari(){
        try {
            URL url = new URL(getInputData().getString("url_circolari"));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            CircolariParser parser1 = new CircolariParser();
            CircolariParser parser2 = new CircolariParser();
            InputStream isURL = con.getInputStream();
            File circolari_file = new File(data_path + "circolari.htm");
            List onlineItems = parser1.parse(isURL);
            isURL.close();
            List localItems = new ArrayList();
            try{
                InputStream isFILE = new FileInputStream(circolari_file);
                localItems = parser2.parse(isFILE);
                isFILE.close();
            }catch(Exception e){}
            List news = new ArrayList();
            for(int i = 0; i < onlineItems.size() && i < 10; i++){
                boolean exists = false;
                for(int j = 0; j < localItems.size() && j < 10; j++){
                    if(((Circolare)onlineItems.get(i)).titolo.equals(((Circolare)localItems.get(j)).titolo)){
                        exists = true;
                    }
                }
                if(!exists){
                    news.add(onlineItems.get(i));
                }
            }
            if(news.size() == 1) sendNotificationCircolari("Duca degli Abruzzi | Nuova Circolare", ((Circolare)news.get(0)).titolo);
            if(news.size() > 1) sendBigNotificationCircolari(news);
        }
        catch(Exception e){e.printStackTrace();}
    }

    public void sendNotificationFeed(String title, FeedItem item){
        Intent resultIntent = new Intent(getApplicationContext(), InfoActivity.class);
        resultIntent.putExtra("title", item.title);
        resultIntent.putExtra("description", item.description);
        resultIntent.putExtra("date", item.date);
        resultIntent.putExtra("link", item.link);
        resultIntent.putExtra("notif", true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp7455")
                .setContentTitle(title)
                .setContentText(item.title)
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(7455, notification.build());
    }

    public void sendNotificationCircolari(String title, String description){
        Intent resultIntent = new Intent(getApplicationContext(), CircolariActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp2690")
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(2690, notification.build());
    }

    public void sendBigNotificationCircolari(List news){
        Intent resultIntent = new Intent(getApplicationContext(), CircolariActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp2690")
                .setContentTitle("Duca degli Abruzzi | Nuove Circolari")
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        if(news.size() <= 6) notification.setContentText("Ci sono " + news.size() + " nuove circolari");
        else notification.setContentText("Ci sono almeno 6 nuove circolari");
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for(int i = 0; i < news.size(); i++){
            style.addLine(((Circolare)news.get(i)).titolo);
        }
        notification.setStyle(style);
        notificationManager.notify(2690, notification.build());
    }

    public void sendBigNotificationFeed(List news){
        Intent resultIntent = new Intent(getApplicationContext(), FeedActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp7455")
                .setContentTitle("Duca degli Abruzzi | Nuove notizie")
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        if(news.size() <= 6) notification.setContentText("Ci sono " + news.size() + " nuove notizie");
        else notification.setContentText("Ci sono almeno 6 nuove notizie");
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for(int i = 0; i < news.size(); i++){
            style.addLine(((FeedItem)news.get(i)).title);
        }
        notification.setStyle(style);
        notificationManager.notify(7455, notification.build());
    }
}